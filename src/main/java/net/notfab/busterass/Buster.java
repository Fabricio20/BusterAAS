package net.notfab.busterass;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("SpellCheckingInspection")
public class Buster implements Runnable {

    private WebDriver driver;
    private WebDriverWait waiter;

    Buster() {
        File extrasFolder = new File("extras");
        System.setProperty("webdriver.opera.driver",
                new File(extrasFolder, "operadriver_win64\\operadriver.exe").getAbsolutePath());
        System.setProperty("webdriver.gecko.driver",
                new File(extrasFolder, "geckodriver_win64\\geckodriver.exe").getAbsolutePath());
        System.setProperty("webdriver.chrome.driver",
                new File(extrasFolder, "chromedriver_win32\\chromedriver.exe").getAbsolutePath());
    }

    Buster useOpera() {
        OperaOptions options = new OperaOptions();
        options.setBinary("C:\\Users\\Fabricio20\\AppData\\Local\\Programs\\Opera\\65.0.3467.72\\opera.exe");
        options.addExtensions(new File("extras/Buster.crx"));
        options.addExtensions(new File("extras/uBlock.crx"));
        System.out.println(options.getCapabilityNames());

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(OperaOptions.CAPABILITY, options);

        driver = new OperaDriver(options);
        waiter = new WebDriverWait(driver, 10);

        // Applied timeouts
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
        return this;
    }

    Buster useChrome() {
        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
        options.addExtensions(new File("extras/Buster.crx"));
        options.addExtensions(new File("extras/uBlock.crx"));

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);

        driver = new ChromeDriver(options);
        waiter = new WebDriverWait(driver, 10);

        // Applied timeouts
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
        return this;
    }

    @Override
    public void run() {
        try {
            driver.get("https://patrickhlauke.github.io/recaptcha/");

            TimeUnit.SECONDS.sleep(5);

            List<WebElement> iFrames = driver.findElements(By.cssSelector("iframe[src*='recaptcha']:not([title])"));
            if (!iFrames.isEmpty()) {
                driver.switchTo().frame(iFrames.get(0));
            } else {
                return;
            }

            WebElement element = driver.findElement(By.className("recaptcha-checkbox-border"));
            if (element != null) {
                element.click();
                driver.switchTo().defaultContent();
                iFrames = driver.findElements(By.cssSelector("iframe[src*='recaptcha'][title*='recaptcha']"));
                if (!iFrames.isEmpty()) {
                    driver.switchTo().frame(iFrames.get(0));
                    // We are in the "solving" iFrame now.
                }
            }

            TimeUnit.SECONDS.sleep(3);

            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(srcFile.toPath(), new File("./solved.png").toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        driver.quit();
    }

}