package org.catalyte.io.tests.scenarios;

import io.qameta.allure.testng.AllureTestNg;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.catalyte.io.pages.ApprenticeshipsPage;
import org.catalyte.io.tests.unit.BaseUiTest;
import org.catalyte.io.utils.ButtonNavHelper;
import org.catalyte.io.utils.TestListener;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

@Listeners({AllureTestNg.class, TestListener.class})
public class ApprenticeshipsPageTests extends BaseUiTest {

  private final String apprenticeshipsPageUrl = "https://www.catalyte.io/apprenticeships/";
  private ApprenticeshipsPage page;

  @BeforeClass(alwaysRun = true)
  public void setUpPages() {
    if (driver == null) {
      throw new IllegalStateException("Driver is null in setUpPages()");
    }
    open(apprenticeshipsPageUrl);
    page = new ApprenticeshipsPage(driver);
    page.waitForCards(2);
  }

  @BeforeMethod(alwaysRun = true)
  @Override
  protected String startUrlForThisClass() {
    return apprenticeshipsPageUrl;
  }

  @BeforeMethod(alwaysRun = true)
  protected List<String> failures() {
    return new ArrayList<>();
  }

  @Test
  public void verifyAllKeyElementsOfHowItWorksSectionPresent() {
    SoftAssert sa = new SoftAssert();

    sa.assertTrue(!page.getIconBoxTitles().isEmpty(),
        "No elements found for: elementor-icon-box-title");

    sa.assertTrue(page.getWpImage2230().isDisplayed(),
        "WP image with class 'wp-image-2230' not displayed");

    sa.assertTrue(!page.getElement4b74d2c3SizeDefault().isEmpty(),
        "No size-default elements under elementor-element-4b74d2c3");

    sa.assertTrue(!page.getElement2570aea1ImageBoxWrappers().isEmpty(),
        "No image box wrappers under elementor-element-2570aea1");

    sa.assertTrue(!page.getElement3d21807cImageBoxWrappers().isEmpty(),
        "No image box wrappers under elementor-element-3d21807c");

    sa.assertTrue(!page.getImageBoxTitles().isEmpty(),
        "No elements found for: elementor-image-box-title");

    sa.assertAll();
  }

  @Test
  public void regressionTestForAllApprenticeshipLinks_CheckNavigationFunctionality() {
    List<String> apprenticeships = page.getApprenticeshipNames();
    int totalPages = apprenticeships.size();
    logger.info("Found apprenticeships: " + totalPages + apprenticeships);
    int testedPages = 0;
    SoftAssert bootstrap = new SoftAssert();
    bootstrap.assertTrue(!apprenticeships.isEmpty(), "No apprenticeship cards found.");
    bootstrap.assertAll();

    for (String name : apprenticeships) {
      SoftAssert sa = new SoftAssert();
      testedPages++;
      logger.info("Testing apprenticeship: " + name);
      try {
        page.clickApprenticeshipType(name);
        sa.assertTrue(page.verifyApprenticeshipPage(name),
            "Validation failed for: " + name + " | URL: " + driver.getCurrentUrl());
      } catch (Exception e) {
        sa.fail("Exception while testing '" + name + "': " + e.getMessage());
        testedPages--;
      } finally {
        driver.navigate().back();
        page.waitUntilBackOnCards();
      }
      sa.assertAll();
      logger.info("Pages tested successfully: " + testedPages + " of " + totalPages);
    }
  }

  @Test
  public void verifyAllOpportunityMetricsElementsPresent() {
    for (WebElement e : page.getOpportunityMetrics()) {
      checkElement(e::isDisplayed, "Element " + e.getAttribute("data-id") + " missing.");
    }
  }

  @Test
  public void faqAccordion_DisplaysExpectedText() {
    String howDoesItWork = "How does a Catalyte apprenticeship work?";
    Assert.assertTrue(
        page.faqContentContainsAfterAllottedTime(howDoesItWork, "paid while learning",
            Duration.ofSeconds(5)),
        "FAQ content did not contain expected phrase for: " + howDoesItWork
    );

    String cost = "How much does an apprenticeship cost?";
    Assert.assertTrue(
        page.faqContentContainsAfterAllottedTime(cost, "100% free", Duration.ofSeconds(5)),
        "FAQ content did not contain expected phrase for: " + cost
    );

    String applyRequirements = "What are the requirements to apply?";
    Assert.assertTrue(
        page.faqContentContainsAfterAllottedTime(applyRequirements, "Be at least 18 years old",
            Duration.ofSeconds(5)),
        "Requirements FAQ missing expected bullet."
    );

    String commitment = "Are apprenticeships a full-time or part-time commitment?";
    Assert.assertTrue(
        page.faqContentContainsAfterAllottedTime(commitment, "40 hours", Duration.ofSeconds(5)),
        "FAQ content did not contain expected phrase for: " + commitment
    );

    String needDegree = "Do I need a college degree to apply?";
    Assert.assertTrue(
        page.faqContentContainsAfterAllottedTime(needDegree, "do not take degrees",
            Duration.ofSeconds(5)),
        "FAQ content did not contain expected phrase for: " + needDegree
    );
  }

  @Test
  public void testAllFaqsButtonDisplayedAndFunctionsLenient() {
    var buttons = ButtonNavHelper.snapshotButton(driver,
        page.getAllFaqsButtonBy());
    Map<String, String> expect = Map.of("All FAQs", "/about/faqs/");

    By ALL_FAQS_SECTION = page.getFaqSectionBy();
    By PAGE_DID_LOAD = page.pageLoadedIfDisplayed();

    for (var b : buttons) {
      ButtonNavHelper.verifyButton(driver, apprenticeshipsPageUrl, PAGE_DID_LOAD, ALL_FAQS_SECTION,
              b, expect,
              defaultWait)
          .ifPresent(failures()::add);
    }
    failures().forEach(logger::warning);
  }
}