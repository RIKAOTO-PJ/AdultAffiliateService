package copel.affiliateproductpackage.adult.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TwitterAPIリクエスト {
    @JsonProperty("text")
    private String text;
    @JsonProperty("media")
    private Media media;

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON変換失敗", e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Media {
        @JsonProperty("media_ids")
        private String[] media_ids;
    }
}
