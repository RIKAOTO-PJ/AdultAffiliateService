package copel.affiliateproductpackage.adult.api.entity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 商品情報APIレスポンス
 * <p>
 * 商品情報APIは、DMM.comの商品情報を取得するAPIです。各種商品を検索し、取得することができます。
 * APIで取得可能な情報は、商品タイトル、出演者、商品画像、レビュー件数・平均、価格等です。
 * アフィリエイトIDを設定することで、アフィリエイトリンクの生成も可能です。
 * 取得する商品は指定したサイト、サービス、フロアにより異なります。
 * また、商品は指定したキーワードなどのパラメータにより検索することが可能です。
 */
@Slf4j
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class 商品情報APIレスポンス {
    /**
     * キーワードリストから除外するキーワードのリスト.
     */
    private static final List<String> exclusionKeyWordList = Arrays.asList("4K", "ハイビジョン", "独占配信");

    /** リクエストオブジェクト */
    @JsonProperty("request")
    private Request request = new Request();

    /** レスポンスの結果 */
    @JsonProperty("result")
    private Result result = new Result();

    @JsonIgnore
    public int getStatusCode() {
        return this.result.getStatus();
    }

    /**
     * 指定した条件で絞り込んだ結果の総件数を取得します.
     *
     * @return 総件数
     */
    @JsonIgnore
    public int getTotalCount() {
        return this.result.getTotal_count();
    }

    @JsonIgnore
    public List<Item> getItems() {
        return this.result.getItems();
    }

    /**
     * 指定した条件で取得しこのオブジェクトにもつ商品の件数を返却します.
     *
     * @return 取得件数
     */
    @JsonIgnore
    public int getHits() {
        return this.result.getItems().size();
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request {
        /** パラメータ */
        @JsonProperty("parameters")
        private Parameters parameters = new Parameters();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Parameters {
        /** API ID（ユーザー固有のID） */
        @JsonProperty("api_id")
        private String api_id = "";

        /** アフィリエイトID */
        @JsonProperty("affiliate_id")
        private String affiliate_id = "";

        /** 対象サイト（FANZA等） */
        @JsonProperty("site")
        private String site = "";

        /** サービス（digitalなど） */
        @JsonProperty("service")
        private String service = "";

        /** フロア（videoaなど） */
        @JsonProperty("floor")
        private String floor = "";

        /** 検索キーワード */
        @JsonProperty("keyword")
        private String keyword = "";
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        /** ステータスコード */
        @JsonProperty("status")
        private int status;

        /** 取得件数 */
        @JsonProperty("result_count")
        private int result_count;

        /** 総件数 */
        @JsonProperty("total_count")
        private int total_count;

        /** 開始位置 */
        @JsonProperty("first_position")
        private int first_position;

        /** 商品情報リスト */
        @JsonProperty("items")
        private List<Item> items = new ArrayList<Item>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        /** サービスコード */
        @JsonProperty("service_code")
        private String service_code = "";

        /** サービス名 */
        @JsonProperty("service_name")
        private String service_name = "";

        /** フロアコード */
        @JsonProperty("floor_code")
        private String floor_code = "";

        /** フロア名 */
        @JsonProperty("floor_name")
        private String floor_name = "";

        /** カテゴリ名 */
        @JsonProperty("category_name")
        private String category_name = "";

        /** コンテンツID */
        @JsonProperty("content_id")
        private String content_id = "";

        /** 商品ID */
        @JsonProperty("product_id")
        private String product_id = "";

        /** タイトル */
        @JsonProperty("title")
        private String title = "";

        /** ボリューム情報 */
        @JsonProperty("volume")
        private String volume = "";

        /** レビュー情報 */
        @JsonProperty("review")
        private Review review = new Review();

        /** 商品URL */
        @JsonProperty("URL")
        private String URL = "";

        /** アフィリエイトURL */
        @JsonProperty("affiliateURL")
        private String affiliateURL = "";

        /** 画像URL群 */
        @JsonProperty("imageURL")
        private ImageURL imageURL = new ImageURL();

        /** サンプル画像URL群 */
        @JsonProperty("sampleImageURL")
        private SampleImageURL sampleImageURL = new SampleImageURL();

        /** サンプル動画URL群 */
        @JsonProperty("sampleMovieURL")
        private SampleMovieURL sampleMovieURL = new SampleMovieURL();

        /** 価格情報 */
        @JsonProperty("prices")
        private Prices prices = new Prices();

        /** 配信日時 */
        @JsonProperty("date")
        private String date = "";

        /** 商品詳細情報 */
        @JsonProperty("iteminfo")
        private ItemInfo iteminfo = new ItemInfo();

        /** キャンペーン情報 */
        @JsonProperty("campaign")
        private List<Campaign> campaign = new ArrayList<Campaign>();

        /**
         * 引数で指定した件数以上のレビュー件数を持つかどうかを判定します.
         *
         * @param count レビュー件数
         * @return 引数で指定した件数以上のレビュー件数を持てばtrue、それ以外はfalse
         */
        @JsonIgnore
        public boolean hasReviewAtLeast(final int count) {
            return this.review.getCount() >= count;
        }

        /**
         * サンプル画像が存在するかどうかを判定します.
         *
         * @return サンプル動画が存在すればtrue、それ以外はfalse.
         */
        @JsonIgnore
        public boolean hasSampleImage() {
            return this.sampleImageURL != null && this.sampleImageURL.hasImage();
        }

        /**
         * サンプル動画が存在するかどうかを判定します.
         *
         * @return サンプル動画が存在すればtrue、それ以外はfalse.
         */
        @JsonIgnore
        public boolean hasSampleMovie() {
            return this.sampleMovieURL != null && this.sampleMovieURL.hasMovie();
        }

        /**
         * サンプル動画の動画タグを生成する.
         *
         * @return 動画タグ
         */
        @JsonIgnore
        public String getSampleMovieTag() {
            if (!this.hasSampleMovie()) {
                return "サンプル動画はありません";
            }
            String mp4Url = this.getSampleMovieMp4Url();
            if (mp4Url != null) {
                String result = "<video controls width=\"720\" height=\"480\" poster=\"{value1}\"><source src=\"{value2}\" type=\"video/mp4\">お使いのブラウザは動画タグに対応していません。</video>";
                return result.replace("{value1}", this.getSampleImageURL().getSample_l().getImage().get(0)).replace("{value2}", mp4Url);
            } else if (this.sampleMovieURL.getMaxSizeMovie() != null) {
                String result = "<div class=\"video-container\"><iframe style=\"display: block; overflow: hidden;\" title=\"サンプル動画\" src=\"{value}\" width=\"720\" height=\"480\" frameborder=\"0\" scrolling=\"no\" allowfullscreen=\"allowfullscreen\"></iframe></div>";
                return result.replace("{value}", this.sampleMovieURL.getMaxSizeMovie());
            } else {
                return "サンプル動画はありません";
            }
        }

        /**
         * 商品IDからサンプル動画のmp4ファイルのURLを生成し返却する.
         *
         * @return サンプル動画のmp4ファイル
         */
        @JsonIgnore
        public String getSampleMovieMp4Url() {
            String originalProductId = this.product_id;
            String url = String.format("https://cc3001.dmm.co.jp/litevideo/freepv/%s/%s/%s/%shhb.mp4",
                    this.product_id.substring(0, 1),
                    this.product_id.substring(0, 3),
                    this.product_id,
                    this.product_id);

            int cnt = 0;
            while (true) {
                try {
                    // URLにHEADアクセスしてステータス確認
                    URL u = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                    conn.setRequestMethod("HEAD");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    int responseCode = conn.getResponseCode();
                    conn.disconnect();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        return url;
                    }

                    // URL変更ロジック
                    if (cnt == 0) {
                        this.product_id = originalProductId.replace("000", "0");
                        url = String.format("https://cc3001.dmm.co.jp/litevideo/freepv/%s/%s/%s/%shhb.mp4",
                                this.product_id.substring(0, 1),
                                this.product_id.substring(0, 3),
                                this.product_id,
                                this.product_id);
                    } else if (cnt == 1) {
                        url = String.format("https://cc3001.dmm.co.jp/litevideo/freepv/%s/%s/%s/%s_mhb_w.mp4",
                                this.product_id.substring(0, 1),
                                this.product_id.substring(0, 3),
                                this.product_id,
                                this.product_id);
                    } else {
                        log.error("サンプル動画のURL取得に失敗しました。");
                        return null;
                    }
                    cnt++;
                } catch (IOException e) {
                    log.error("通信エラー: " + e.getMessage());
                    return null;
                }
            }
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Review {
        /** レビュー数 */
        @JsonProperty("count")
        private int count;

        /** 平均評価 */
        @JsonProperty("average")
        private String average = "";
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageURL {
        /** リスト用画像URL */
        @JsonProperty("list")
        private String list = "";

        /** 小画像URL */
        @JsonProperty("small")
        private String small = "";

        /** 大画像URL */
        @JsonProperty("large")
        private String large = "";
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SampleImageURL {
        /** 小サイズサンプル画像群 */
        @JsonProperty("sample_s")
        private Sample sample_s = new Sample();

        /** 大サイズサンプル画像群 */
        @JsonProperty("sample_l")
        private Sample sample_l = new Sample();

        @JsonIgnore
        public boolean hasImage() {
            return this.sample_s.size() > 0 || this.sample_l.size() > 0;
        }

        @JsonIgnore
        public List<String> getSamples() {
            return this.sample_l.size() > 0 ? this.sample_l.getImage() : this.sample_s.getImage();
        }

        @JsonIgnore
        public boolean is同人Comic() {
            boolean result = !getSamples().isEmpty() &&
                   getSamples().get(0).split("/").length > 4 &&
                   "comic".equals(getSamples().get(0).split("/")[4]);
            return result;
        }

        @JsonIgnore
        public boolean is同人Game() {
            return !getSamples().isEmpty() &&
                    getSamples().get(0).split("/").length > 4 &&
                    "game".equals(getSamples().get(0).split("/")[4]);
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sample {
        /** サンプル画像URL一覧 */
        @JsonProperty("image")
        private List<String> image = new ArrayList<String>();

        @JsonIgnore
        public int size() {
            return this.image.size();
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SampleMovieURL {
        @JsonProperty("size_476_306")
        private String size_476_306 = "";

        @JsonProperty("size_560_360")
        private String size_560_360 = "";

        @JsonProperty("size_644_414")
        private String size_644_414 = "";

        @JsonProperty("size_720_480")
        private String size_720_480 = "";

        @JsonProperty("pc_flag")
        private int pc_flag;

        @JsonProperty("sp_flag")
        private int sp_flag;

        @JsonIgnore
        public boolean hasMovie() {
            return (size_476_306 != null && !size_476_306.isEmpty()) ||
                    (size_560_360 != null && !size_560_360.isEmpty()) ||
                    (size_644_414 != null && !size_644_414.isEmpty()) ||
                    (size_720_480 != null && !size_720_480.isEmpty());
        }

        @JsonIgnore
        public String getMaxSizeMovie() {
            if (size_720_480 != null && !size_720_480.isEmpty()) return size_720_480;
            if (size_644_414 != null && !size_644_414.isEmpty()) return size_644_414;
            if (size_560_360 != null && !size_560_360.isEmpty()) return size_560_360;
            if (size_476_306 != null && !size_476_306.isEmpty()) return size_476_306;
            return "";
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemInfo {
        @JsonProperty("genre")
        private List<Genre> genre = new ArrayList<Genre>();

        @JsonProperty("maker")
        private List<Maker> maker = new ArrayList<Maker>();

        @JsonProperty("actress")
        private List<Actress> actress = new ArrayList<Actress>();

        @JsonProperty("director")
        private List<Director> director = new ArrayList<Director>();
 
        @JsonProperty("label")
        private List<Label> label = new ArrayList<Label>();

        @JsonProperty("series")
        private List<Series> series = new ArrayList<Series>();

        /**
         * 女優とジャンルの文字列リストを取得する.
         *
         * @return 女優名とジャンル名の文字列リスト.
         */
        @JsonIgnore
        public List<String> getActressAndGenreList() {
            return Stream.concat(
                    actress.stream().map(Actress::getName),
                    genre.stream().map(Genre::getName)
                )
                .filter(Objects::nonNull)
                .filter(name -> !name.isBlank())
                .collect(Collectors.toList());
        }

        /**
         * 出演者をカンマ区切りで取得します.
         *
         * @return カンマ区切りのキーワード
         */
        @JsonIgnore
        public String getActressWords() {
            return this.actress != null ? this.actress.stream().map(Actress::getName).collect(Collectors.joining(",")) : "";
        }

        /**
         * 作品を表すキーワードになる単語をカンマ区切りで取得します.
         *
         * @return カンマ区切りのキーワード
         */
        @JsonIgnore
        public String getKeywords() {
            String genreWords = this.genre != null ? this.genre.stream().map(Genre::getName).collect(Collectors.joining(",")) : "";
            String actressWords = this.actress != null ? this.actress.stream().map(Actress::getName).collect(Collectors.joining(",")) : "";
            String seriesWords = this.series != null ? this.series.stream().map(Series::getName).collect(Collectors.joining(",")) : "";
            String result = genreWords + "," +  actressWords + "," + seriesWords;
            for (final String exclusionKeyword : exclusionKeyWordList) {
                result = result.replace(exclusionKeyword, "");
            }
            return result;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Campaign {
        @JsonProperty("title")
        private String title = "";

        @JsonProperty("id")
        private int id;

        @JsonProperty("name")
        private String name = "";

        @JsonProperty("description")
        private String description = "";

        @JsonProperty("date_begin")
        private String dateBegin = "";

        @JsonProperty("date_end")
        private String dateEnd = "";
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Genre {
        @JsonProperty("id")
        private int id;

        @JsonProperty("name")
        private String name = "";
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Maker {
        @JsonProperty("id")
        private int id;

        @JsonProperty("name")
        private String name = "";
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Actress {
        @JsonProperty("id")
        private int id;

        @JsonProperty("name")
        private String name = "";

        @JsonProperty("ruby")
        private String ruby = "";
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Director {
        @JsonProperty("id")
        private int id;

        @JsonProperty("name")
        private String name = "";

        @JsonProperty("ruby")
        private String ruby = "";
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Label {
        @JsonProperty("id")
        private int id;

        @JsonProperty("name")
        private String name = "";
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Series {
        @JsonProperty("id")
        private int id;

        @JsonProperty("name")
        private String name = "";
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Prices {
        @JsonProperty("price")
        private String price = "";

        @JsonProperty("deliveries")
        private Deliveries deliveries = new Deliveries();

        @JsonProperty("list_price")
        private String listPrice = "";
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Deliveries {
        @JsonProperty("delivery")
        private List<Delivery> delivery = new ArrayList<Delivery>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Delivery {
        @JsonProperty("type")
        private String type = "";

        @JsonProperty("price")
        private String price = "";

        @JsonProperty("list_price")
        private String listPrice = "";
    }
}