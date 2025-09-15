package org.catalyte.io.tests.unit;

import io.qameta.allure.testng.AllureTestNg;
import java.net.URL;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.catalyte.io.pages.PageFooter;
import org.catalyte.io.utils.TestListener;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

@Listeners({AllureTestNg.class, TestListener.class})
public class PageFooterTests extends BaseUiTest{

  private PageFooter footer;
  private List<String> warnings;
  private final String startPageUrl = "https://www.catalyte.io/";

  @BeforeClass(alwaysRun = true)
  public void setUpPages() {
    if (driver == null) {
      throw new IllegalStateException("Driver is null in setUpPages()");
    }
    open(startPageUrl);
    footer = new PageFooter(driver);
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
  public void verifyFooterMenus_staleSafe() {
    String base = "https://www.catalyte.io/";
    wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    SoftAssert softly = new SoftAssert();

    // 1) Build heading -> hrefs (strings only) on one known page
    driver.get(base);
    By FOOTER = By.cssSelector("section.footer-menu");
    wait.until(ExpectedConditions.presenceOfElementLocated(FOOTER));

    Map<String, List<String>> menus = new LinkedHashMap<>();
    for (WebElement h : driver.findElements(By.cssSelector("section.footer-menu h6.elementor-heading-title"))) {
      String title = h.getText().trim();
      if (title.equalsIgnoreCase("Follow")) continue; // skip social
      WebElement col = h.findElement(By.xpath("./ancestor::div[contains(@class,'elementor-top-column')]"));
      // visible UL (ignore hidden duplicate)
      Optional<WebElement> ul = col.findElements(By.cssSelector("nav.elementor-nav-menu__container ul"))
          .stream().filter(WebElement::isDisplayed).findFirst();
      if (ul.isEmpty()) continue;
      List<String> hrefs = ul.get().findElements(By.cssSelector("a[href]")).stream()
          .map(a -> a.getAttribute("href")).filter(Objects::nonNull).distinct().collect(Collectors.toList());
      menus.put(title, hrefs);
    }

    // 2) For each href: re-open base page, re-find the link fresh, click, validate
    for (Map.Entry<String, List<String>> entry : menus.entrySet()) {
      String heading = entry.getKey();
      for (String href : entry.getValue()) {
        driver.get(base);
        wait.until(ExpectedConditions.presenceOfElementLocated(FOOTER));

        By linkBy = By.cssSelector("section.footer-menu a[href='" + cssEscape(href) + "']");
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(linkBy));

        // same-tab
        ((JavascriptExecutor) driver).executeScript("arguments[0].removeAttribute('target');", link);

        String before = driver.getCurrentUrl();
        safeClick(link, linkBy);

        boolean changed = waitUrlChangeOrHash(href, before);
        boolean ok = changed && urlsMatchLenient(href, driver.getCurrentUrl());

        softly.assertTrue(ok, "[Footer][" + heading + "] " + href + " → " + driver.getCurrentUrl());
      }
    }
    softly.assertAll();
  }

  /* --- tiny helpers --- */

  private void safeClick(WebElement link, By linkBy) {
    try { link.click(); }
    catch (StaleElementReferenceException | ElementClickInterceptedException e) {
      // re-find (stale-safe) and JS click
      WebElement fresh = wait.until(ExpectedConditions.elementToBeClickable(linkBy));
      ((JavascriptExecutor) driver).executeScript("arguments[0].click();", fresh);
    }
  }

  private boolean waitUrlChangeOrHash(String expectedHref, String startUrl) {
    try {
      if (expectedHref.contains("#")) {
        return new WebDriverWait(driver, Duration.ofSeconds(5))
            .until(d -> d.getCurrentUrl().contains("#"));
      }
      return new WebDriverWait(driver, Duration.ofSeconds(10))
          .until(d -> !d.getCurrentUrl().equals(startUrl));
    } catch (TimeoutException t) { return false; }
  }

  private boolean urlsMatchLenient(String expected, String actual) {
    try {
      URL e = new URL(expected), a = new URL(actual);
      boolean host = a.getHost().equalsIgnoreCase(e.getHost());
      String ep = norm(e.getPath()), ap = norm(a.getPath());
      return host && (ap.startsWith(ep) || ep.startsWith(ap));
    } catch (Exception ignore) { return actual.startsWith(expected); }
  }

  private String norm(String p) { return (p == null || p.isBlank()) ? "/" : (p.endsWith("/") ? p : p + "/"); }

  // Escape quotes and backslashes for a[href='...'] CSS
  private String cssEscape(String s) { return s.replace("\\", "\\\\").replace("'", "\\'"); }

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

  private boolean menuUrlNavigationSuccessful(WebElement link, String heading){
    footer.clickNavMenuLink(link);
    getWait().until(ExpectedConditions.urlContains(heading));
    String currentUrl = driver.getCurrentUrl().toLowerCase();
    return currentUrl.contains(heading);
  }
}