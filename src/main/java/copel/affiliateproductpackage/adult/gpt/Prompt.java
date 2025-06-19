package copel.affiliateproductpackage.adult.gpt;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.io.JsonStringEncoder;

import lombok.Data;

@Data
public class Prompt {
    /**
     * プロンプト.
     */
    private String prompt;

    /**
     * プロンプトをテキストファイルから読み込む.
     * 基本的にテキストファイルはsrc/main/resource/prompt配下に配置すること。
     * その上で、ファイルパスの指定は/prompt/XXX.txtの形式で指定すること。
     *
     * @param filePath テキストファイルのファイルパス
     */
    public void createPromptByFilePath(final String filePath) {
        try (InputStream is = getClass().getResourceAsStream(filePath);
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            this.prompt = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            throw new RuntimeException("ファイルの読み込みに失敗しました: " + filePath, e);
        }
    }

    /**
     * プロンプト中の文字列を置換する.
     *
     * @param key 置換対象の文字列
     * @param value 置換後の文字列
     */
    public void replaceAll(final String key, final String value) {
        this.prompt = this.prompt.replaceAll(key, value);
    }

    /**
     * プロンプト中の先頭の{value}の
     * @param value
     */
    public void setValue(final String value) {
        this.prompt = this.prompt.replaceFirst("\\{value\\}", value);
    }

    /**
     * このプロンプトの末尾に文字列を追加する.
     *
     * @param str 文字列
     */
    public void append(final String str) {
        this.prompt += str;
    }

    @Override
    public String toString() {
        return prompt != null
                ? new String(JsonStringEncoder.getInstance().quoteAsString(this.prompt))
                : "";
    }
}
