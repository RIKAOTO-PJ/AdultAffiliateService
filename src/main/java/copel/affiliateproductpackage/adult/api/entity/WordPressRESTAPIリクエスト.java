package copel.affiliateproductpackage.adult.api.entity;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WordPressRESTAPIリクエスト {
    @JsonProperty("date")
    private String date;

    @JsonProperty("date_gmt")
    private String dateGmt;

    @JsonProperty("slug")
    private String slug;

    @JsonProperty("status")
    private String status;

    @JsonProperty("title")
    private Object title;

    @JsonProperty("content")
    private Object content;

    @JsonProperty("author")
    private Integer author;

    @JsonProperty("excerpt")
    private String excerpt;

    @JsonProperty("featured_media")
    private Integer featuredMedia;

    @JsonProperty("comment_status")
    private String commentStatus;

    @JsonProperty("ping_status")
    private String pingStatus;

    @JsonProperty("format")
    private String format;

    @JsonProperty("meta")
    private Map<String, Object> meta;

    @JsonProperty("sticky")
    private Boolean sticky;

    @JsonProperty("template")
    private String template;

    @JsonProperty("categories")
    private List<Integer> categories;

    @JsonProperty("tags")
    private List<Integer> tags;

    @JsonProperty("password")
    private String password;

    @JsonProperty("permalink_template")
    private String permalinkTemplate;

    @JsonProperty("blocks")
    private List<Object> blocks;

    @JsonProperty("menu_order")
    private Integer menuOrder;

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
