package copel.affiliateproductpackage.adult.unit;

import java.time.Duration;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * 画面項目を扱うクラス.
 *
 * @author 鈴木一矢
 *
 */
public class WebBrowser {
    /**
     * 要素の読み込み待ち上限時間のデフォルト値.
     */
    private final static int DEFAULT_WAIT_LIMIT = 10;
    /**
     * WebDriverクライアント.
     */
    private WebDriver driver;
    /**
     * WebDriverWaitインスタンス.
     */
    private WebDriverWait wait;
    /**
     * Lambda環境での実行であるかどうか.
     */
    private boolean isLambda = false;
    /**
     * GitHUB Actions環境での実行であるかどうか.
     */
    private boolean isGithubActions = false;

    /**
     * デフォルトコンストラクタ(ヘッドレスモードなし).
     */
    public WebBrowser() {
        this(false);
    }
    /**
     * コンストラクタ.
     *
     * @param sessionFilePath セッションファイルの場所
     * @param isHeadlessMode ヘッドレスモードフラグ
     */
    public WebBrowser(final String sessionFilePath, final boolean isHeadlessMode) {
        this(isHeadlessMode);
    }
    /**
     * コンストラクタ.
     *
     * @param isHeadlessMode ヘッドレスモードフラグ
     */
    public WebBrowser(final boolean isHeadlessMode) {
        // Lambda環境での実行かどうかを判定
        this.isLambda = System.getenv("AWS_LAMBDA_FUNCTION_NAME") != null;
        // GitHub Actions環境での実行かどうかを判定
        this.isGithubActions = Boolean.TRUE.toString().equals(System.getenv("GITHUB_ACTIONS"));

        // オプションを定義
        ChromeOptions options = new ChromeOptions();

        // ブラウザを人間が操作しているように見せる
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/122.0.0.0 Safari/537.36");

        // ヘッドレスモードの指定
        if (isHeadlessMode && !this.isLambda && !this.isGithubActions) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1280,1696");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
        }

        // Lambdaで動かすときの設定（必ずヘッドレスモードになる）
        if (this.isLambda) {
            System.setProperty("webdriver.chrome.driver", "/opt/bin/chromedriver");
            options.setBinary("/opt/bin/chrome");
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1280x1696");
            options.addArguments("--single-process");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-dev-tools");
            options.addArguments("--disable-setuid-sandbox");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--remote-debugging-port=9222");
        }
        // isGithubActionsで動かすときの設定（必ずヘッドレスモードになる）
        else if (this.isGithubActions) {
            System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
            options.setBinary("/usr/bin/google-chrome");
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1280,1696");
        }
        // ローカルではChromeDriver を自動でセットアップ
        else {
            WebDriverManager.chromedriver().setup();
        }

        // WebDriverを作成
        this.driver = new ChromeDriver(options);

        // navigator.webdriver を false にする
        ((JavascriptExecutor) this.driver).executeScript(
            "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})"
        );

