package copel.affiliateproductpackage.adult.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

public class RIKAOTO_PUBLISHED_PRODUCTEntityLot implements Iterable<RIKAOTO_PUBLISHED_PRODUCTEntity> {
    /**
     * JSON変換用 ObjectMapper.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Lotオブジェクト.
     */
    private Collection<RIKAOTO_PUBLISHED_PRODUCTEntity> entityLot;

    /**
     * コンストラクタ.
     */
    public RIKAOTO_PUBLISHED_PRODUCTEntityLot() {
        this.entityLot = new ArrayList<RIKAOTO_PUBLISHED_PRODUCTEntity>();
    }

    /**
     * PartitonKeyで絞り込み、検索結果をこのLotに持つ.
     */
    public void fetchByPk(final String partitionKey) {
        this.entityLot.clear();
        RIKAOTO_PUBLISHED_PRODUCTEntity dummy = new RIKAOTO_PUBLISHED_PRODUCTEntity();
        dummy.getTable()
             .query(QueryConditional.keyEqualTo(Key.builder().partitionValue(partitionKey).build()))
             .items()
             .forEach(this.entityLot::add);
    }

    /**
     * 指定したpartitionKeyとrowKeyを持つレコードがこのLotに存在するかどうかを返却する.
     *
     * @param partitionKey パーティションキー
     * @param rowKey ソートキー
     * @return 存在すればtrue、それ以外はfalse
     */
    public boolean exist(final String partitionKey, final String rowKey) {
        return entityLot.stream()
                .anyMatch(e -> partitionKey.equals(e.getMediaId()) && rowKey.equals(e.getProductId()));
    }

    @Override
    public Iterator<RIKAOTO_PUBLISHED_PRODUCTEntity> iterator() {
        return this.entityLot.iterator();
    }

    @Override
    public String toString() {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.entityLot);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
