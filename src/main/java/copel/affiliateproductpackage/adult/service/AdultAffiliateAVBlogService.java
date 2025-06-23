package copel.affiliateproductpackage.adult.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import copel.affiliateproductpackage.adult.api.TwitterAPI;
import copel.affiliateproductpackage.adult.api.TwitterAPI.MediaType;
import copel.affiliateproductpackage.adult.api.WordPressRESTAPI;
import copel.affiliateproductpackage.adult.api.WordPressRESTAPI.WordPressCategory;
import copel.affiliateproductpackage.adult.api.WordPressRESTAPI.WordPressCategoryLot;
import copel.affiliateproductpackage.adult.api.WordPressRESTAPI.WordPressTag;
import copel.affiliateproductpackage.adult.api.WordPressRESTAPI.WordPressTagLot;
import copel.affiliateproductpackage.adult.api.商品情報API;
import copel.affiliateproductpackage.adult.api.entity.TwitterAPIリクエスト;
import copel.affiliateproductpackage.adult.api.entity.TwitterAPIレスポンス;
import copel.affiliateproductpackage.adult.api.entity.WordPressRESTAPIリクエスト;
import copel.affiliateproductpackage.adult.api.entity.商品情報APIリクエスト;
import copel.affiliateproductpackage.adult.api.entity.商品情報APIリクエスト.フロア;
import copel.affiliateproductpackage.adult.api.entity.商品情報APIレスポンス;
import copel.affiliateproductpackage.adult.api.entity.商品情報APIレスポンス.Actress;
import copel.affiliateproductpackage.adult.api.entity.商品情報APIレスポンス.Genre;
import copel.affiliateproductpackage.adult.api.entity.商品情報APIレスポンス.Item;
import copel.affiliateproductpackage.adult.database.RIKAOTO_PUBLISHED_PRODUCTEntity;
import copel.affiliateproductpackage.adult.database.RIKAOTO_PUBLISHED_PRODUCTEntityLot;
import copel.affiliateproductpackage.adult.gpt.Gemini;
import copel.affiliateproductpackage.adult.gpt.GptAnswer;
import copel.affiliateproductpackage.adult.gpt.Prompt;
import copel.affiliateproductpackage.adult.gpt.Transformer;
import copel.affiliateproductpackage.adult.unit.Image;
import copel.affiliateproductpackage.adult.unit.Video;
import copel.affiliateproductpackage.adult.unit.WebBrowser;
import lombok.extern.slf4j.Slf4j;

/**
 * アダルトアフィリエイトAVブログにAI生成した記事を投稿するサービス.
 *
 * @author 鈴木一矢
 *
 */
@Slf4j
public class AdultAffiliateAVBlogService {
    /**
     * 環境変数「GEMINI_API_KEY」.
     */
    private static final String GEMINI_API_KEY = System.getenv("GEMINI_API_KEY");
    /**
     * 環境変数「FANZA_API_KEY」.
     */
    private static final String FANZA_API_KEY = System.getenv("FANZA_API_KEY");
    /**
     * 環境変数「FANZA_AFFILIATE_ID」.
     */
    private static final String FANZA_AFFILIATE_ID = System.getenv("FANZA_AFFILIATE_ID");
    /**
     * 環境変数「WORDPRESS_MEDIA_ID」.
     */
    private static final String WORDPRESS_MEDIA_ID = System.getenv("WORDPRESS_MEDIA_ID");
    /**
     * 環境変数「WORDPRESS_USER_NAME」.
     */
    private static final String WORDPRESS_USER_NAME = System.getenv("WORDPRESS_USER_NAME");
    /**
     * 環境変数「WORDPRESS_APPLICATION_PASSWORD」.
     */
    private static final String WORDPRESS_APPLICATION_PASSWORD = System.getenv("WORDPRESS_APPLICATION_PASSWORD");
    /**
     * 環境変数「TWITTER_API_KEY」
     */
    private final static String TWITTER_API_KEY = System.getenv("TWITTER_API_KEY");
    /**
     * 環境変数「TWITTER_API_SECRET」
     */
    private final static String TWITTER_API_SECRET = System.getenv("TWITTER_API_SECRET");
    /**
     * 環境変数「TWITTER_ACCESS_TOKEN」
     */
    private final static String TWITTER_ACCESS_TOKEN = System.getenv("TWITTER_ACCESS_TOKEN");
    /**
     * 環境変数「TWITTER_ACCESS_SECRET」
     */
    private final static String TWITTER_ACCESS_SECRET = System.getenv("TWITTER_ACCESS_SECRET");

