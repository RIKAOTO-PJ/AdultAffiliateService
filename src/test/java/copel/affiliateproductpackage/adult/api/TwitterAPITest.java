package copel.affiliateproductpackage.adult.api;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;

import copel.affiliateproductpackage.adult.api.entity.TwitterAPIリクエスト;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
class TwitterAPITest {

    @SystemStub
    private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @BeforeEach
    void before() {
        MockitoAnnotations.openMocks(this);
        environmentVariables.set("TWITTER_API_KEY", "dummy");
        environmentVariables.set("TWITTER_API_SECRET", "dummy");
        environmentVariables.set("TWITTER_ACCESS_TOKEN", "dummy");
        environmentVariables.set("TWITTER_ACCESS_SECRET", "dummy");
    }

    @Test
    void test() throws IOException, InterruptedException, ExecutionException {
        TwitterAPIリクエスト request = new TwitterAPIリクエスト();
        request.setText("テストツイートです");
        System.out.println(request.toString());
        TwitterAPI.post(request);
    }
}
