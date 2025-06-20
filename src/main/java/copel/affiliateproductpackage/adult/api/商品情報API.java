package copel.affiliateproductpackage.adult.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import copel.affiliateproductpackage.adult.api.entity.商品情報APIリクエスト;
import copel.affiliateproductpackage.adult.api.entity.商品情報APIレスポンス;

/**
 * DMM.com の商品の情報を取得することが可能なAPI です.
 * リクエストURLは以下の形式です.
 * https://api.dmm.com/affiliate/v3/ItemList?api_id=[APIID]&affiliate_id=[アフィリエイトID]&site=FANZA&service=digital&floor=videoa&hits=10&sort=date&keyword=%e4%b8%8a%e5%8e%9f%e4%ba%9c%e8%a1%a3&output=json
 *
 * 商品情報APIは、DMM.comの商品情報を取得するAPIです。各種商品を検索し、取得することができます。
 * APIで取得可能な情報は、商品タイトル、出演者、商品画像、レビュー件数・平均、価格等です。
 * アフィリエイトIDを設定することで、アフィリエイトリンクの生成も可能です。
 * 取得する商品は指定したサイト、サービス、フロアにより異なります。
 * また、商品は指定したキーワードなどのパラメータにより検索することが可能です。
 *
 * @author 鈴木一矢
 */
public class 商品情報API {
    /**
     * 商品情報APIエンドポイント.
     */
    private static final String ENDPOINT = "https://api.dmm.com/affiliate/v3/ItemList";

    /**
     * 商品情報APIにGETリクエストを送信します.
     *
     * @param request リクエストエンティティ
     * @return レスポンスエンティティ
     */
    @SuppressWarnings("unchecked")
    public static 商品情報APIレスポンス get(final 商品情報APIリクエスト request) {
        try {
            // ObjectMapper を使ってリクエストをMap化
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            Map<String, Object> paramMap = mapper.convertValue(request, Map.class);

            // クエリ文字列に変換
            StringBuilder queryBuilder = new StringBuilder();
            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().toString().isEmpty()) {
                    queryBuilder.append("&")
                            .append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                            .append("=")
                            .append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
                }
            }

            // URL作成
            String fullUrl = ENDPOINT + "?" + queryBuilder.substring(1);
            URL url = new URL(fullUrl);

            // HTTP GET リクエスト
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // ステータスコード200以外はエラーハンドリング
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                String errorBody;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    errorBody = response.toString();
                } catch (Exception e) {
                    errorBody = "（エラーボディの取得に失敗）";
                }
                throw new IOException("HTTP error code: " + conn.getResponseCode() + "\nResponse body: " + errorBody);
            }

            // JSONレスポンスをオブジェクトに変換
            return mapper.readValue(conn.getInputStream(), 商品情報APIレスポンス.class);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("商品情報APIリクエストに失敗しました", e);
        }
    }
}
