package copel.affiliateproductpackage.adult.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import copel.affiliateproductpackage.adult.api.entity.商品情報APIリクエスト;
import copel.affiliateproductpackage.adult.api.entity.商品情報APIレスポンス;

class 商品情報APITest {
    private static final String DATETIME_ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    @Test
    void test() throws JsonProcessingException {
        LocalDateTime today = LocalDate.now().atStartOfDay();
        商品情報APIリクエスト request = new 商品情報APIリクエスト("dummy", "dummy");
        request.setKeyword("");
        request.setSort("review");
        request.setGteDate(today.minusYears(3).format(DateTimeFormatter.ofPattern(DATETIME_ISO_PATTERN)));
        request.setLteDate(today.minusDays(1).format(DateTimeFormatter.ofPattern(DATETIME_ISO_PATTERN)));
        request.setService("digital");
        request.setFloor("videoa");
        商品情報APIレスポンス response = 商品情報API.get(request);
        System.out.println(response);
    }
}
