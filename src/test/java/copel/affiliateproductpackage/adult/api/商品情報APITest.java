package copel.affiliateproductpackage.adult.api;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import copel.affiliateproductpackage.adult.api.entity.商品情報APIリクエスト;
import copel.affiliateproductpackage.adult.api.entity.商品情報APIレスポンス;

class 商品情報APITest {
    @Test
    void test() throws JsonProcessingException {
        商品情報APIリクエスト request = new 商品情報APIリクエスト("dummy", "dummy");
        商品情報APIレスポンス response = 商品情報API.get(request);
        System.out.println(response);
    }
}
