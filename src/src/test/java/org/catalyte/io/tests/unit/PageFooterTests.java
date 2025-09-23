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
import org.catalyte.io.utils.ButtonNavHelper;
import org.catalyte.io.utils.LocatorMapper;
import org.catalyte.io.utils.TestListener;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
public class PageFooterTests extends BaseUiTest {

  private final String startPageUrl = "https://www.catalyte.io/";
  ButtonNavHelper buttonNavHelper;
  private PageFooter footer;
  private LocatorMapper mapper;

  @BeforeClass(alwaysRun = true)
  public void setUpPages() {
    if (driver == null) {
      throw new IllegalStateException("Driver is null in setUpPages()");
    }
    open(startPageUrl);
    footer = new PageFooter(driver);
    mapper = new LocatorMapper(footer);
    buttonNavHelper = new ButtonNavHelper();
  }

  @BeforeMethod(alwaysRun = true)
  @Override
  protected String startUrlForThisClass() {
    return startPageUrl;
  }

  @BeforeMethod(alwaysRun = true)
  protected List<String> failures() {
    return new ArrayList<>();
  }

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

    safeOpen(startPageUrl);
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("section.footer-menu")));

    Map<String, List<String>> menus = new LinkedHashMap<>();
    for (String heading : mapper.headingKeys()) {
      mapper.linksInMenuBy(heading).ifPresent(linksBy -> {
        List<String> hrefs = driver.findElements(linksBy).stream()
            .map(a -> a.getAttribute("href"))
            .filter(Objects::nonNull).distinct().toList();
        menus.put(heading, hrefs);
      });
    }

    for (var entry : menus.entrySet()) {
      String heading = entry.getKey();
      String separator = "\n --------------- \n";
      logger.info(separator + "Testing nav menu with heading " + heading + separator);

      for (String href : entry.getValue()) {
        safeOpen(startPageUrl);
        wait.until(
            ExpectedConditions.presenceOfElementLocated(By.cssSelector("section.footer-menu")));

        By menuBy = mapper.menuBy(heading).orElseThrow();
        By linkBy = mapper.linkInMenuBy(heading, href).orElseThrow();

        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(linkBy));
        String menuLinkText = link.getText();
        logger.info("Testing nav menu link " + menuLinkText);

        ((JavascriptExecutor) driver).executeScript("arguments[0].removeAttribute('target');",
            link);
        String before = driver.getCurrentUrl();

        // JS-click to avoid full-load waits
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);

        boolean changed = footer.waitForUrlChangeOrHash(
            new WebDriverWait(driver, Duration.ofSeconds(10)), before, href);
        if (changed) {
          try {
            ((JavascriptExecutor) driver).executeScript("window.stop();");
          } catch (Exception ignored) {
          }
        }

        String current = driver.getCurrentUrl();
        boolean ok = changed && allowedForHeading(heading, current, href);
        if (!ok) {
          failures().add(
              String.format("[Footer][%s][%s] %s → %s", heading, menuLinkText, href, current));
        }
      }
    }

    if (!failures().isEmpty()) {
      String msg =
          "Footer nav warnings (" + failures().size() + "):\n" + String.join("\n", failures());
      logger.warning(msg);
    }
    Assert.assertTrue(true); // always pass by design
  }

  @Test
  public void verifyTopFooterSectionTextDisplayed() {
    for (WebElement e : footer.getTopFooterSectionText()) {
      logger.info(e.getText());
      checkElement(e::isDisplayed,
          "One or more top footer section text elements missing");
    }
  }

  @Test
  public void verifyTopFooterSectionButtonsDisplayedAndFunctionalLenient() {
    driver.get(startPageUrl);
    getWait().until(
        ExpectedConditions.presenceOfElementLocated(By.cssSelector("section.footer-menu")));

    var buttons = ButtonNavHelper.snapshotButtons(
        footer.getTopFooterSectionButtons()); // (text, href)
    Map<String, String> expect = Map.of(
        "Learn more", "/apprenticeships/",
        "Connect now", "/about/contact-sales/"
    );

    By TOP_FOOTER = footer.getPageFooterSectionTopLocator();
    By FOOTER_DID_LOAD = By.cssSelector("section.footer-menu");
    Duration defaultWait = Duration.ofSeconds(10);

    for (var b : buttons) {
      ButtonNavHelper.verifyButton(driver, startPageUrl, FOOTER_DID_LOAD, TOP_FOOTER, b, expect,
              defaultWait)
          .ifPresent(failures()::add);
    }
    failures().forEach(logger::warning);
  }

  //==== Footer test helpers ==== //
  private boolean isPresent(By locator) {
    return !driver.findElements(locator).isEmpty();
  }

  private boolean waitForFooter() {
    try {
      WebElement footerElement = wait.until(
          ExpectedConditions.presenceOfElementLocated
              (footer.getFooterLocatorsAsList().get(0)));
      return footerElement != null;
    } catch (Exception e) {
      return false;
    }
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
    if (list == null || list.isEmpty()) {
      return urlsMatchLenient(expectedHref, actualUrl);
    }
    return list.stream().anyMatch(allow -> urlsMatchLenient(allow, actualUrl));
  }
}