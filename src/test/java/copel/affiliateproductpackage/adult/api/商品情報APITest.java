package copel.affiliateproductpackage.adult.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import copel.affiliateproductpackage.adult.api.entity.商品情報APIリクエスト;
import copel.affiliateproductpackage.adult.api.entity.商品情報APIリクエスト.フロア;
import copel.affiliateproductpackage.adult.api.entity.商品情報APIレスポンス;
import copel.affiliateproductpackage.adult.api.entity.商品情報APIレスポンス.Item;

class 商品情報APITest {
    private static final String FANZA_API_ID = "Dpr7L2c6QsPEUfDndAYD";
    private static final String FANZA_AFFILIATE_ID = "rikaoto-999";
    private static final String DATETIME_ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    @Test
    void test() throws JsonProcessingException {
        LocalDateTime today = LocalDate.now().atStartOfDay();
        商品情報APIリクエスト request = new 商品情報APIリクエスト(FANZA_API_ID, FANZA_AFFILIATE_ID, フロア.FANZA_同人_同人);
        request.setKeyword("");
        request.setSort("review");
        request.setGteDate(today.minusYears(3).format(DateTimeFormatter.ofPattern(DATETIME_ISO_PATTERN)));
        request.setLteDate(today.minusDays(1).format(DateTimeFormatter.ofPattern(DATETIME_ISO_PATTERN)));
        商品情報APIレスポンス response = 商品情報API.get(request);
        for (Item item : response.getItems()) {
            System.out.println(item.getTitle());
            for (String sample : item.getSampleImageURL().getSample_l().getImage()) {
                System.out.println(sample);
            }
        }
    }
}
