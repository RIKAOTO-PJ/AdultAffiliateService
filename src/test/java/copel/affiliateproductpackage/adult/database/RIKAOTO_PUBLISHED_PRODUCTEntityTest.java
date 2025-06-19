package copel.affiliateproductpackage.adult.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RIKAOTO_PUBLISHED_PRODUCTEntityTest {
    @BeforeEach
    void before() {
    }

    @AfterEach
    void after() {
        RIKAOTO_PUBLISHED_PRODUCTEntity entity = new RIKAOTO_PUBLISHED_PRODUCTEntity();
        entity.setMediaId("test_media.com");
        entity.setProductId("test_product_1");
        entity.delete();
    }

    @Test
    void saveTest() {
        RIKAOTO_PUBLISHED_PRODUCTEntity entity = new RIKAOTO_PUBLISHED_PRODUCTEntity();
        entity.setMediaId("test_media.com");
        entity.setProductId("test_product_1");
        entity.save();
        System.out.println(entity);
    }
}