        // WebDriverWaitを作成
        this.wait = new WebDriverWait(this.driver, Duration.ofSeconds(DEFAULT_WAIT_LIMIT));
    }

    /**
     * 引数のURL先にアクセスする.
     *
     * @param url アクセス先URL
     * @throws InterruptedException 
     */
    public void access(final String url) throws InterruptedException {
        this.driver.get(url);
        this.wait(3);
    }

    /**
     * 画面項目に文字を入力する(Xpathで探索).
     *
     * @param xpath XPath
     * @param text 入力する文字列
     * @throws InterruptedException
     */
    public void sendKeysByXpath(final String xpath, final String text) throws InterruptedException {
        WebElement element = this.wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        element = this.driver.findElement(By.xpath(xpath));
        element.clear();
        element.sendKeys(text);
    }

    /**
     * 画面項目に文字を入力する(name属性で探索).
     *
     * @param name name属性値
     * @param text 入力する文字列
     * @throws InterruptedException
     */
    public void sendKeysByName(final String name, final String text) throws InterruptedException {
        WebElement element = this.wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(name)));
        element = this.driver.findElement(By.name(name));
        element.sendKeys(text);
    }

    /**
     * 指定したXPathの要素が見える位置までスクロールする.
     *
     * @param xpath スクロール先の要素のXPath
     */
    public void scrollToElementByXpath(final String xpath) {
        WebElement element = this.wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        ((JavascriptExecutor) this.driver).executeScript("arguments[0].scrollIntoView({ behavior: 'smooth', block: 'center' });", element);
    }

    /**
     * 画面項目をクリックする(Xpathで探索).
     *
     * @param xpath XPath
     */
    public void clickByXpath(final String xpath) {
        WebElement element = this.wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        element.click();
    }

    /**
     * 画面項目をJavaScriptでクリックする.
     *
     * @param xpath Xpath
     */
    public void clickByXpathWithJS(final String xpath) {
        WebElement element = this.wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        ((JavascriptExecutor) this.driver).executeScript("arguments[0].click();", element);
    }

    /**
     * 画面項目をクリックする(name属性で探索).
     *
     * @param name 名前
     */
    public void clickByName(final String name) {
        WebElement element = this.driver.findElement(By.name(name));
        element.click();
    }

    /**
     * 指定したXPathの要素のalt属性を取得する.
     *
     * @param xpath XPath
     * @return alt属性の値
     */
    public String getAltAttributeByXpath(final String xpath) {
        WebElement element = this.wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        return element.getAttribute("alt");
    }

    /**
     * 指定したXPathのプルダウン（<select>）要素から、指定したオプションを選択する.
     *
     * @param xpath XPath
     * @param visibleText 選択するオプションの表示テキスト
     */
    public void selectOptionByXpath(final String xpath, final String visibleText) {
        WebElement dropdownElement = this.wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        Select dropdown = new Select(dropdownElement);
        dropdown.selectByVisibleText(visibleText);
    }

    /**
     * カレンダーの入力項目に日付を入力する(XPathで探索).
     * 
     * @param xpath XPath
     * @param date 日付 (例: "2025-05-15")
     * @throws InterruptedException
     */
    public void inputDateByXpath(final String xpath, final String date) throws InterruptedException {
        WebElement element = this.wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        // JavaScriptでvalueを直接設定し、changeイベントも発火させる
        String script = "arguments[0].value='" + date + "';" +
                        "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));";
        ((JavascriptExecutor) this.driver).executeScript(script, element);
    }

    /**
     * アラートが表示されるまで待機してOKをクリックする.
     */
    public void waitForAlertAndAccept() {
        this.wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = this.driver.switchTo().alert();
        alert.accept();
    }

    /**
     * アラートが現在表示されているかどうかを判定する.
     *
     * @return アラートが表示されていればtrue、そうでなければfalse
     */
    public boolean isAlertPresent() {
        try {
            WebDriverWait shortWait = new WebDriverWait(this.driver, Duration.ofSeconds(2));
            shortWait.until(ExpectedConditions.alertIsPresent());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 引数の秒数だけ処理を待機する.
     *
     * @param second 待機秒数
     * @throws InterruptedException 
     */
    public void wait(final int second) throws InterruptedException {
        Thread.sleep(second * 1000);
    }
    public void wait(final double second) throws InterruptedException {
        Thread.sleep((long) second * 1000);
    }

    /**
     * 引数のURL先に遷移が完了するまで待機する.
     *
     * @param url 遷移先URL
     */
    public void waitByUrl(final String url) {
        this.wait.until(ExpectedConditions.urlContains(url));
    }

    /**
     * 引数のXpathで指定した要素が読み込まれるまで待機する.
     *
     * @param xpath Xpath
     */
    public void waitUntilLoadByXpath(final String xpath) {
        this.wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
    }

    /**
     * Xpathに指定したInput要素のvalue属性が指定の値になるまで待機する.
     *
     * @param xpath Xpath
     * @param expectedValue value属性の期待値
     */
    public void waitForInputValueChange(final String xpath, final String expectedValue) {
        this.wait.until(ExpectedConditions.attributeToBe(By.xpath(xpath), "value", expectedValue));
    }

    /**
     * JavaScriptを実行する.
     *
     * @param script JavaScriptコード
     */
    public String executeScript(final String script) {
        JavascriptExecutor js = (JavascriptExecutor) this.driver;
        return (String) js.executeScript(script);
    }
    public String executeScript(final String script, final String var) {
        JavascriptExecutor js = (JavascriptExecutor) this.driver;
        return (String) js.executeScript(script, var);
    }

    /**
     * 引数のXpathの要素が持つテキストを取得.
     *
     * @param xpath Xpath
     * @return テキスト
     */
    public String getTextByXpath(final String xpath) {
        WebElement element = this.wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        return element.getText();
    }

    /**
     * 指定したXPathの要素のclass属性を取得する.
     *
     * @param xpath XPath
     * @return class属性の値
     */
    public String getClassAttributeByXpath(final String xpath) {
        WebElement element = this.wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        return element.getAttribute("class");
    }

    /**
     * 指定したXPathの要素が存在するかを判定する.
     *
     * @param xpath XPath
     * @return 要素が存在すればtrue、存在しなければfalse
     */
    public boolean existsByXpath(final String xpath) {
        try {
            this.wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    /**
     * ドライバを終了する.
     */
    public void quit() {
        this.driver.quit();
    }
}
