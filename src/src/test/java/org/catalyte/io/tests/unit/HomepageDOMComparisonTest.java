package org.catalyte.io.tests.unit;

import io.qameta.allure.testng.AllureTestNg;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import org.catalyte.io.pages.HomePage;
import org.catalyte.io.utils.LoggerUtil;
import org.catalyte.io.utils.TestListener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({AllureTestNg.class, TestListener.class})
public class HomepageDOMComparisonTest extends BaseUiTest {

  private static final java.util.logging.Logger logger = LoggerUtil.getLogger(
      HomepageDOMComparisonTest.class);

  private HomePage home;

  @BeforeClass(alwaysRun = true)
  public void setUp() throws Exception {
    super.setUpBase();
    open("https://www.catalyte.io/hire-talent/hire-apprentices/");
    home = new HomePage(driver);
    driver.manage().window().maximize();
  }

  @Test
  public void compareBaselineWithLivePageLenientThreshold() throws Exception {
    String baselineHtml = new String(Files.readAllBytes(Paths.get("src/test/resources/home.html")));
    Document baseline = Jsoup.parse(baselineHtml);

    driver.get("https://catalyte.io");
    String liveHtml = driver.getPageSource();
    Document updated = Jsoup.parse(liveHtml);

    logger.info("Starting DOM comparison test...");

    // 1. Page title check (warning only)
    if (!updated.title().equals(baseline.title())) {
      logger.warning(
          "Warning: Page title changed from \"" + baseline.title() + "\" to \"" + updated.title()
              + "\"");
    }

    // 2. Critical IDs with threshold
    Set<String> criticalIds = Set.of("mainNav", "heroSection", "footer");
    int missingCriticalIds = 0;
    for (Element e : baseline.select("[id]")) {
      String id = e.id();
      if (criticalIds.contains(id) && updated.getElementById(id) == null) {
        logger.severe("Warning: Missing critical element with id: " + id);
        missingCriticalIds++;
      }
    }
    double missingPercent = (missingCriticalIds * 100.0) / criticalIds.size();
    Assert.assertTrue(missingPercent < 50,
        "Too many critical elements missing! Missing " + missingPercent + "%");

    // 3. Text content check (lenient partial match)
    Elements baselineTexts = baseline.select("h1, h2, h3, button, a");
    int missingTextCount = 0;
    for (Element e : baselineTexts) {
      String text = e.text().trim();
      if (!text.isEmpty()) {
        boolean exists =
            !updated.select("*:containsOwn(" + text.substring(0, Math.min(10, text.length())) + ")")
                .isEmpty();
        if (!exists) {
          logger.warning("Warning: Text might have changed or missing: \"" + text + "\"");
          missingTextCount++;
        }
      }
    }
    double missingTextPercent = (missingTextCount * 100.0) / baselineTexts.size();
    if (missingTextPercent > 30) { // fail only if more than 30% of key texts are missing
      Assert.fail("Too many key texts missing: " + missingTextPercent + "%");
    }

    // 4. Structural elements count (warning only)
    int baselineSections = baseline.select("header, nav, main, footer").size();
    int updatedSections = updated.select("header, nav, main, footer").size();
    if (baselineSections != updatedSections) {
      logger.warning(
          "Warning: Number of major sections changed: baseline=" + baselineSections + ", live="
              + updatedSections);
    }
  }

  @AfterClass
  public void tearDown() {
    if (driver != null) {
      driver.quit();
    }
  }
}