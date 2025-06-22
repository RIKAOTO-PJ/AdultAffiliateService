package copel.affiliateproductpackage.adult.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import copel.affiliateproductpackage.adult.api.TwitterAPI;
import copel.affiliateproductpackage.adult.api.TwitterAPI.MediaType;
import copel.affiliateproductpackage.adult.api.商品情報API;
import copel.affiliateproductpackage.adult.api.entity.TwitterAPIリクエスト;
import copel.affiliateproductpackage.adult.api.entity.TwitterAPIレスポンス;
import copel.affiliateproductpackage.adult.api.entity.商品情報APIリクエスト;
import copel.affiliateproductpackage.adult.api.entity.商品情報APIリクエスト.フロア;
import copel.affiliateproductpackage.adult.api.entity.商品情報APIレスポンス;
import copel.affiliateproductpackage.adult.api.entity.商品情報APIレスポンス.Item;
import copel.affiliateproductpackage.adult.database.RIKAOTO_PUBLISHED_PRODUCTEntity;
import copel.affiliateproductpackage.adult.database.RIKAOTO_PUBLISHED_PRODUCTEntityLot;
import copel.affiliateproductpackage.adult.unit.Image;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdultAffiliateMangaTwitterService {
    /**
     * 環境変数「FANZA_API_KEY」.
     */
    private static final String FANZA_API_KEY = System.getenv("FANZA_API_KEY");
    /**
     * 環境変数「FANZA_AFFILIATE_ID」.
     */
    private static final String FANZA_AFFILIATE_ID = System.getenv("FANZA_AFFILIATE_ID");
    /**
     * 環境変数「DOUJIN_TWITTER_MEDIA_ID」.
     */
    private static final String DOUJIN_TWITTER_MEDIA_ID = System.getenv("DOUJIN_TWITTER_MEDIA_ID");
    /**
     * 環境変数「TWITTER_API_KEY_2」
     */
    private final static String TWITTER_API_KEY_2 = System.getenv("TWITTER_API_KEY_2");
    /**
     * 環境変数「TWITTER_API_SECRET_2」
     */
    private final static String TWITTER_API_SECRET_2 = System.getenv("TWITTER_API_SECRET_2");
    /**
     * 環境変数「TWITTER_ACCESS_TOKEN_2」
     */
    private final static String TWITTER_ACCESS_TOKEN_2 = System.getenv("TWITTER_ACCESS_TOKEN_2");
    /**
     * 環境変数「TWITTER_ACCESS_SECRET_2」
     */
    private final static String TWITTER_ACCESS_SECRET_2 = System.getenv("TWITTER_ACCESS_SECRET_2");

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
        商品情報APIリクエスト 商品情報APIリクエスト = new 商品情報APIリクエスト(FANZA_API_KEY, FANZA_AFFILIATE_ID, フロア.FANZA_同人_同人);
        商品情報APIリクエスト.setKeyword("ふたなり");
        商品情報APIリクエスト.setSort("review");
        商品情報APIリクエスト.setGteDate(today.minusYears(3).format(DateTimeFormatter.ofPattern(DATETIME_ISO_PATTERN)));
        商品情報APIリクエスト.setLteDate(today.minusDays(1).format(DateTimeFormatter.ofPattern(DATETIME_ISO_PATTERN)));

        商品情報APIレスポンス 商品情報APIレスポンス = 商品情報API.get(商品情報APIリクエスト);
        log.info("総件数: {}", 商品情報APIレスポンス.getTotalCount());

        // 商品がヒットしなかった場合、処理を終了する
        if (商品情報APIレスポンス.getTotalCount() < 1) {
            log.info("検索条件に一致する商品が存在しません。処理を終了します。");
            return;
        }

        // RIKAOTO_PUBLISHED_PRODUCTEntityテーブルに登録されているこのブログの商品ID一覧を取得
        RIKAOTO_PUBLISHED_PRODUCTEntityLot RIKAOTO_PUBLISHED_PRODUCTEntityLot = new RIKAOTO_PUBLISHED_PRODUCTEntityLot();
        RIKAOTO_PUBLISHED_PRODUCTEntityLot.fetchByPk(DOUJIN_TWITTER_MEDIA_ID);

        // 記事化する対象の商品を選定する
        Item item = null;
        while (商品情報APIレスポンス.getHits() > 0 && item == null) {
            // まだ記事化していない、かつサンプル画像が存在する同人コミック、かつ2件以上のレビューを持つ商品を1つ選定
            item = 商品情報APIレスポンス.getItems().stream()
                    .filter(element -> element.hasSampleImage())
                    .filter(element -> element.getSampleImageURL().is同人Comic())
                    .filter(element -> !RIKAOTO_PUBLISHED_PRODUCTEntityLot.exist(DOUJIN_TWITTER_MEDIA_ID, element.getProduct_id()))
                    .findFirst()
                    .orElse(null);

            // 該当する商品がなければoffsetを更新し再検索
            if (item == null) {
                商品情報APIリクエスト.nextOffset();
                商品情報APIレスポンス = 商品情報API.get(商品情報APIリクエスト);
                log.info("まだ記事化していない、かつサンプル画像が存在する同人コミック、かつ2件以上のレビューを持つ商品が見つかりませんでした。再検索します。リクエスト: {}", 商品情報APIリクエスト);
            }
        }

        // 記事化していない商品が1つもなければ、処理終了
        if (item == null) {
            log.info("まだ記事化していない、かつサンプル画像が存在する同人コミック、かつ2件以上のレビューを持つ商品がありません。検索条件に修正を加えるか、新商品が発売されるまでお待ちください。処理を終了します。");
            return;
        }
        log.info("記事化する商品: {}", item);

        // 画像を全てダウンロードしTwitterにアップロード→メディアID取得
        List<String> urlList = item.getSampleImageURL().getSamples();
        List<String> mediaIdList = new ArrayList<String>();
        for (int i = 0; i < urlList.size(); i++) {
            Image image = new Image();
            image.downloadAndRead(urlList.get(i));

            // 動画をTwitterにULしメディアIDを取得する
            try {
                String twitterMediaId = TwitterAPI.uploadMedia(image.getContent(), MediaType.Image, TWITTER_API_KEY_2, TWITTER_API_SECRET_2, TWITTER_ACCESS_TOKEN_2, TWITTER_ACCESS_SECRET_2);
                if (twitterMediaId == null || twitterMediaId.isEmpty()) {
                    log.info("画像のアップロードに失敗したため、ツイートはせず処理を正常に終了します");
                    return;
                } else {
                    mediaIdList.add(twitterMediaId);
                    log.info("画像のアップロードに成功 mediaId: {}", twitterMediaId);
                }
            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        int postCount = (mediaIdList.size() + 3) / 4;

        // ツイートする
        try {
            // 表紙ポスト
            TwitterAPIリクエスト TwitterAPIリクエスト = new TwitterAPIリクエスト();
            TwitterAPIリクエスト.setText(item.getTitle() + "(1/" + Integer.toString(postCount + 1) + ")");
            TwitterAPIリクエスト.addMedia(mediaIdList.get(urlList.size() - 2));
            log.info("1ポスト目をツイートします: {}", TwitterAPIリクエスト);
            TwitterAPIレスポンス response = TwitterAPI.post(TwitterAPIリクエスト, TWITTER_API_KEY_2, TWITTER_API_SECRET_2, TWITTER_ACCESS_TOKEN_2, TWITTER_ACCESS_SECRET_2);

            // 2枚目以降のポスト
            int i = 0;
            boolean isContinue = true;
            while (isContinue) {
                TwitterAPIリクエスト = new TwitterAPIリクエスト();
                TwitterAPIリクエスト.setText(item.getTitle() + "(" + Integer.toString(i + 2) + "/" + Integer.toString(postCount + 1) + ")");
                TwitterAPIリクエスト.setReplyTweetId(response.getTweetId());
                try {
                    TwitterAPIリクエスト.addMedia(mediaIdList.get(i * 4 + 1));
                    TwitterAPIリクエスト.addMedia(mediaIdList.get(i * 4 + 2));
                    TwitterAPIリクエスト.addMedia(mediaIdList.get(i * 4 + 3));
                    TwitterAPIリクエスト.addMedia(mediaIdList.get(i * 4 + 4));
                } catch (IndexOutOfBoundsException e) {
                    isContinue = false;
                }
                log.info("{}ポスト目をツイートします: {}", i + 2, TwitterAPIリクエスト);
                response = TwitterAPI.post(TwitterAPIリクエスト, TWITTER_API_KEY_2, TWITTER_API_SECRET_2, TWITTER_ACCESS_TOKEN_2, TWITTER_ACCESS_SECRET_2);
                i++;
            }

            // 最終ポスト
            TwitterAPIリクエスト = new TwitterAPIリクエスト();
            TwitterAPIリクエスト.setText("続きはこちら▼\n"+ item.getAffiliateURL());
            TwitterAPIリクエスト.setReplyTweetId(response.getTweetId());
            log.info("最後のポストをツイートします: {}", TwitterAPIリクエスト);
            response = TwitterAPI.post(TwitterAPIリクエスト, TWITTER_API_KEY_2, TWITTER_API_SECRET_2, TWITTER_ACCESS_TOKEN_2, TWITTER_ACCESS_SECRET_2);
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // RIKAOTO_PUBLISHED_PRODUCTテーブルにレコードを登録する
        RIKAOTO_PUBLISHED_PRODUCTEntity entity = new RIKAOTO_PUBLISHED_PRODUCTEntity();
        entity.setMediaId(DOUJIN_TWITTER_MEDIA_ID);
        entity.setProductId(item.getProduct_id());
        entity.save();
        log.info("RIKAOTO_PUBLISHED_PRODUCTテーブルにレコードを登録しました: {}", entity.toString());
    }
}
