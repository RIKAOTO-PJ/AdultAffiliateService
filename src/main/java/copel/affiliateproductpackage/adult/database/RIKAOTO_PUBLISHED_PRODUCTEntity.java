package copel.affiliateproductpackage.adult.database;

import java.time.Instant;

import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;


/**
 * 作成済み記事のプロダクトID情報を表すクラス。<br>
 * このクラスはDynamoDBテーブル「RIKAOTO_PUBLISHED_PRODUCT」とマッピングされており、<br>
 * インスタンス単位でCRUD（作成・取得・更新・削除）操作を提供します。
 */
@DynamoDbBean
public class RIKAOTO_PUBLISHED_PRODUCTEntity extends DynamoDB<RIKAOTO_PUBLISHED_PRODUCTEntity> {
    /**
     * メディアID（PK）.
     * ※ブログのIDやTwitterのIDなどメディアを一意に識別するもの
     */
    private String mediaId;

    /**
     * 掲載した商品ID.
     */
    private String productId;

    /**
     * コンストラクタ.
     */
    public RIKAOTO_PUBLISHED_PRODUCTEntity() {
        super("RIKAOTO_PUBLISHED_PRODUCT", RIKAOTO_PUBLISHED_PRODUCTEntity.class);
    }

    /**
     * メディアID（PK）を取得します。
     *
     * @return メディアID
     */
    @DynamoDbPartitionKey
    @DynamoDbAttribute("media_id")
    public String getMediaId() {
        return mediaId;
    }

    /**
     * メディアID（PK）を設定します。
     *
     * @param mediaId 設定するメディアID
     */
    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    /**
     * 表示名を取得します。
     *
     * @return 掲載した商品ID
     */
    @DynamoDbSortKey
    @DynamoDbAttribute("product_id")
    public String getProductId() {
        return productId;
    }

    /**
     * 表示名を設定します。
     *
     * @param productId 掲載した商品ID
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * このインスタンスの内容をDynamoDBに保存します。<br>
     * タイムスタンプは現在時刻に自動更新されます。
     */
    public void save() {
        this.timestamp = Instant.now().toString();
        this.table.putItem(this);
    }

    /**
     * このインスタンスのmediaIdに対応するレコードをDynamoDBから削除します。
     */
    public void delete() {
        this.table.deleteItem(Key.builder().partitionValue(this.mediaId).sortValue(this.productId).build());
    }

    /**
     * このインスタンスのmediaIdに基づき、DynamoDBから最新の情報を取得し、<br>
     * インスタンスのフィールドを上書きします。<br>
     * レコードが見つからない場合は何もしません。
     */
    public void fetch() {
        RIKAOTO_PUBLISHED_PRODUCTEntity latest = this.table.getItem(Key.builder()
                .partitionValue(this.mediaId)
                .sortValue(this.productId)
                .build());
        if (latest != null) {
            this.timestamp = latest.getTimestamp();
        }
    }
}
