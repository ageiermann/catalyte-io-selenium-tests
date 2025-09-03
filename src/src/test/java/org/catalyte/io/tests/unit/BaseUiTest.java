package org.catalyte.io.tests.unit;

import io.qameta.allure.Attachment;
import io.qameta.allure.testng.AllureTestNg;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.catalyte.io.utils.TestListener;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

@Listeners({AllureTestNg.class, TestListener.class})
public abstract class BaseUiTest {

  protected WebDriver driver;
  protected WebDriverWait wait;

  // Keep a handle to delete the temp profile afterward
  private Path userDataDir;

  /** Override to change default timeout (seconds). */
  protected int defaultTimeoutSeconds() { return 20; }

  /** Override to change default window size. */
  protected Dimension defaultWindowSize() { return new Dimension(1920, 1080); }

  /** One-time driver init per class. */
  @BeforeClass(alwaysRun = true)
  public final void setUpBase() throws Exception {
    ChromeOptions options = new ChromeOptions();

    // Stable flags for CI runners
    options.addArguments(
        "--headless=new",
        "--no-sandbox",
        "--disable-dev-shm-usage",
        "--disable-gpu",
        "--window-size=" + defaultWindowSize().width + "," + defaultWindowSize().height
    );

    // Make the profile path unique for each test class + CI run
    String runId = System.getenv().getOrDefault("GITHUB_RUN_ID", String.valueOf(System.nanoTime()));
    userDataDir = Files.createTempDirectory("chrome-prof-" + getClass().getSimpleName() + "-" + runId + "-");
    options.addArguments("--user-data-dir=" + userDataDir.toAbsolutePath());

    driver = new ChromeDriver(options);
    wait   = new WebDriverWait(driver, Duration.ofSeconds(defaultTimeoutSeconds()));
  }
  //lazy getter to protect run order
  protected WebDriverWait getWait() {
    if (wait == null) {
      if (driver == null) {
        throw new IllegalStateException("Driver is null; BaseUiTest did not start correctly.");
      }
      wait = new WebDriverWait(driver, Duration.ofSeconds(defaultTimeoutSeconds()));
    }
    return wait;
  }

  /** Optional helper to open a URL (keeps tests tidy). */
  protected void open(String url) {
    driver.get(url);
  }

  /** On failure, attach a screenshot to Allure. */
  @AfterMethod(alwaysRun = true)
  protected void afterEach(ITestResult result) {
    if (!result.isSuccess() && driver instanceof TakesScreenshot ts) {
      attachScreenshot(ts.getScreenshotAs(OutputType.BYTES));
    }
  }

  @Attachment(value = "Failure Screenshot", type = "image/png")
  private byte[] attachScreenshot(byte[] bytes) { return bytes; }

  /** Quit and cleanup the temp Chrome profile. */
  @AfterClass(alwaysRun = true)
  public void tearDownBase() throws Exception {
    if (driver != null) {
      try { driver.quit(); } catch (Exception ignored) {}
    }
    if (userDataDir != null) {
      try { Files.walk(userDataDir)
          .sorted((a,b) -> b.getNameCount() - a.getNameCount()) // delete children first
          .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignored) {} });
      } catch (Exception ignored) {}
    }
  }
}