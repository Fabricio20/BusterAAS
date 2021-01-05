package net.notfab.busterass.routes;

import io.undertow.server.HttpServerExchange;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeExtract {

    private final Random random = new Random();
    private final WebDriver driver;
    private final WebDriverWait waiter;
    private final Pattern pattern = Pattern.compile(".config = (\\{.+});yt");

    public YoutubeExtract() {
        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
        options.addExtensions(new File("extras/uBlock.crx"));

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);

        driver = new ChromeDriver(options);
        waiter = new WebDriverWait(driver, 10);

        // Applied timeouts
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
    }

    public JSONObject handleRequest(HttpServerExchange exchange) throws Exception {
        String URL = exchange.getQueryParameters().get("url").getFirst();
        JSONObject playerData = this.openURL(URL);
        if (playerData == null) {
            throw new IllegalStateException("No player data");
        }
        JSONObject format = this.findAdaptiveOpusFormat(playerData);
        if (format == null) {
            throw new IllegalStateException("No adaptive formats");
        }
        this.pauseStream();
        // --
        JSONObject response = new JSONObject();
        response.put("url", format.getString("url"));
        response.put("approxDurationMs", format.getString("approxDurationMs"));
        response.put("playabilityStatus", playerData.getJSONObject("playabilityStatus"));
        {
            JSONObject metadata = new JSONObject();
            JSONObject microFormat = playerData.getJSONObject("microformat")
                    .getJSONObject("playerMicroformatRenderer");
            metadata.put("publishDate", microFormat.getString("publishDate"));
            metadata.put("lengthSeconds", microFormat.getString("lengthSeconds"));
            metadata.put("title", microFormat.getJSONObject("title").getString("simpleText"));
            metadata.put("description", microFormat.getJSONObject("description").getString("simpleText"));
            metadata.put("channel", microFormat.getString("ownerChannelName"));
            metadata.put("uploadDate", microFormat.getString("uploadDate"));
            metadata.put("isUnlisted", microFormat.getBoolean("isUnlisted"));
            metadata.put("viewCount", microFormat.getString("viewCount"));
            metadata.put("category", microFormat.getString("category"));
            metadata.put("isFamilySafe", microFormat.getBoolean("isFamilySafe"));
            metadata.put("availableCountries", microFormat.getJSONArray("availableCountries"));

            JSONObject videoDetails = playerData.getJSONObject("videoDetails");
            metadata.put("isLiveContent", videoDetails.getBoolean("isLiveContent"));
            metadata.put("keywords", videoDetails.getJSONArray("keywords"));
            metadata.put("isPrivate", videoDetails.getBoolean("isPrivate"));
            metadata.put("isCrawlable", videoDetails.getBoolean("isCrawlable"));
            metadata.put("averageRating", videoDetails.getDouble("averageRating"));
            metadata.put("allowRatings", videoDetails.getBoolean("allowRatings"));
            response.put("metadata", metadata);
        }
        return response;
    }

    private JSONObject openURL(String url) {
        driver.get(url);
        waiter.withTimeout(Duration.ofSeconds(5))
                .until(x -> !x.findElements(By.id("player-wrap")).isEmpty());
        WebElement element = driver.findElement(By.id("player-wrap"));
        List<WebElement> elementList = element.findElements(By.xpath("//script[contains(text(), 'googlevideo.com')]"));
        if (!elementList.isEmpty()) {
            String text = elementList.get(0).getAttribute("text");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return new JSONObject(new JSONObject(matcher.group(1))
                        .getJSONObject("args")
                        .getString("player_response"));
            }
        }
        return null;
    }

    private JSONObject findAdaptiveOpusFormat(JSONObject object) {
        JSONArray adaptiveFormats = object.getJSONObject("streamingData")
                .getJSONArray("adaptiveFormats");
        JSONObject format = null;
        for (int i = 0; i < adaptiveFormats.length(); i++) {
            format = adaptiveFormats.getJSONObject(i);
            if (format.getString("mimeType").contains("opus")) {
                break;
            }
        }
        return format;
    }

    private void pauseStream() throws Exception {
        List<WebElement> videos = driver.findElements(By.className("video"));
        if (videos.isEmpty()) {
            return;
        }
        TimeUnit.SECONDS.sleep(1);
        ((JavascriptExecutor) driver).executeScript("document.getElementsByTagName(\"video\")[0].click();");
        this.simulateHumanBehaviour();
    }

    private void simulateHumanBehaviour() throws Exception {
        // Random sleep (1)
        if (random.nextBoolean()) {
            TimeUnit.SECONDS.sleep(random.nextInt(2) + 1);
        }
        // Toggle auto-play
        if (random.nextBoolean()) {
            WebElement element = driver.findElement(By.id("toggleButton"));
            if (element != null) {
                element.click();
            }
        }
        // Random sleep (2)
        if (random.nextBoolean()) {
            TimeUnit.SECONDS.sleep(random.nextInt(2) + 1);
        }
        // Scroll down a bit
        if (random.nextBoolean()) {
            ((JavascriptExecutor) driver).executeScript("scroll(0," + (random.nextInt(500) + 100) + ")");
        }
    }

}
