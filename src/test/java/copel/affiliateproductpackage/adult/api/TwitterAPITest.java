package copel.affiliateproductpackage.adult.api;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import copel.affiliateproductpackage.adult.api.TwitterAPI.MediaType;
import copel.affiliateproductpackage.adult.api.entity.TwitterAPIリクエスト;
import copel.affiliateproductpackage.adult.api.entity.TwitterAPIレスポンス;
import copel.affiliateproductpackage.adult.unit.Video;

class TwitterAPITest {

    @Test
    void tweetTest() throws IOException, InterruptedException, ExecutionException {
        TwitterAPIリクエスト request = new TwitterAPIリクエスト();
        request.setText("test");
        TwitterAPI.post(request, "dummy", "dummy", "dummy", "dummy");
    }

    @Test
    void replyTest() throws IOException, InterruptedException, ExecutionException {
        TwitterAPIリクエスト request = new TwitterAPIリクエスト();
        request.setText("テストツイート2");
        TwitterAPIレスポンス response = TwitterAPI.post(request, "dummy", "dummy", "dummy", "dummy");
        request.setText("テストリプライ");
        request.setReplyTweetId(response.getData().getEdit_history_tweet_ids().get(0));
        TwitterAPI.post(request, "dummy", "dummy", "dummy", "dummy");
    }

    @Test
    void uploadMediaTest() throws IOException, InterruptedException, ExecutionException {
        Video video = new Video();
        assertTrue(video.downloadAndRead("https://cc3001.dmm.co.jp/litevideo/freepv/h/h_4/h_402mjad379/h_402mjad379mhb.mp4"));
        assertNotNull(video.getContent());
        String mediaId = TwitterAPI.uploadMedia(video.getContent(), MediaType.Video, "dummy", "dummy", "dummy", "dummy");
        assertNotNull(mediaId);
    }

    @Test
    void tweetWithMediaTest() throws IOException, InterruptedException, ExecutionException {
        Video video = new Video();
        assertTrue(video.downloadAndRead("https://cc3001.dmm.co.jp/litevideo/freepv/h/h_4/h_402mjad379/h_402mjad379mhb.mp4"));
        assertNotNull(video.getContent());
        String mediaId = TwitterAPI.uploadMedia(video.getContent(), MediaType.Video, "dummy", "dummy", "dummy", "dummy");
        assertNotNull(mediaId);
        TwitterAPIリクエスト request = new TwitterAPIリクエスト();
        request.setText("ここに文章");
        request.addMedia(mediaId);
        TwitterAPI.post(request, "dummy", "dummy", "dummy", "dummy");
    }
}