    /**
     * ISO形式の日付フォーマットパターン.
     */
    private static final String DATETIME_ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * メイン文.
     *
     * @param args コマンドライン引数.
     */
    public static void main(String[] args) {
        try {
            execute();
        } catch (IOException | RuntimeException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 処理を実行する.
     *
     * @throws IOException
     * @throws RuntimeException
     * @throws InterruptedException
     */
    public static void execute() throws IOException, RuntimeException, InterruptedException {
        // 商品を検索（3年前同日～1日前までの間の作品に限定）
        LocalDateTime today = LocalDate.now().atStartOfDay();
        商品情報APIリクエスト 商品情報APIリクエスト = new 商品情報APIリクエスト(FANZA_API_KEY, FANZA_AFFILIATE_ID, フロア.FANZA_動画_素人);
        商品情報APIリクエスト.setKeyword("");
        商品情報APIリクエスト.setSort("review");
        商品情報APIリクエスト.setGteDate(today.minusYears(3).format(DateTimeFormatter.ofPattern(DATETIME_ISO_PATTERN)));
        商品情報APIリクエスト.setLteDate(today.minusDays(1).format(DateTimeFormatter.ofPattern(DATETIME_ISO_PATTERN)));
        log.debug("商品情報APIリクエスト: {}", 商品情報APIリクエスト);

        商品情報APIレスポンス 商品情報APIレスポンス = 商品情報API.get(商品情報APIリクエスト);
        log.info("総件数: {}件", 商品情報APIレスポンス.getTotalCount());

        // 商品がヒットしなかった場合、処理を終了する
        if (商品情報APIレスポンス.getTotalCount() < 1) {
            log.info("検索条件に一致する商品が存在しません。処理を終了します。");
            return;
        }

        // RIKAOTO_PUBLISHED_PRODUCTEntityテーブルに登録されているこのブログの商品ID一覧を取得
        RIKAOTO_PUBLISHED_PRODUCTEntityLot RIKAOTO_PUBLISHED_PRODUCTEntityLot = new RIKAOTO_PUBLISHED_PRODUCTEntityLot();
        RIKAOTO_PUBLISHED_PRODUCTEntityLot.fetchByPk(WORDPRESS_MEDIA_ID);

        // 記事化する対象の商品を選定する
        Item item = null;
        while (商品情報APIレスポンス.getHits() > 0 && item == null) {
            // まだ記事化していない、かつサンプル動画が存在する、かつ2件以上のレビューを持つ商品を1つ選定
            item = 商品情報APIレスポンス.getItems().stream()
                    .filter(element -> element.hasSampleMovie())
                    .filter(element -> element.hasReviewAtLeast(2))
                    .filter(element -> !RIKAOTO_PUBLISHED_PRODUCTEntityLot.exist(WORDPRESS_MEDIA_ID, element.getProduct_id()))
                    .findFirst()
                    .orElse(null);

            // 該当する商品がなければoffsetを更新し再検索
            if (item == null) {
                商品情報APIリクエスト.nextOffset();
                商品情報APIレスポンス = 商品情報API.get(商品情報APIリクエスト);
                log.info("まだ記事化していない、かつサンプル動画が存在する、かつ2件以上のレビューを持つ商品が見つかりませんでした。再検索します。リクエスト: {}", 商品情報APIリクエスト);
            }
        }

        // 記事化していない商品が1つもなければ、処理終了
        if (item == null) {
            log.info("まだ記事化していない、かつサンプル動画が存在する、かつ2件以上のレビューを持つ商品がありません。検索条件に修正を加えるか、新商品が発売されるまでお待ちください。処理を終了します。");
            return;
        }
        log.info("記事化する商品: {}", item);

        // Seleniumでレビューコメントと作品あらすじを取得
        WebBrowser webBrowser = new WebBrowser(true);

        webBrowser.access(item.getURL());
        log.debug("「{}」にアクセス", item.getURL());

        // 18歳以上のチェックが存在すればクリック
        if (webBrowser.existsByXpath("//*[@id=\":R6:\"]/div[2]/div[2]/div[3]/div[1]/a")) {
            webBrowser.clickByXpath("//*[@id=\":R6:\"]/div[2]/div[2]/div[3]/div[1]/a");
            log.debug("「18歳以上」をクリック");
        }

        // 広告が存在すれば、広告の閉じるボタンを押下する
        if (webBrowser.existsByXpath("//*[@id=\"campaign-popup-wrap\"]")) {
            log.debug("広告が見つかりました");
            webBrowser.clickByXpath("//*[@id=\"campaign-popup-close\"]");
            log.debug("「閉じる」をクリック");
        }

        // あらすじを取得
        String あらすじ = "";
        if (webBrowser.existsByXpath("//*[@id=\"mu\"]/div/table/tbody/tr/td[1]/div[4]")) {
            log.debug("あらすじが見つかりました");
            webBrowser.clickByXpath("//*[@id=\"mu\"]/div/table/tbody/tr/td[1]/div[4]");
            あらすじ = webBrowser.getTextByXpath("//*[@id=\"mu\"]/div/table/tbody/tr/td[1]/div[4]");
            log.info("あらすじを取得しました: {}", あらすじ);
        }

        // レビューを評価高い順に取得
        String review = "";
        if (webBrowser.existsByXpath("//*[@id=\"review\"]/div[2]/div/div[3]/div[1]/div[1]/div/select")) {
            log.debug("レビューコメントが見つかりました");
            webBrowser.scrollToElementByXpath("//*[@id=\"review\"]/div[2]/div/div[3]/div[1]/div[1]/div/select");
            webBrowser.selectOptionByXpath("//*[@id=\"review\"]/div[2]/div/div[3]/div[1]/div[1]/div/select", "評価が高い順");
            webBrowser.wait(1);
            try {
                review += webBrowser.getTextByXpath("//*[@id=\"review\"]/div[2]/div/div[3]/div[2]/ul/li[1]/div[1]") + webBrowser.getTextByXpath("//*[@id=\"review\"]/div[2]/div/div[3]/div[2]/ul/li[1]/p/span[2]");
                review += webBrowser.getTextByXpath("//*[@id=\"review\"]/div[2]/div/div[3]/div[2]/ul/li[2]/div[1]") + webBrowser.getTextByXpath("//*[@id=\"review\"]/div[2]/div/div[3]/div[2]/ul/li[2]/p/span[2]");
                review += webBrowser.getTextByXpath("//*[@id=\"review\"]/div[2]/div/div[3]/div[2]/ul/li[3]/div[1]") + webBrowser.getTextByXpath("//*[@id=\"review\"]/div[2]/div/div[3]/div[2]/ul/li[3]/p/span[2]");
            } catch (Exception e) {
                log.debug("レビューは3件以下です");
            }
            log.info("レビューを取得しました: {}", review);
        }

        // Seleniumを終了
        webBrowser.quit();

        // タイトル生成プロンプトを作成
        Prompt タイトル生成プロンプト = new Prompt();
        if (item != null) {
            タイトル生成プロンプト.createPromptByFilePath("/prompt/アダルトブログタイトル生成プロンプト.txt");
            タイトル生成プロンプト.setValue(item.getTitle());
            タイトル生成プロンプト.setValue(item.getIteminfo().getKeywords());
            タイトル生成プロンプト.setValue(あらすじ);
        }
        log.debug("タイトル生成プロンプトを生成: {}", タイトル生成プロンプト.toString());

        // 記事生成プロンプトを作成
        Prompt 記事生成プロンプト = new Prompt();
        if (item != null) {
            記事生成プロンプト.createPromptByFilePath("/prompt/アダルトブログ記事生成プロンプト.txt");
            記事生成プロンプト.setValue(item.getTitle());
            記事生成プロンプト.setValue(あらすじ);
            記事生成プロンプト.setValue(review);
            記事生成プロンプト.setValue(item.getProduct_id());
            記事生成プロンプト.setValue(item.getDate());
            記事生成プロンプト.setValue(item.getIteminfo().getActressWords());
            記事生成プロンプト.setValue(item.getVolume());
            記事生成プロンプト.setValue(item.getReview().getAverage());
            記事生成プロンプト.setValue(item.getIteminfo().getKeywords());
            記事生成プロンプト.setValue(item.getSampleImageURL().getSample_l().getImage().size() > 0 ? item.getSampleImageURL().getSample_l().getImage().get(0) : "");
            記事生成プロンプト.setValue(item.getSampleImageURL().getSample_l().getImage().size() > 1 ? item.getSampleImageURL().getSample_l().getImage().get(1) : "");
            記事生成プロンプト.setValue(item.getSampleImageURL().getSample_l().getImage().size() > 2 ? item.getSampleImageURL().getSample_l().getImage().get(2) : "");
            記事生成プロンプト.setValue(item.getSampleImageURL().getSample_l().getImage().size() > 3 ? item.getSampleImageURL().getSample_l().getImage().get(3) : "");
            記事生成プロンプト.setValue(item.getSampleImageURL().getSample_l().getImage().size() > 4 ? item.getSampleImageURL().getSample_l().getImage().get(4) : "");
            記事生成プロンプト.setValue(item.getSampleImageURL().getSample_l().getImage().size() > 5 ? item.getSampleImageURL().getSample_l().getImage().get(5) : "");
            記事生成プロンプト.setValue(item.getSampleImageURL().getSample_l().getImage().size() > 6 ? item.getSampleImageURL().getSample_l().getImage().get(6) : "");
            記事生成プロンプト.setValue(item.getAffiliateURL());
            記事生成プロンプト.setValue(item.getSampleMovieTag());
        }
        log.debug("記事生成プロンプトを生成: {}", 記事生成プロンプト.toString());

        // サムネ用の画像をアップロード
        int mediaId = WordPressRESTAPI.uploadImage(item.getProduct_id() + ".jpg", item.getImageURL().getLarge(), WORDPRESS_USER_NAME, WORDPRESS_APPLICATION_PASSWORD);
        if (mediaId < 0) {
            log.error("サムネ画像のアップロードに失敗しました。他の画像を使用します。");
            mediaId = WordPressRESTAPI.uploadImage(item.getProduct_id() + ".jpg", item.getSampleImageURL().getSample_l().getImage().get(0), WORDPRESS_USER_NAME, WORDPRESS_APPLICATION_PASSWORD);
        }
        log.info("サムネイル画像ID: {}", mediaId);

        // この記事に付与するタグのLotを生成
        WordPressTagLot targetTagLot = new WordPressTagLot();

        // 既に登録済みのタグ一覧を取得する
        WordPressTagLot wordPressTagLot = WordPressRESTAPI.getTags(WORDPRESS_USER_NAME, WORDPRESS_APPLICATION_PASSWORD);
        log.debug("登録済みタグ一覧: {}", wordPressTagLot);

        // 商品情報APIで取得したキーワードを文字列リスト化
        List<String> keyWordsList = Arrays.stream(item.getIteminfo().getKeywords().split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        // 商品情報APIで取得したキーワードの内WordPressタグに登録されていないものを全て登録
        for (final String keyWord : keyWordsList) {
            // 空の文字列は無視
            if (keyWord != null && !"".equals(keyWord)) {
                if (!wordPressTagLot.contains(keyWord)) {
                    // WordPressにタグ登録する
                    int tagId = WordPressRESTAPI.addTag(keyWord, WORDPRESS_USER_NAME, WORDPRESS_APPLICATION_PASSWORD);
                    // タグ登録に成功した場合、本記事のタグとして追加する
                    if (tagId > 0) {
                        WordPressTag tag =  new WordPressTag();
                        tag.setId(tagId);
                        tag.setName(keyWord);
                        targetTagLot.addTag(tag);
                    }
                } else {
                    // 既に登録済みのタグであれば、本記事のタグとして追加する
                    targetTagLot.addTag(wordPressTagLot.getWordPressTagByName(keyWord));
                }
            }
        }
        log.info("付与対象タグ: {}", targetTagLot);

        // この記事に付与するカテゴリのLotを生成
        WordPressCategoryLot targetCategoryLot = new WordPressCategoryLot();

        // 既に登録済みのカテゴリ一覧を取得する
        WordPressCategoryLot wordPressCategoryLot = WordPressRESTAPI.getCategories(WORDPRESS_USER_NAME, WORDPRESS_APPLICATION_PASSWORD);
        log.debug("登録済みカテゴリ一覧: {}", wordPressCategoryLot);

        // ルートカテゴリを取得
        WordPressCategory ルートカテゴリ_女優 = wordPressCategoryLot.getWordPressCategoryByName("女優");
        WordPressCategory ルートカテゴリ_ジャンル = wordPressCategoryLot.getWordPressCategoryByName("ジャンル");

        // 商品情報APIで取得した女優名リストの内WordPressカテゴリに登録されていないものを全て登録
        for (final Actress actress : item.getIteminfo().getActress()) {
            if (!wordPressCategoryLot.contains(actress.getName())) {
                // WordPressにカテゴリ登録する
                int categoryId = WordPressRESTAPI.addCategory(actress.getName(), ルートカテゴリ_女優.getId(), WORDPRESS_USER_NAME, WORDPRESS_APPLICATION_PASSWORD);
                // カテゴリ登録に成功した場合、本記事のカテゴリとして追加する
                if (categoryId > 0) {
                    WordPressCategory category =  new WordPressCategory();
                    category.setId(categoryId);
                    category.setName(actress.getName());
                    targetCategoryLot.addCategory(category);
                }
            } else {
                // 既に登録済みのカテゴリであれば、本記事のカテゴリとして追加する
                targetCategoryLot.addCategory(wordPressCategoryLot.getWordPressCategoryByName(actress.getName()));
            }
        }

        // 商品情報APIで取得したジャンル名リストの内WordPressカテゴリに登録されていないものを全て登録
        for (final Genre genre : item.getIteminfo().getGenre()) {
            if (!wordPressCategoryLot.contains(genre.getName())) {
                // WordPressにカテゴリ登録する
                int categoryId = WordPressRESTAPI.addCategory(genre.getName(), ルートカテゴリ_ジャンル.getId(), WORDPRESS_USER_NAME, WORDPRESS_APPLICATION_PASSWORD);
                // カテゴリ登録に成功した場合、本記事のカテゴリとして追加する
                if (categoryId > 0) {
                    WordPressCategory category =  new WordPressCategory();
                    category.setId(categoryId);
                    category.setName(genre.getName());
                    targetCategoryLot.addCategory(category);
                }
            } else {
                // 既に登録済みのカテゴリであれば、本記事のカテゴリとして追加する
                targetCategoryLot.addCategory(wordPressCategoryLot.getWordPressCategoryByName(genre.getName()));
            }
        }
        log.info("付与対象カテゴリ: {}", targetCategoryLot);

        // Geminiでタイトルと記事を生成
        Transformer transformer = new Gemini(GEMINI_API_KEY);
        GptAnswer タイトル = transformer.generate(タイトル生成プロンプト.toString());
        タイトル.clean();
        GptAnswer 記事 = transformer.generate(記事生成プロンプト.toString());
        記事.clean();
        log.info("タイトル: {}", タイトル);
        log.info("記事: {}", 記事);

        // WordPressに記事を投稿
        WordPressRESTAPIリクエスト WordPressRESTAPIリクエスト = new WordPressRESTAPIリクエスト();
        WordPressRESTAPIリクエスト.setTitle(タイトル.toString());
        WordPressRESTAPIリクエスト.setContent(記事.toString());
        WordPressRESTAPIリクエスト.setFeaturedMedia(mediaId);
        WordPressRESTAPIリクエスト.setCategories(targetCategoryLot.getCategoryIdList());
        WordPressRESTAPIリクエスト.setTags(targetTagLot.getTagIdList());
        WordPressRESTAPIリクエスト.setStatus("publish");
        log.debug("WordPressRESTAPIリクエスト: {}", WordPressRESTAPIリクエスト);
        int responseStatus = WordPressRESTAPI.post(WordPressRESTAPIリクエスト, WORDPRESS_USER_NAME, WORDPRESS_APPLICATION_PASSWORD);

        // RIKAOTO_PUBLISHED_PRODUCTテーブルにレコードを登録する
        if (responseStatus/100 == 2) {
            RIKAOTO_PUBLISHED_PRODUCTEntity entity = new RIKAOTO_PUBLISHED_PRODUCTEntity();
            entity.setMediaId(WORDPRESS_MEDIA_ID);
            entity.setProductId(item.getProduct_id());
            entity.save();
            log.info("RIKAOTO_PUBLISHED_PRODUCTテーブルにレコードを登録しました: {}", entity.toString());
        } else {
            log.error("記事の投稿に失敗しました。アップロードしたメディアを削除します。");
            if (WordPressRESTAPI.deleteMedia(mediaId, WORDPRESS_USER_NAME, WORDPRESS_APPLICATION_PASSWORD)) {
                log.info("メディアの削除に成功しました。削除したメディアID: {}", mediaId);
            } else {
                log.error("メディアの削除も失敗しました。削除失敗したメディアID: {}", mediaId);
            }
        }

        // Twitterで作品紹介する
        try {
            String twitterMediaId = null;

            // mp4形式の動画が取得できるなら、ツイートを行う
            String sampleMovieMp4Url = item.getSampleMovieMp4Url();
            if (sampleMovieMp4Url == null) {
                log.info("mp4形式の動画が見つからないため、サンプル画像でツイートします");
                if (!item.getSampleImageURL().hasImage()) {
                    log.info("サンプル画像も見つかりませんでした。処理を終了します");
                    return;
                }

                // サンプル画像のバイナリデータをDL
                Image image = new Image();
                image.downloadAndRead(item.getSampleImageURL().getSamples().get(0));

                // 画像をTwitterにULしメディアIDを取得する
                twitterMediaId = TwitterAPI.uploadMedia(image.getContent(), MediaType.Image, TWITTER_API_KEY, TWITTER_API_SECRET, TWITTER_ACCESS_TOKEN, TWITTER_ACCESS_SECRET);
            } else {
                log.info("mp4形式の動画が見つかりました");

                // 動画のバイナリデータをDL
                Video video = new Video();
                video.downloadAndRead(sampleMovieMp4Url);

                // 動画をTwitterにULしメディアIDを取得する
                twitterMediaId = TwitterAPI.uploadMedia(video.getContent(), MediaType.Video, TWITTER_API_KEY, TWITTER_API_SECRET, TWITTER_ACCESS_TOKEN, TWITTER_ACCESS_SECRET);
            }

            // メディアIDが取得できていない場合、処理を中止
            if (twitterMediaId == null || twitterMediaId.isEmpty()) {
                log.info("Twitterにアップロードできないファイルのため、ツイートはせず処理を正常に終了します");
                return;
            }

            // ツイート内容を生成する
            Prompt ツイート生成プロンプト = new Prompt();
            ツイート生成プロンプト.createPromptByFilePath("/prompt/アダルトブログツイート生成プロンプト.txt");
            ツイート生成プロンプト.setValue(item.getTitle());
            ツイート生成プロンプト.setValue(あらすじ);
            ツイート生成プロンプト.setValue(review);
            GptAnswer ツイート = transformer.generate(ツイート生成プロンプト.toString());
            ツイート.clean();
            log.debug("ツイートを生成しました: {}", ツイート);

            // 作品の感想をツイート
            TwitterAPIリクエスト request = new TwitterAPIリクエスト();
            request.setText(ツイート.toString());
            request.addMedia(twitterMediaId);
            TwitterAPIレスポンス response = TwitterAPI.post(request, TWITTER_API_KEY, TWITTER_API_SECRET, TWITTER_ACCESS_TOKEN, TWITTER_ACCESS_SECRET);
            log.info("ツイートしました: {}", response);

            // アフィリエイトリンクをリプライにぶら下げる
            request = new TwitterAPIリクエスト();
            request.setText(item.getAffiliateURL());
            request.setReplyTweetId(response.getTweetId());
            response = TwitterAPI.post(request, TWITTER_API_KEY, TWITTER_API_SECRET, TWITTER_ACCESS_TOKEN, TWITTER_ACCESS_SECRET);
            log.info("アフィリエイトリンクをリプライしました: {}", response);

            log.info("全ての処理が正常に成功しました");
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
