package copel.affiliateproductpackage.adult.gpt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 【SES AIアシスタント】
 * Geminiクラス.
 *
 * @author 鈴木一矢.
 *
 */
@Slf4j
public class Gemini implements Transformer {
    /**
     * 生成APIのエンドポイント.
     */
    private static final String GEMINI_COMPLETION_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    /**
     * 生成APIのデフォルトGPTモデル名
     */
    private static final String COMPLETION_MODEL_DEFAULT = "gemini-1.5-flash";
    /**
     * APIキー.
     */
    private final String apiKey;
    /**
     * GPTモデル.
     */
    private final String completionModel;

    /**
     * コンストラクタ.
     *
     * @param apiKey APIキー
     */
    public Gemini(final String apiKey) {
        this.apiKey = apiKey;
        this.completionModel = COMPLETION_MODEL_DEFAULT;
    }
    /**
     * コンストラクタ.
     *
     * @param apiKey APIキー
     * @param completionModel GPTモデル
     */
    public Gemini(final String apiKey, final String completionModel) {
        this.apiKey = apiKey;
        this.completionModel = completionModel;
    }

    @Override
    public float[] embedding(final String inputString) throws IOException, RuntimeException {
        return null;
    }

    @Override
    public GptAnswer generate(final String prompt) throws IOException, RuntimeException {
        ObjectMapper objectMapper = new ObjectMapper();

        // リクエストボディの作成
        String requestBody = String.format(
                "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}",
                prompt
            );

        // HTTPリクエストの準備
        URL url = new URL(GEMINI_COMPLETION_API_URL + this.completionModel + ":generateContent?key=" + this.apiKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        // リクエストボディを送信
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // レスポンスの取得
        int responseCode = connection.getResponseCode();
        switch (responseCode) {
            case HttpURLConnection.HTTP_OK:
                break;
            case HttpURLConnection.HTTP_BAD_REQUEST:
                connection.disconnect();
                throw new RuntimeException("400 Bad Request: 無効なパラメータ、不適切なリクエストフォーマット、支払い上限超過エラー");
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                connection.disconnect();
                throw new RuntimeException("401 Unauthorized: APIキーが無効、または提供されていないエラー");
            case HttpURLConnection.HTTP_FORBIDDEN:
                connection.disconnect();
                throw new RuntimeException("403 Forbidden: アカウントの制限、または対象モデルが利用不可のエラー");
            case HttpURLConnection.HTTP_NOT_FOUND:
                connection.disconnect();
                throw new RuntimeException("404 Not Found: APIのエンドポイントが間違っている、またはモデル名が無効のエラー");
            case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
                connection.disconnect();
                throw new RuntimeException("408 Request Timeout: リクエストが時間内に処理されなかったエラー");
            case 429:
                connection.disconnect();
                throw new RuntimeException("429 Too Many Requests: クレジット不足、短時間に過剰なリクエストを送信したためエラーが発生しました");
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                connection.disconnect();
                throw new RuntimeException("500 Internal Server Error: OpenAIのサーバーで問題が発生しました");
            case HttpURLConnection.HTTP_UNAVAILABLE:
                connection.disconnect();
                throw new RuntimeException("503 Service Unavailable: OpenAIのサーバーがメンテナンス中、または負荷が高い状態です");
            default:
                break;
        }

         // JSONレスポンスの解析
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            // JSONをJacksonで解析
            JsonNode jsonResponse = objectMapper.readTree(response.toString());
            log.debug("Gemini生成のレスポンス: {}", response);

            // "candidates" 配列から "content" -> "parts" -> "text" を取得
            JsonNode candidates = jsonResponse.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    String resultText = parts.get(0).path("text").asText();
                    return new GptAnswer(resultText, Gemini.class);
                }
            }

            return new GptAnswer("", Gemini.class);
        }
    }

}
