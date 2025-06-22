package copel.affiliateproductpackage.adult.api.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TwitterAPIレスポンス {
    @JsonProperty("data")
    private Data data = new Data();

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("title")
    private String title = "";

    @JsonProperty("type")
    private String type = "";

    @JsonProperty("detail")
    private String detail = "";

    @JsonProperty("media_id_string")
    private String media_id_string = "";

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON変換失敗", e);
        }
    }

    /**
     * メディアIDを持つかどうか判定する.
     *
     * @return メディアIDを持てばtrue、それ以外はfalse.
     */
    @JsonIgnore
    public boolean hasMediaId() {
        return this.media_id_string != null && !this.media_id_string.isEmpty();
    }

    /**
     * つぶやいたツイートのIDを返却します.
     *
     * @return ツイートID
     */
    @JsonIgnore
    public String getTweetId() {
        return this.data.getEdit_history_tweet_ids().get(0);
    }

    /**
     * ステータス200番台であるかどうかを返却します.
     *
     * @return 200番台であればtrue、それ以外はfalse.
     */
    @JsonIgnore
    public boolean isOK() {
        return this.status != null ? (this.status / 100 == 2) : true;
    }

    /**
     * 失敗レスポンスの場合、失敗原因を文字列を返却します.
     *
     * @return 失敗原因.
     */
    @JsonIgnore
    public String getFailedReason() {
        if (this.isOK() || this.detail == null) {
            return null;
        }
        switch (this.detail) {
            case "Unauthorized" :
                return "認証に失敗しました。";
            case "You are not allowed to create a Tweet with duplicate content.":
                return "同じ内容のツイートを連続で投稿する事はできません。";
            case "Too Many Requests":
                return "時間内のリクエスト数制限に達しました。一定時間を空けた後に再度試してください。";
            case "One or more parameters to your request was invalid":
                return "1つ以上のパラメータが不正です";
            default:
                return this.detail;
        }
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        @JsonProperty("id")
        private String id = "";

        @JsonProperty("edit_history_tweet_ids")
        private List<String> edit_history_tweet_ids = new ArrayList<String>();

        @JsonProperty("text")
        private String text = "";

        @Override
        public String toString() {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            try {
                return objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON変換失敗", e);
            }
        }
    }
}
