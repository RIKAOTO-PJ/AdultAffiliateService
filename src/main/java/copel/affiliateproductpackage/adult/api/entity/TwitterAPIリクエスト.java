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
public class TwitterAPIリクエスト {
    @JsonProperty("text")
    private String text = "";

    @JsonProperty("media")
    private Media media = new Media();

    @JsonProperty("reply")
    private Reply reply = new Reply();

    @JsonIgnore
    public void setReplyTweetId(final String tweetId) {
        this.reply = new Reply();
        this.reply.setIn_reply_to_tweet_id(tweetId);
    }

    @JsonIgnore
    public void addMedia(final String mediaId) {
        this.media.getMedia_ids().add(mediaId);
    }

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

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Media {
        @JsonProperty("media_ids")
        private List<String> media_ids = new ArrayList<String>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Reply {
        @JsonProperty("in_reply_to_tweet_id")
        private String in_reply_to_tweet_id = "";
    }
}
