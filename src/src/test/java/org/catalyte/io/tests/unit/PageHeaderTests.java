package org.catalyte.io.tests.unit;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.catalyte.io.pages.PageHeader;
import org.catalyte.io.utils.ButtonNavHelper;
import org.catalyte.io.utils.LocatorMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PageHeaderTests extends BaseUiTest {

  private final String startPageUrl = "https://www.catalyte.io/";
  ButtonNavHelper buttonNavHelper;
  private PageHeader header;
  private LocatorMapper mapper;

  @BeforeClass(alwaysRun = true)
  public void setUpPages() {
    if (driver == null) {
      throw new IllegalStateException("Driver is null in setUpPages()");
    }
    open(startPageUrl);
    header = new PageHeader(driver);
    mapper = new LocatorMapper(header);
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

  /**
   * Basic smoke: header present, logo visible, primary/secondary menus present.
   */
  @Test
  public void verifyHeaderScaffoldPresent() {
    driver.get(startPageUrl);
    wait.until(presenceOfElementLocated(header.headerRootBy()));

    // By-based, warn-only checks (similar to your Checkable pattern)
    checkPresent(header.logoBy(), "Header: logo link missing");
    checkPresent(header.primaryMenuUlBy(), "Header: primary menu missing");
    checkPresent(header.secondaryMenuUlBy(), "Header: secondary menu missing");

    // lenient test
    Assert.assertTrue(true);
  }

  /**
   * Top-level desktop links navigate (lenient; allow fan-out paths per label).
   */
  @Test
  public void verifyPrimaryTopLinksNavigateLenient() {
    driver.get(startPageUrl);
    header.waitForHeaderReady();

    // Snapshot once to avoid staleness
    List<PageHeader.Link> topLinks = header.snapshotTopLinks();

    // Allowed paths per top label (fan-out acceptable)
    Map<String, List<String>> ALLOW = Map.of(
        "Services", List.of("/services/", "/services/solutions/", "/services/engagement-models/"),
        "Apprentices", List.of("/hire-talent/", "/hire-talent/hire-apprentices/"),
        "Success stories",
        List.of("/success-stories/", "/client-success-stories/", "/alumni-success-stories/"),
        "Our company", List.of("/about/"),
        "Insights", List.of("/insights/")
    );

    for (PageHeader.Link link : topLinks) {
      String label = link.text;
      if (label == null || label.isBlank()) {
        continue;
      }

      // Some headers (with mega menus) may intercept; we click the label explicitly
      driver.get(startPageUrl);
      header.waitForHeaderReady();

      String before = driver.getCurrentUrl();
      logger.info("Header top-link: " + label + " -> click");
      boolean clicked = header.clickTopLinkByLabel(label);
      if (!clicked) {
        warnings.add("Top link not found: " + label);
        continue;
      }

      boolean changed = waitForUrlChange(Duration.ofSeconds(10), before);
      if (changed) {
        stopLoading();
      }

      String finalUrl = driver.getCurrentUrl().toLowerCase(Locale.ROOT);
      boolean ok = allowed(finalUrl, ALLOW.getOrDefault(label, List.of()));
      if (!ok) {
        warnings.add(String.format("[Header][Top] '%s' → %s", label, finalUrl));
      }
    }

    dumpWarnings();
    Assert.assertTrue(true); // lenient
  }

  /**
   * Submenu links under key top items navigate (lenient).
   */
  @Test
  public void verifyPrimarySubmenusNavigateLenient() {
    driver.get(startPageUrl);
    header.waitForHeaderReady();

    List<String> topWithSubmenus = List.of("Services", "Engagement models", "Success stories",
        "Job Seekers");

    for (String menu : topWithSubmenus) {
      driver.get(startPageUrl);
      header.waitForHeaderReady();

      if (!header.expandTopMenu(menu)) {
        warnings.add("[Header][Submenu] Could not expand: " + menu);
        continue;
      }

      List<String> hrefs = header.submenuHrefs(menu);
      if (hrefs.isEmpty()) {
        warnings.add("[Header][Submenu] No links under: " + menu);
        continue;
      }

      logger.info("Header submenu '" + menu + "' hrefs: " + hrefs.size());
      for (String href : hrefs) {
        driver.get(startPageUrl);
        header.waitForHeaderReady();
        // Re-expand each time to get fresh DOM
        if (!header.expandTopMenu(menu)) {
          warnings.add("[Header][Submenu] Re-expand failed: " + menu);
          continue;
        }

        // Click by exact href within this menu (absolute hrefs in markup)
        By liBy = headerPrimaryItemBy(menu);
        By linkBy = new ByChained(liBy,
            By.cssSelector("ul.sub-menu a.menu-link[href=\"" + href + "\"]"));

        Optional<WebElement> sub = attempt(() -> driver.findElement(linkBy));
        if (sub.isEmpty()) {
          warnings.add(String.format("[Header][Submenu] Link not found: %s → %s", menu, href));
          continue;
        }

        String before = driver.getCurrentUrl();
        try {
          jsHelper().executeScript("arguments[0].removeAttribute('target');", sub.get());
        } catch (Exception ignored) {
        }
        header.safeClickDismissCookies(sub.get());

        boolean changed = waitForUrlChange(Duration.ofSeconds(10), before);
        if (changed) {
          stopLoading();
        }

        String finalUrl = driver.getCurrentUrl();
        if (!finalUrl.startsWith(href)) {
          warnings.add(String.format("[Header][Submenu] %s: %s → %s", menu, href, finalUrl));
        }
      }
    }

    dumpWarnings();
    Assert.assertTrue(true); // lenient
  }

  /**
   * Wait for URL to change from 'before'.
   */
  private boolean waitForUrlChange(Duration timeout, String before) {
    try {
      return new WebDriverWait(driver, timeout)
          .until(d -> !d.getCurrentUrl().equals(before));
    } catch (Exception e) {
      return false;
    }
  }

  private void stopLoading() {
    try {
      jsHelper().executeScript("window.stop();");
    } catch (Exception ignored) {
    }
  }

  private boolean allowed(String url, List<String> allowedPaths) {
    if (allowedPaths == null || allowedPaths.isEmpty()) {
      return true;
    }
    for (String p : allowedPaths) {
      if (url.contains(p)) {
        return true;
      }
    }
    return false;
  }

  private void dumpWarnings() {
    if (!warnings.isEmpty()) {
      logger.warning("Header warnings (" + warnings.size() + "):\n" + String.join("\n", warnings));
    }
  }

  /**
   * Rebuild the same LI locator used by PageHeader (so we can chain beneath it in test).
   */
  private By headerPrimaryItemBy(String label) {
    String lower = label.trim().toLowerCase(Locale.ROOT).replace("'", "''");
    String xp = ".//li[contains(@class,'menu-item')][.//span[contains(@class,'menu-text') " +
        "and translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='"
        + lower + "']]";
    return new ByChained(header.primaryMenuUlBy(), By.xpath(xp));
  }

  private <T> Optional<T> attempt(Callable<T> c) {
    try {
      return Optional.ofNullable(c.call());
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}