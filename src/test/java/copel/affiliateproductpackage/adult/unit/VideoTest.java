package copel.affiliateproductpackage.adult.unit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class VideoTest {

    @Test
    void test() {
        Video video = new Video();
        assertTrue(video.downloadAndRead("https://cc3001.dmm.co.jp/litevideo/freepv/c/chr/chrv00203/chrv00203mhb.mp4"));
        assertTrue(video.save("src/main/resources/download/chrv00203mhb.mp4"));
        assertTrue(video.cut(10,20));
        assertTrue(video.save("src/main/resources/download/after.mp4"));
    }
}
