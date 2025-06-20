package copel.affiliateproductpackage.adult.api;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;

import copel.affiliateproductpackage.adult.api.entity.TwitterAPIリクエスト;
import copel.affiliateproductpackage.adult.api.entity.TwitterAPIレスポンス;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TwitterAPI {
    /**
     * Twitter APIのエンドポイント.
     */
    private final static String TWEET_API_ENDPOINT = "https://api.twitter.com/2/tweets";
    /**
     * Twitter メディアアップロードエンドポイント.
     */
    private final static String MEDIA_UPLOAD_ENDPOINT = "https://upload.twitter.com/1.1/media/upload.json";

    /**
     * ツイートを投稿します
     *
     * @param request リクエスト内容
     * @param twitterApiKey TwitterのAPIキー
     * @param twitterApiSecret TwitterのAPIシークレット
     * @param twitterAccessToken Twitterのアクセストークン
     * @param twitterAccessSecret Twitterのアクセスシークレット
     * @return TwitterAPIレスポンス
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static TwitterAPIレスポンス post(final TwitterAPIリクエスト request,
            final String twitterApiKey, final String twitterApiSecret, final String twitterAccessToken, final String twitterAccessSecret)
            throws IOException, InterruptedException, ExecutionException {

        final OAuth10aService service = new ServiceBuilder(twitterApiKey)
                .apiSecret(twitterApiSecret)
                .build(TwitterApi.instance());

        final OAuth1AccessToken accessToken = new OAuth1AccessToken(twitterAccessToken, twitterAccessSecret);

        final OAuthRequest oauthRequest = new OAuthRequest(Verb.POST, TWEET_API_ENDPOINT);
        oauthRequest.addHeader("Content-Type", "application/json");
        oauthRequest.setPayload(request.toString());

        service.signRequest(accessToken, oauthRequest);

        try (Response response = service.execute(oauthRequest)) {
            ObjectMapper mapper = new ObjectMapper();
            TwitterAPIレスポンス TwitterAPIレスポンス = mapper.readValue(response.getBody(), TwitterAPIレスポンス.class);
            if (TwitterAPIレスポンス.isOK()) {
                log.info("ツイートしました. レスポンス: {}", TwitterAPIレスポンス);
            } else {
                log.error("ツイートに失敗しました. 原因: {}, レスポンス: {}", TwitterAPIレスポンス.getFailedReason(), TwitterAPIレスポンス);
            }
            return TwitterAPIレスポンス;
        }
    }

    /**
     * Twitterにメディア(動画や画像)をアップロードしそのIDを返却します.
     *
     * @param mediaData メディアのバイトデータ
     * @param mediaType メディアの種別
     * @param twitterApiKey TwitterのAPIキー
     * @param twitterApiSecret TwitterのAPIシークレット
     * @param twitterAccessToken Twitterのアクセストークン
     * @param twitterAccessSecret Twitterのアクセスシークレット
     * @return メディアID
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @SuppressWarnings("resource")
    public static String uploadMedia(final byte[] mediaData, final MediaType mediaType,
            final String twitterApiKey, final String twitterApiSecret, final String twitterAccessToken, final String twitterAccessSecret)
            throws IOException, InterruptedException, ExecutionException {

        OAuth10aService service = new ServiceBuilder(twitterApiKey)
                .apiSecret(twitterApiSecret)
                .build(TwitterApi.instance());
        OAuth1AccessToken accessToken = new OAuth1AccessToken(twitterAccessToken, twitterAccessSecret);

        ObjectMapper mapper = new ObjectMapper();

        // INIT
        OAuthRequest initRequest = new OAuthRequest(Verb.POST, MEDIA_UPLOAD_ENDPOINT);
        initRequest.addParameter("command", "INIT");
        initRequest.addParameter("media_type", mediaType.getMediaType());
        initRequest.addParameter("total_bytes", String.valueOf(mediaData.length));
        initRequest.addParameter("media_category", mediaType.getMediaCategory());

        service.signRequest(accessToken, initRequest);
        try (Response initResponse = service.execute(initRequest)) {
            if (!initResponse.isSuccessful()) {
                throw new IOException("INIT failed: " + initResponse.getBody());
            }

            JsonNode initJson = mapper.readTree(initResponse.getBody());
            String mediaId = initJson.get("media_id_string").asText();

            // APPEND（multipart/form-data を HttpURLConnection で送信）
            final int CHUNK_SIZE = 5 * 1024 * 1024;
            int segmentIndex = 0;
            for (int i = 0; i < mediaData.length; i += CHUNK_SIZE) {
                int end = Math.min(i + CHUNK_SIZE, mediaData.length);
                byte[] chunk = Arrays.copyOfRange(mediaData, i, end);

                String boundary = "----Boundary" + System.currentTimeMillis();
                String appendUrl = MEDIA_UPLOAD_ENDPOINT + "?command=APPEND&media_id=" + mediaId + "&segment_index=" + segmentIndex;

                URL url = new URL(appendUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                // OAuth署名生成（headerのみ利用）
                OAuthRequest dummy = new OAuthRequest(Verb.POST, appendUrl);
                service.signRequest(accessToken, dummy);
                conn.setRequestProperty("Authorization", dummy.getHeaders().get("Authorization"));

                try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
                    out.writeBytes("--" + boundary + "\r\n");
                    out.writeBytes("Content-Disposition: form-data; name=\"media\"; filename=\"" + mediaType.getTempFileName() + "\"\r\n");
                    out.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
                    out.write(chunk);
                    out.writeBytes("\r\n--" + boundary + "--\r\n");
                }

                int responseCode = conn.getResponseCode();
                if (responseCode != 204 && responseCode != 200) {
                    String error = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))
                            .lines().collect(Collectors.joining("\n"));
                    throw new IOException("APPEND failed (segment " + segmentIndex + "): " + error);
                }
                segmentIndex++;
            }

            // FINALIZE
            OAuthRequest finalizeRequest = new OAuthRequest(Verb.POST, MEDIA_UPLOAD_ENDPOINT);
            finalizeRequest.addParameter("command", "FINALIZE");
            finalizeRequest.addParameter("media_id", mediaId);

            service.signRequest(accessToken, finalizeRequest);
            try (Response finalizeResponse = service.execute(finalizeRequest)) {
                if (!finalizeResponse.isSuccessful()) {
                    throw new IOException("FINALIZE failed: " + finalizeResponse.getBody());
                }

                JsonNode finalJson = mapper.readTree(finalizeResponse.getBody());
                return finalJson.get("media_id_string").asText();
            }
        }
    }

    /**
     * メディアタイプ列挙型.
     */
    public static enum MediaType {
        Video("video/mp4", "tweet_video", "video.mp4"),
        Image("image/jpg", "tweet_image", "image.jpg"),
        Gif("image/gif", "tweet_gif", "image.gif"),;

        private String mediaType;
        private String mediaCategory;
        private String tempFileName;

        private MediaType(String mediaType, String mediaCategory, String tempFileName) {
            this.mediaType = mediaType;
            this.mediaCategory = mediaCategory;
            this.tempFileName = tempFileName;
        }

        public String getMediaType() {
            return mediaType;
        }
        public String getMediaCategory() {
            return mediaCategory;
        }
        public String getTempFileName() {
            return tempFileName;
        }
    }
}
