package org.catalyte.io.tests.unit;

import io.qameta.allure.Attachment;
import io.qameta.allure.testng.AllureTestNg;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.catalyte.io.utils.LoggerUtil;
import org.catalyte.io.utils.TestListener;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

@Listeners({AllureTestNg.class, TestListener.class})
public abstract class BaseUiTest {

  protected final java.util.logging.Logger logger = LoggerUtil.getLogger(getClass());
  protected WebDriver driver;
  protected WebDriverWait wait;
  protected List<String> warnings;
  protected Duration defaultWait = Duration.ofSeconds(10);
  // Keep a handle to delete the temp profile afterward
  private Path userDataDir;

  /**
   * Override to change default timeout (seconds).
   */
  protected int defaultTimeoutSeconds() {
    return 20;
  }

  /**
   * Override to change default window size.
   */
  protected Dimension defaultWindowSize() {
    return new Dimension(1920, 1080);
  }

  /**
   * One-time driver init per class.
   */
  @BeforeClass(alwaysRun = true)
  public final void setUpBase() throws Exception {
    ChromeOptions opts = new ChromeOptions();

    // Stable flags for CI runners
    opts.setPageLoadStrategy(PageLoadStrategy.EAGER);     // don't wait for every subresource
    opts.addArguments("--headless=new");                  // headless in CI
    opts.addArguments("--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage"); // CI stability
    opts.addArguments("--disable-extensions", "--disable-infobars");
    opts.addArguments("--blink-settings=imagesEnabled=false"); // lighter pages

    // Make the profile path unique for each test class + CI run
    String runId = System.getenv().getOrDefault("GITHUB_RUN_ID", String.valueOf(System.nanoTime()));
    userDataDir = Files.createTempDirectory(
        "chrome-prof-" + getClass().getSimpleName() + "-" + runId + "-");
    opts.addArguments("--user-data-dir=" + userDataDir.toAbsolutePath());

    driver = new ChromeDriver(opts);
    wait = new WebDriverWait(driver, Duration.ofSeconds(defaultTimeoutSeconds()));
    warnings = new ArrayList<>();

    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(12));
    driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(10));
  }

  //Resetting driver state per method to stop flakiness
  protected String startUrlForThisClass() {
    return "https://google.com";
  }

  @BeforeMethod(alwaysRun = true)
  public void resetState() {
    driver.manage().deleteAllCookies();
    driver.navigate().to("about:blank");
    driver.get(startUrlForThisClass());
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

  /**
   * Optional helper to open a URL (keeps tests tidy).
   */
  protected void open(String url) {
    driver.get(url);
  }

  //Helpers to fix browser timeouts
  protected synchronized void restartDriver() {
    try {
      if (driver != null) {
        driver.quit();
      }
    } catch (Exception ignored) {
    }

    this.driver = buildDriver();
  }

  private WebDriver buildDriver() {
    org.openqa.selenium.chrome.ChromeOptions opts = new org.openqa.selenium.chrome.ChromeOptions();
    opts.setPageLoadStrategy(org.openqa.selenium.PageLoadStrategy.EAGER);
    opts.addArguments("--headless=new", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage",
        "--disable-extensions", "--disable-infobars", "--blink-settings=imagesEnabled=false");
    org.openqa.selenium.WebDriver d = new org.openqa.selenium.chrome.ChromeDriver(opts);
    d.manage().timeouts().pageLoadTimeout(java.time.Duration.ofSeconds(12));
    d.manage().timeouts().scriptTimeout(java.time.Duration.ofSeconds(10));
    return d;
  }

  protected void safeOpen(String url) {
    try {
      driver.navigate().to(url);
    } catch (WebDriverException e) {
      if (isDeadSession(e)) {
        restartDriver();
        driver.navigate().to(url);
      } else {
        logger.severe("Driver crashed.");
        throw e;
      }
    }
  }

  private boolean isDeadSession(Throwable t) {
    String msg = String.valueOf(t.getMessage());
    return msg.contains("Session ID is null") || msg.contains("invalid session id")
        || msg.contains("disconnected") || msg.contains("target frame detached") ||
        msg.contains("may have died");
  }

  /**
   * On failure, attach a screenshot to Allure.
   */
  @AfterMethod(alwaysRun = true)
  protected void afterEach(ITestResult result) {
    if (!result.isSuccess() && driver instanceof TakesScreenshot ts) {
      attachScreenshot(ts.getScreenshotAs(OutputType.BYTES));
    }
  }

  @Attachment(value = "Failure Screenshot", type = "image/png")
  private byte[] attachScreenshot(byte[] bytes) {
    return bytes;
  }

  /**
   * Quit and cleanup the temp Chrome profile.
   */
  @AfterClass(alwaysRun = true)
  public void tearDownBase() throws Exception {
    if (driver != null) {
      try {
        driver.quit();
      } catch (Exception ignored) {
      }
    }
    if (userDataDir != null) {
      try {
        Files.walk(userDataDir)
            .sorted((a, b) -> b.getNameCount() - a.getNameCount()) // delete children first
            .forEach(p -> {
              try {
                Files.deleteIfExists(p);
              } catch (Exception ignored) {
              }
            });
      } catch (Exception ignored) {
      }
    }
  }

  // === By-based Checkable factories ===
  protected Checkable present(By by) {
    return () -> !driver.findElements(by).isEmpty();
  }

  protected Checkable displayed(By by) {
    return () -> driver.findElements(by).stream().anyMatch(WebElement::isDisplayed);
  }

  protected Checkable allDisplayed(By by) {
    return () -> {
      var els = driver.findElements(by);
      return !els.isEmpty() && els.stream().allMatch(WebElement::isDisplayed);
    };
  }

  protected Checkable countAtLeast(By by, int n) {
    return () -> driver.findElements(by).size() >= n;
  }

  protected Checkable visibleWithin(By by, Duration timeout) {
    return () -> {
      new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOfElementLocated(by));
      return true;
    };
  }

  protected Checkable clickableWithin(By by, java.time.Duration timeout) {
    return () -> {
      new WebDriverWait(driver, timeout).until(ExpectedConditions.elementToBeClickable(by));
      return true;
    };
  }

  // === Convenience wrappers ===
  protected void checkPresent(By by, String warningMessage) {
    checkElement(present(by), warningMessage);
  }

  protected void checkDisplayed(By by, String warningMessage) {
    checkElement(displayed(by), warningMessage);
  }

  protected void checkVisibleWithin(By by, Duration timeout, String warningMessage) {
    checkElement(visibleWithin(by, timeout), warningMessage);
  }

  protected void checkClickableWithin(By by, Duration timeout, String warningMessage) {
    checkElement(clickableWithin(by, timeout), warningMessage);
  }

  // === Ready-made Check objects for checkAll(...) ===
  protected Check presentCheck(By by, String warningMessage) {
    return Check.of(present(by), warningMessage);
  }

  protected Check displayedCheck(By by, String warningMessage) {
    return Check.of(displayed(by), warningMessage);
  }

  protected Check allDisplayedCheck(By by, String warningMessage) {
    return Check.of(allDisplayed(by), warningMessage);
  }

  protected Check countAtLeastCheck(By by, int n, String msg) {
    return Check.of(countAtLeast(by, n), msg);
  }

  protected Check visibleWithinCheck(By by, Duration t, String msg) {
    return Check.of(visibleWithin(by, t), msg);
  }

  protected Check clickableWithinCheck(By by, Duration t, String msg) {
    return Check.of(clickableWithin(by, t), msg);
  }

  // Return true/false AND log/warn on failure
  protected boolean assertOrWarn(Checkable c, String warningMessage) {
    boolean ok;
    try {
      ok = c.check();
    } catch (Exception e) {
      ok = false;
    }
    if (!ok) {
      warnings.add(warningMessage);
      logger.warning(warningMessage);
    }
    return ok;
  }

  // Combinators
  protected Checkable and(Checkable a, Checkable b) {
    return () -> a.check() && b.check();
  }

  protected Checkable or(Checkable a, Checkable b) {
    return () -> a.check() || b.check();
  }

  protected Checkable not(Checkable a) {
    return () -> !a.check();
  }

  /**
   * Existing single-condition method (unchanged for backwards compatibility).
   */
  protected void checkElement(Checkable condition, String warningMessage) {
    try {
      if (!condition.check()) {
        warnings.add(warningMessage);
        logger.warning(warningMessage);
      }
    } catch (Exception e) {
      String msg = warningMessage + " (exception: " + e.getMessage() + ")";
      warnings.add(msg);
      logger.warning(msg);
    }
  }

  /**
   * Multi-check version. Returns true if all pass, false if any fail.
   */
  protected boolean checkAll(Check... checks) {
    boolean allPassed = true;
    for (Check c : checks) {
      try {
        if (!c.condition.check()) {
          allPassed = false;
          warnings.add(c.warningMessage);
          logger.warning(c.warningMessage);
        }
      } catch (Exception e) {
        allPassed = false;
        String msg = c.warningMessage + " (exception: " + e.getMessage() + ")";
        warnings.add(msg);
        logger.warning(msg);
      }
    }
    return allPassed;
  }

  // ===== Utility methods =====
  @FunctionalInterface
  public interface Checkable {

    boolean check();
  }

  public static class Check {

    final Checkable condition;
    final String warningMessage;

    private Check(Checkable condition, String warningMessage) {
      this.condition = condition;
      this.warningMessage = warningMessage;
    }

    public static Check of(Checkable condition, String warningMessage) {
      return new Check(condition, warningMessage);
    }
  }
}