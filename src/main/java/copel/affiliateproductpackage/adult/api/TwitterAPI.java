package copel.affiliateproductpackage.adult.api;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;

import copel.affiliateproductpackage.adult.api.entity.TwitterAPIリクエスト;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TwitterAPI {
    private final static String TWITTER_API_KEY = System.getenv("TWITTER_API_KEY");
    private final static String TWITTER_API_SECRET = System.getenv("TWITTER_API_SECRET");
    private final static String TWITTER_ACCESS_TOKEN = System.getenv("TWITTER_ACCESS_TOKEN");
    private final static String TWITTER_ACCESS_SECRET = System.getenv("TWITTER_ACCESS_SECRET");

    private final static String TWEET_API_ENDPOINT = "https://api.twitter.com/2/tweets";

    /**
     * 文章をツイートする.
     *
     * @param request リクエストオブジェクト
     * @throws ExecutionException 
     * @throws InterruptedException 
     * @throws IOException 
     */
    public static void post(final TwitterAPIリクエスト request) throws IOException, InterruptedException, ExecutionException {
        final OAuth10aService service = new ServiceBuilder(TWITTER_API_KEY)
                .apiSecret(TWITTER_API_SECRET)
                .build(TwitterApi.instance());

        final OAuth1AccessToken accessToken = new OAuth1AccessToken(TWITTER_ACCESS_TOKEN, TWITTER_ACCESS_SECRET);

        final OAuthRequest OAuthRequest = new OAuthRequest(Verb.POST, TWEET_API_ENDPOINT);
        OAuthRequest.addHeader("Content-Type", "application/json");
        OAuthRequest.setPayload(request.toString());

        service.signRequest(accessToken, OAuthRequest);

        try (Response response = service.execute(OAuthRequest)) {
            if (response.getCode() != 200) {
                log.error("Status code: " + response.getCode());
                log.error("Response body: " + response.getBody());
            } else {
                log.info("ツイートに成功しました。");
            }
        }
    }
}
