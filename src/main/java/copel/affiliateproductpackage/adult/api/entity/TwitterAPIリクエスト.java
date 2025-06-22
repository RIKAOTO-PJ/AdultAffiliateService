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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TwitterAPIリクエスト {
    @JsonProperty("text")
    private String text = "";

    @JsonProperty("media")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Media media = null;

    @JsonProperty("reply")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Reply reply = null;

    @JsonIgnore
    public void setReplyTweetId(final String tweetId) {
        if (tweetId != null && !tweetId.isEmpty()) {
            this.reply = new Reply();
            this.reply.setIn_reply_to_tweet_id(tweetId);
        } else {
            this.reply = null;
        }
    }

    @JsonIgnore
    public void addMedia(final String mediaId) {
        if (mediaId != null && !mediaId.isEmpty()) 
            (media = media == null ? new Media() : media).getMedia_ids().add(mediaId);
    }

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON変換失敗", e);
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private static class Media {
        @JsonProperty("media_ids")
        private List<String> media_ids = new ArrayList<String>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private static class Reply {
        @JsonProperty("in_reply_to_tweet_id")
        private String in_reply_to_tweet_id = "";
    }
}
