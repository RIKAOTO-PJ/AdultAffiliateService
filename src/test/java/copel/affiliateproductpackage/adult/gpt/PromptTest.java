package copel.affiliateproductpackage.adult.gpt;

import org.junit.jupiter.api.Test;

class PromptTest {

    @Test
    void test() {
        Prompt prompt = new Prompt();
        prompt.createPromptByFilePath("/prompt/アダルトブログ記事生成プロンプト.txt");
        prompt.setValue("作品のタイトル");
        prompt.setValue("あらすじ");
        prompt.setValue("キーワードたち");
        System.out.println(prompt);
    }
}
