package copel.affiliateproductpackage.adult.api;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import copel.affiliateproductpackage.adult.api.entity.WordPressRESTAPIリクエスト;

class WordPressRESTAPITest {

    @Test
    void postTest() throws IOException, InterruptedException {
        WordPressRESTAPIリクエスト request = new WordPressRESTAPIリクエスト();
        request.setTitle("これはタイトルです！！");
        request.setContent("ああああ<br>ああああ\nああああ");
        request.setStatus("publish");
        WordPressRESTAPI.post(request, "dummy", "dummy");
    }

    @Test
    void uploadImageTest() {
        int mediaId = WordPressRESTAPI.uploadImage("h_1324skmj00518.jpg", "https://pics.dmm.co.jp/digital/video/h_1324skmj00518/h_1324skmj00518pl.jpg", "dummy", "dummy");
        assertTrue(mediaId > 0);
    }
}
