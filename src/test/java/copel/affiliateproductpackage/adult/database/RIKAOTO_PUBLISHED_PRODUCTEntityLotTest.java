package copel.affiliateproductpackage.adult.database;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RIKAOTO_PUBLISHED_PRODUCTEntityLotTest {
    @BeforeEach
    void before() {
        RIKAOTO_PUBLISHED_PRODUCTEntity entity = new RIKAOTO_PUBLISHED_PRODUCTEntity();
        entity.setMediaId("test_media.com");
        entity.setProductId("test_product_1");
        entity.save();
        entity = new RIKAOTO_PUBLISHED_PRODUCTEntity();
        entity.setMediaId("test_media.com");
        entity.setProductId("test_product_2");
        entity.save();
    }

    @AfterEach
    void after() {
        RIKAOTO_PUBLISHED_PRODUCTEntity entity = new RIKAOTO_PUBLISHED_PRODUCTEntity();
        entity.setMediaId("test_media.com");
        entity.setProductId("test_product_1");
        entity.delete();
        entity = new RIKAOTO_PUBLISHED_PRODUCTEntity();
        entity.setMediaId("test_media.com");
        entity.setProductId("test_product_2");
        entity.delete();
    }

    @Test
    void fetchByPkTest() {
        RIKAOTO_PUBLISHED_PRODUCTEntityLot entityLot = new RIKAOTO_PUBLISHED_PRODUCTEntityLot();
        entityLot.fetchByPk("test_media.com");
        System.out.println(entityLot);
    }

    @Test
    void existTest() {
        RIKAOTO_PUBLISHED_PRODUCTEntityLot entityLot = new RIKAOTO_PUBLISHED_PRODUCTEntityLot();
        entityLot.fetchByPk("test_media.com");
        assertTrue(entityLot.exist("test_media.com", "test_product_2"));
        assertFalse(entityLot.exist("test_media.com", "test_product_3"));
    }
}
