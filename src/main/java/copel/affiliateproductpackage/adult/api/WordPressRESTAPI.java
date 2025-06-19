package copel.affiliateproductpackage.adult.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import copel.affiliateproductpackage.adult.api.entity.WordPressRESTAPIリクエスト;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WordPressRESTAPI {
    /**
     * エンドポイント.
     */
    private static final String POST_ENDPOINT = "https://adult.mongolian.jp/wp-json/wp/v2/posts";
    private static final String MEDIA_ENDPOINT = "https://adult.mongolian.jp/wp-json/wp/v2/media";
    private static final String CATEGORY_ENDPOINT = "https://adult.mongolian.jp/wp-json/wp/v2/categories";
    private static final String TAG_ENDPOINT = "https://adult.mongolian.jp/wp-json/wp/v2/tags";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * WordPressへ投稿する.
     */
    public static int post(final WordPressRESTAPIリクエスト request, final String userName, final String applicationPassword) throws IOException, InterruptedException {
        // アプリケーションパスワードを使ったBasic認証
        String auth = Base64.getEncoder().encodeToString((userName + ":" + applicationPassword).getBytes());

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(POST_ENDPOINT))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(request.toString()))
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode()/100 != 2) {
            log.error("Status: {}", response.statusCode());
            log.error("Body: {}", response.body());
        } else {
            log.info("記事のアップロードに成功しました. response: {}", response.body());
        }

        return response.statusCode();
    }

    /**
     * WordPressへ画像をアップロードしてそのメディアIDを取得する.
     *
     * @param fileName ファイル名
     * @param imageUrl アップロードしたいファイルのURL
     * @return メディアID.
     */
    public static int uploadImage(final String fileName, final String imageUrl, final String userName, final String applicationPassword) {
        String auth = Base64.getEncoder().encodeToString((userName + ":" + applicationPassword).getBytes());

        try (InputStream imageStream = new URL(imageUrl).openStream()) {
            // InputStream を byte 配列に読み込む
            byte[] imageBytes = imageStream.readAllBytes();

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MEDIA_ENDPOINT))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "image/jpeg")
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .POST(BodyPublishers.ofByteArray(imageBytes))
                    .build();

            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                log.error("画像アップロード失敗: status={}, body={}", response.statusCode(), response.body());
                return -1;
            } else {
                log.info("画像アップロード成功: {}", response.body());
                JsonNode root = objectMapper.readTree(response.body());
                return root.get("id").asInt();
            }
        } catch (Exception e) {
            log.error("画像アップロード処理でエラー", e);
            return -1;
        }
    }

    /**
     * 指定したメディアIDの画像をWordPressから削除します.
     *
     * @param mediaId メディアID
     * @return 成功すればtrue、失敗すればfalse
     */
    public static boolean deleteMedia(final int mediaId, final String userName, final String applicationPassword) {
        String auth = Base64.getEncoder().encodeToString((userName + ":" + applicationPassword).getBytes());

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MEDIA_ENDPOINT + "/" + mediaId + "?force=true"))
                    .header("Authorization", "Basic " + auth)
                    .DELETE()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                log.error("メディア削除失敗: status={}, body={}", response.statusCode(), response.body());
                return false;
            }

            log.info("メディア削除成功: mediaId={}, response={}", mediaId, response.body());
            return true;

        } catch (Exception e) {
            log.error("メディア削除処理でエラー: mediaId={}", mediaId, e);
            return false;
        }
    }

    /**
     * カテゴリの一覧を取得します.
     *
     * @return カテゴリ一覧.
     */
    public static WordPressCategoryLot getCategories(final String userName, final String applicationPassword) {
        WordPressCategoryLot allCategories = new WordPressCategoryLot();
        int page = 1;
        boolean hasMore = true;
        String auth = Base64.getEncoder().encodeToString((userName + ":" + applicationPassword).getBytes());
        HttpClient client = HttpClient.newHttpClient();
        while (hasMore) {
            try {
                URI uri = URI.create(CATEGORY_ENDPOINT + "?per_page=100&page=" + page);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("Authorization", "Basic " + auth)
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
                if (response.statusCode() / 100 != 2) {
                    log.error("カテゴリ取得失敗: page={}, status={}, body={}", page, response.statusCode(), response.body());
                    break;
                }
                List<WordPressCategory> currentPage = objectMapper.readValue(response.body(), new TypeReference<>() {});
                if (currentPage.isEmpty()) {
                    hasMore = false;
                } else {
                    allCategories.getCategoryLot().addAll(currentPage);
                    page++;
                }
            } catch (Exception e) {
                log.error("カテゴリページ取得エラー: page=" + page, e);
                break;
            }
        }
        return allCategories;
    }

    /**
     * 引数のカテゴリ名をWordPressのカテゴリに追加する（親カテゴリ指定可能）.
     *
     * @param categoryName カテゴリ名
     * @param parentCategoryId 親カテゴリID（nullなら親なし）
     * @return カテゴリID(追加に失敗した場合-1)
     */
    public static int addCategory(final String categoryName, final Integer parentCategoryId, final String userName, final String applicationPassword) {
        try {
            String auth = Base64.getEncoder().encodeToString((userName + ":" + applicationPassword).getBytes());

            // リクエストJSONの構築
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("name", categoryName);
            if (parentCategoryId != null) {
                jsonNode.put("parent", parentCategoryId);
            }
            String json = objectMapper.writeValueAsString(jsonNode);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CATEGORY_ENDPOINT))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                log.error("カテゴリ追加失敗: status={}, body={}", response.statusCode(), response.body());
                return -1;
            }

            JsonNode root = objectMapper.readTree(response.body());
            return root.get("id").asInt();

        } catch (Exception e) {
            log.error("カテゴリ追加処理でエラー", e);
            return -1;
        }
    }

    /**
     * タグ一覧を取得します.
     *
     * @return タグ一覧
     */
    public static WordPressTagLot getTags(final String userName, final String applicationPassword) {
        WordPressTagLot allTags = new WordPressTagLot();
        int page = 1;
        boolean hasMore = true;
        String auth = Base64.getEncoder().encodeToString((userName + ":" + applicationPassword).getBytes());
        HttpClient client = HttpClient.newHttpClient();
        while (hasMore) {
            try {
                URI uri = URI.create(TAG_ENDPOINT + "?per_page=100&page=" + page);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("Authorization", "Basic " + auth)
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
                if (response.statusCode() / 100 != 2) {
                    log.error("タグ取得失敗: page={}, status={}, body={}", page, response.statusCode(), response.body());
                    break;
                }
                List<WordPressTag> currentPage = objectMapper.readValue(response.body(), new TypeReference<>() {});
                if (currentPage.isEmpty()) {
                    hasMore = false;
                } else {
                    allTags.getTagLot().addAll(currentPage);
                    page++;
                }
            } catch (Exception e) {
                log.error("タグページ取得エラー: page=" + page, e);
                break;
            }
        }
        return allTags;
    }

    /**
     * 引数のタグ名をWordPressのタグに追加する.
     *
     * @param tagName タグ名
     * @return タグID(追加に失敗した場合-1)
     */
    public static int addTag(final String tagName, final String userName, final String applicationPassword) {
        try {
            String auth = Base64.getEncoder().encodeToString((userName + ":" + applicationPassword).getBytes());

            String json = objectMapper.writeValueAsString(Collections.singletonMap("name", tagName));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TAG_ENDPOINT))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                log.error("タグ追加失敗: status={}, body={}", response.statusCode(), response.body());
                return -1;
            }

            JsonNode root = objectMapper.readTree(response.body());
            return root.get("id").asInt();

        } catch (Exception e) {
            log.error("タグ追加処理でエラー", e);
            return -1;
        }
    }

    @Data
    public static class WordPressCategoryLot implements Iterable<WordPressCategory> {
        private List<WordPressCategory> categoryLot = new ArrayList<WordPressCategory>();

        /**
         * このLotにカテゴリを追加します.
         *
         * @param category カテゴリ
         */
        public void addCategory(final WordPressCategory category) {
            this.categoryLot.add(category);
        }

        /**
         * 引数のカテゴリ名がこのLotに含まれているかどうかを返却する.
         *
         * @param categoryName カテゴリ名
         * @return 含まれていればtrue、それ以外はfalse
         */
        public boolean contains(final String categoryName) {
            return categoryLot.stream().anyMatch(c -> categoryName != null && categoryName.equals(c.getName()));
        }

        /**
         * 指定した名前のカテゴリをこのLotから取得します.
         *
         * @param categoryName カテゴリ名
         * @return 該当カテゴリが存在すればそれを返却し、存在しなければnull
         */
        public WordPressCategory getWordPressCategoryByName(final String categoryName) {
            return categoryLot.stream()
                    .filter(c -> categoryName != null && categoryName.equals(c.getName()))
                    .findFirst()
                    .orElse(null);
        }

        /**
         * このLotに含まれるカテゴリのID一覧を返却します.
         *
         * @return カテゴリIDのリスト
         */
        public List<Integer> getCategoryIdList() {
            return categoryLot.stream()
                              .map(WordPressCategory::getId)
                              .toList();
        }

        @Override
        public Iterator<WordPressCategory> iterator() {
            return this.categoryLot.iterator();
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WordPressCategory {
        private int id;
        private String name;
        private String slug;
    }

    @Data
    public static class WordPressTagLot implements Iterable<WordPressTag> {
        private List<WordPressTag> tagLot = new ArrayList<WordPressTag>();

        public void setTagLot(List<WordPressTag> tagLot) {
            this.tagLot = tagLot;
        }

        public void addTag(final WordPressTag tag) {
            this.tagLot.add(tag);
        }

        public boolean contains(final String tagName) {
            return tagLot.stream().anyMatch(t -> tagName != null && tagName.equals(t.getName()));
        }

        public WordPressTag getWordPressTagByName(final String tagName) {
            return tagLot.stream()
                    .filter(t -> tagName != null && tagName.equals(t.getName()))
                    .findFirst()
                    .orElse(null);
        }

        public List<Integer> getTagIdList() {
            return tagLot.stream()
                          .map(WordPressTag::getId)
                          .toList();
        }

        @Override
        public Iterator<WordPressTag> iterator() {
            return tagLot.iterator();
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WordPressTag {
        private int id;
        private String name;
        private String slug;
    }
}
