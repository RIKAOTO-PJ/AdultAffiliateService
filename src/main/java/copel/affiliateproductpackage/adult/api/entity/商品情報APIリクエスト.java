package copel.affiliateproductpackage.adult.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class 商品情報APIリクエスト {
    /**
     * 【必須項目】
     * APIID
     * 登録時に割り振られたID
     */
    @JsonProperty("api_id")
    private String apiId;

    /**
     * 【必須項目】
     * アフィリエイトID
     * 登録時に割り振られた990～999までのアフィリエイトID
     */
    @JsonProperty("affiliate_id")
    private String affiliateId;

    /**
     * 【必須項目】
     * サイト
     * 一般（DMM.com）かアダルト（FANZA）か
     */
    @JsonProperty("site")
    private String site;

    /**
     * サービス
     * フロアAPIから取得できるサービスコードを指定
     */
    @JsonProperty("service")
    private String service;

    /**
     * フロア
     * フロアAPIから取得できるフロアコードを指定
     */
    @JsonProperty("floor")
    private String floor;

    /**
     * 取得件数
     * 初期値：20　最大：100
     */
    @JsonProperty("hits")
    private String hits = "100";

    /**
     * 検索開始位置
     * 初期値：1　最大：50000
     */
    @JsonProperty("offset")
    private String offset = "1";

    /**
     * ソート順
     * 初期値：rank
     * 人気：rank
     * 価格が高い順：price
     * 価格が安い順：-price
     * 発売日：date
     * 評価：review
     * マッチング順：match
     */
    @JsonProperty("sort")
    private String sort;

    /**
     * キーワード
     * UTF-8で指定
     * キーワード検索のヒント
     */
    @JsonProperty("keyword")
    private String keyword;

    /**
     * 商品ID
     * 商品に振られているcontent_id
     */
    @JsonProperty("cid")
    private String cid;

    /**
     * 絞りこみ項目
     * 女優：actress
     * 作者：author
     * ジャンル：genre
     * シリーズ：series
     * メーカー：maker
     * 絞り込み項目を複数設定する場合、パラメータ名を配列化します。
     * 例：&article[0]=genre&article[1]=actress
     */
    @JsonProperty("article")
    private String article;

    /**
     * 絞り込みID
     * 上記絞り込み項目のID(各検索APIから取得可能)
     * 絞り込み項目を複数設定する場合、パラメータ名を配列化します。
     * 例：&article_id[0]=111111&article_id[1]=222222
     */
    @JsonProperty("article_id")
    private String articleId;

    /**
     * 発売日絞り込み
     * このパラメータで指定した日付以降に発売された商品を絞り込むことができます。
     * ISO8601形式でフォーマットした日付を指定してください。(ただし、タイムゾーンは指定できません)
     */
    @JsonProperty("gte_date")
    private String gteDate;

    /**
     * 発売日絞り込み
     * このパラメータで指定した日付以前に発売された商品を絞り込むことができます。
     * フォーマットはgte_dateと同じです。
     */
    @JsonProperty("lte_date")
    private String lteDate;

    /**
     * 在庫絞り込み
     * 初期値：絞り込みなし
     * 在庫あり：stock
     * 予約商品（在庫あり）：reserve
     * 予約商品（キャンセル待ち）：reserve_empty
     * DMM通販のみ：mono
     * マーケットプレイスのみ：dmp
     * ※通販サービスのみ指定可能
     */
    @JsonProperty("mono_stock")
    private String monoStock;

    /**
     * 出力形式
     * json / xml
     */
    @JsonProperty("output")
    private String output;

    /**
     * コールバック
     * 出力形式jsonで指定した場合に、このパラメータでコールバック関数名を指定すると、JSONP形式で出力されます
     */
    @JsonProperty("callback")
    private String callback;

    /**
     * コンストラクタ.
     *
     * @param apiId APIID
     * @param affiliateId アフィリエイトID
     * @param floor フロアコード
     */
    public 商品情報APIリクエスト(final String apiId, final String affiliateId, final フロア floor) {
        this.apiId = apiId;
        this.affiliateId = affiliateId;
        this.site = floor.getSite();
        this.service = floor.getService();
        this.floor = floor.getFloor();
    }

    /**
     * offsetを次のOffsetページに更新します.
     */
    @JsonIgnore
    public void nextOffset() {
        if (this.offset == null || this.offset.isBlank()) {
            throw new IllegalArgumentException("空またはnullのためoffsetを増やせません");
        }
        try {
            int numOffset = Integer.parseInt(this.offset);
            int numHits = Integer.parseInt(this.hits);
            this.offset = String.valueOf(numOffset + numHits);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("数値に変換できない文字列がセットされているため、offsetを増やせませんでした。");
        }
    }

    public static enum フロア {
        FANZA_動画_ビデオ("FANZA", "digital", "videoa"),
        FANZA_動画_素人("FANZA", "digital", "videoc"),
        FANZA_動画_成人映画("FANZA", "digital", "nikkatsu"),
        FANZA_動画_アニメ動画("FANZA", "digital", "anime"),
        FANZA_月額動画_見放題chデラックス("FANZA", "monthly", "premium"),
        FANZA_月額動画_VRch("FANZA", "monthly", "vr"),
        FANZA_月額動画_見放題ch("FANZA", "monthly", "standard"),
        FANZA_通販_DVD("FANZA", "mono", "dvd"),
        FANZA_通販_大人のおもちゃ("FANZA", "mono", "goods"),
        FANZA_通販_アニメ("FANZA", "mono", "anime"),
        FANZA_通販_PCゲーム("FANZA", "mono", "pcgame"),
        FANZA_通販_ブック("FANZA", "mono", "book"),
        FANZA_アダルトPCゲーム_アダルトPCゲーム("FANZA", "pcgame", "digital_pcgame"),
        FANZA_同人_同人("FANZA", "doujin", "digital_doujin"),
        FANZA_FANZAブックス_コミック("FANZA", "ebook", "comic"),
        FANZA_FANZAブックス_美少女ノベル("FANZA", "ebook", "novel"),
        FANZA_FANZAブックス_アダルト写真集("FANZA", "ebook", "photo");

        private String site;
        private String service;
        private String floor;

        private フロア(String site, String service, String floor) {
            this.site = site;
            this.service = service;
            this.floor = floor;
        }

        public String getSite() {
            return site;
        }
        public String getService() {
            return service;
        }
        public String getFloor() {
            return floor;
        }
    }
}
