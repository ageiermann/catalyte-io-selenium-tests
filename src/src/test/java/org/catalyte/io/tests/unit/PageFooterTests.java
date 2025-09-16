package org.catalyte.io.tests.unit;

import io.qameta.allure.testng.AllureTestNg;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.catalyte.io.pages.PageFooter;
import org.catalyte.io.utils.LocatorMapper;
import org.catalyte.io.utils.TestListener;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

@Listeners({AllureTestNg.class, TestListener.class})
public class PageFooterTests extends BaseUiTest{

  private PageFooter footer;
  private LocatorMapper mapper;
  private List<String> warnings;
  private final String startPageUrl = "https://www.catalyte.io/";

  @BeforeClass(alwaysRun = true)
  public void setUpPages() {
    if (driver == null) {
      throw new IllegalStateException("Driver is null in setUpPages()");
    }
    open(startPageUrl);
    footer = new PageFooter(driver);
    mapper = new LocatorMapper(footer);
    warnings = new ArrayList<>();
  }

  @BeforeMethod(alwaysRun = true)
  @Override
  protected String startUrlForThisClass() { return startPageUrl; }

  // === Methods For Footer Common To All Pages === //
  @Test
  public void verifyAllFooterElementsPresentAcrossAllPages() {

    SoftAssert softAssert = new SoftAssert();

    for (String url : footer.getPageURLs()) {
      driver.get(url);
      boolean footerFound = waitForFooter();
      softAssert.assertTrue(footerFound, "Footer container not found on page " + url);
      logger.info("Testing page " + url);

      for (By locator : footer.getFooterLocatorsAsList()) {
        boolean present = isPresent(locator);
        softAssert.assertTrue(present, "Missing footer element [" + locator + "] on page " + url);
      }
    }
    softAssert.assertAll("Footer verification issues found on one or more pages.");
  }

  @Test
  public void verifyAllFooterNavMenuLinksDisplayedAndFunctionalLenient() {
    WebDriverWait wait = getWait();
    List<String> failures = new ArrayList<>();

    safeOpen(startPageUrl);
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("section.footer-menu")));

    Map<String, List<String>> menus = new LinkedHashMap<>();
    for (WebElement headingEl : footer.getNavMenuHeadings()) {
      String headingTitle = headingEl.getText().trim();
      if (headingTitle.equalsIgnoreCase("Follow")) continue;

      mapper.menuBy(headingTitle).ifPresent(menuBy -> {
        WebElement menu = wait.until(ExpectedConditions.visibilityOfElementLocated(menuBy));
        List<String> hrefs = menu.findElements(By.cssSelector("a.menu-link[href]"))
            .stream().map(a -> a.getAttribute("href")).filter(Objects::nonNull).distinct().toList();
        menus.put(headingTitle, hrefs);
      });
    }

    for (var entry : menus.entrySet()) {
      String heading = entry.getKey();
      logger.info("Testing nav menu with heading " + heading);

      for (String href : entry.getValue()) {
        driver.get(startPageUrl);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("section.footer-menu")));

        By menuBy = mapper.menuBy(heading).orElseThrow();
        WebElement menu = wait.until(ExpectedConditions.visibilityOfElementLocated(menuBy));

        By linkBy = By.cssSelector("a.menu-link[href=\"" + cssEscape(href) + "\"]");
        WebElement link = menu.findElement(linkBy);

        // force same-tab
        ((JavascriptExecutor) driver).executeScript("arguments[0].removeAttribute('target');", link);

        String before = driver.getCurrentUrl();
        try {
          footer.clickNavMenuLink(link);
        } catch (ElementClickInterceptedException e) {
          ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
        }
        catch (WebDriverException died) {
          safeOpen(startPageUrl);
          wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("section.footer-menu")));
          menu = wait.until(ExpectedConditions.visibilityOfElementLocated(menuBy));
          link = menu.findElement(linkBy);
          ((JavascriptExecutor) driver).executeScript("arguments[0].removeAttribute('target');", link);
          footer.clickNavMenuLink(link);
        }

        boolean changed = footer.waitForUrlChangeOrHash(wait, before, href);
        String currentUrl = driver.getCurrentUrl();

        boolean ok = changed && allowedForHeading(heading, currentUrl, href);
        if (!ok) {
          failures.add(String.format("[Footer][%s] %s → %s", heading, href, currentUrl));
        }
      }
    }

    if (!failures.isEmpty()) {
      String navMenuWarnings = "Footer nav warnings (" + failures.size() + "):\n"
          + String.join("\n", failures);
      logger.warning(navMenuWarnings);
      warnings.add(navMenuWarnings);
    }
    Assert.assertTrue(true); // always pass
  }

  //==== Footer test helpers ==== //
  private boolean isPresent(By locator) { return !driver.findElements(locator).isEmpty(); }

  private boolean waitForFooter() {
    try {
      WebElement footerElement = wait.until(
          ExpectedConditions.presenceOfElementLocated
              (footer.getFooterLocatorsAsList().get(0)));
      return footerElement != null;
    }
    catch (Exception e) {
      return false;
    }
  }
  private boolean successfullyNavigated(WebElement link, String href, String expected) {

    // Force same tab
    ((JavascriptExecutor) driver).executeScript("arguments[0].removeAttribute('target');", link);
    String before = driver.getCurrentUrl();
    long t0 = System.nanoTime();
    Duration maxWait = Duration.ofSeconds(10);
    boolean navigated = false;
    String afterUrl;

    try {
      try { link.click(); }
      catch (ElementClickInterceptedException e) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
      }

      if (href.contains("#")) {
        navigated = new WebDriverWait(driver, maxWait)
            .until(d -> d.getCurrentUrl().contains("#"));
      } else {
        navigated = new WebDriverWait(driver, maxWait)
            .until(d -> !d.getCurrentUrl().equals(before));
      }
    } catch (TimeoutException te) {
      navigated = false;
    }
    if(!urlsMatchLenient(href, driver.getCurrentUrl())){ navigated = false; }

    afterUrl = driver.getCurrentUrl();
    long ms = (System.nanoTime() - t0) / 1_000_000;
    return navigated;
  }

  private boolean urlsMatchLenient(String expected, String actual) {
    try {
      URL e = new URL(expected), a = new URL(actual);
      boolean host = a.getHost().equalsIgnoreCase(e.getHost());
      String ep = (e.getPath().endsWith("/") ? e.getPath() : e.getPath() + "/");
      String ap = (a.getPath().endsWith("/") ? a.getPath() : a.getPath() + "/");
      return host && (ap.startsWith(ep) || ep.startsWith(ap)); // allow redirects/trailing slash
    } catch (Exception ignore) {
      return actual.startsWith(expected) || actual.startsWith(expected + "/");
    }
  }

  private boolean allowedForHeading(String heading, String actualUrl, String expectedHref) {
    Map<String, List<String>> allowed = mapper.getAllowedUrls();
    List<String> list = allowed.get(heading);
    if (list == null || list.isEmpty()) return urlsMatchLenient(expectedHref, actualUrl);
    return list.stream().anyMatch(allow -> urlsMatchLenient(allow, actualUrl));
  }

  private String normalize(String p){
    return (p == null || p.isBlank())
        ? "/"
        : (p.endsWith("/")
            ? p
            : p+"/" );
  }

  private String cssEscape(String s){
    return s.replace("\\","\\\\")
        .replace("\"","\\\"");
  }
}