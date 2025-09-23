package org.catalyte.io.tests.scenarios;

import io.qameta.allure.testng.AllureTestNg;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.catalyte.io.pages.HirePage;
import org.catalyte.io.tests.unit.BaseUiTest;
import org.catalyte.io.utils.ButtonNavHelper;
import org.catalyte.io.utils.TestListener;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({AllureTestNg.class, TestListener.class})
public class HireApprenticeSectionTests extends BaseUiTest {

  private final String hirePageUrl = "https://www.catalyte.io/hire-talent/hire-apprentices/";
  private HirePage page;

  @BeforeClass(alwaysRun = true)
  public void setUpPages() {
    // BaseUiTest.setUpBase() runs first and creates 'driver'
    if (driver == null) {
      throw new IllegalStateException("Driver is null in setUpPages()");
    }
    open(hirePageUrl);
    page = new HirePage(driver);
  }

  @BeforeMethod
  public void resetWarnings() {
    warnings = new ArrayList<>();
  }

  @BeforeMethod(alwaysRun = true)
  @Override
  public String startUrlForThisClass() {
    return hirePageUrl;
  }

  @BeforeMethod(alwaysRun = true)
  protected List<String> failures() {
    return new ArrayList<>();
  }

  @Test
  public void testImagesExistLenient() {
    checkAll(
        Check.of(page.getImage3111()::isDisplayed, "Image 3111 missing"),
        Check.of(page.getImage3112()::isDisplayed, "Image 3112 missing"),
        Check.of(page.getImage3113()::isDisplayed, "Image 3113 missing"));
  }

  @Test
  public void testVideosExistLenient() throws InterruptedException {
    checkAll(
        Check.of(page.getPRVideo()::isDisplayed, "Apprenticeships PR video iframe missing"),
        Check.of(page.getTestimonialsVideo()::isDisplayed,
            "Apprenticeships testimonials video iframe missing"));
  }

  @Test
  public void testWorkWithUsButtonDisplayedAndFunctionsLenient() {
    var buttons = ButtonNavHelper.snapshotButton(driver,
        page.getWorkWithUsButtonBy());
    Map<String, String> expect = Map.of("Connect now", "/about/contact-sales/");

    By WORK_WITH_US_SECTION = page.getWorkWithUsButtonScope();
    By PAGE_DID_LOAD = page.pageLoadedIfDisplayed();

    for (var b : buttons) {
      ButtonNavHelper.verifyButton(driver, hirePageUrl, PAGE_DID_LOAD, WORK_WITH_US_SECTION, b,
              expect,
              defaultWait)
          .ifPresent(failures()::add);
    }
    failures().forEach(logger::warning);
  }

  @Test
  public void testMenuTextItemsLenient() {
    if (page.getMenuTextItems().isEmpty()) {
      warnings.add("Menu text items missing");
      logger.warning("Menu text items missing");
    }
  }

  @Test
  public void faqAccordion_ExistsAndDisplaysExpectedText() {
    String howCanIHire = "How can I hire Catalyte talent?";
    Assert.assertTrue(
        page.faqContentContainsAfterAllottedTime(howCanIHire, "We work closely",
            Duration.ofSeconds(5)),
        "FAQ content did not contain expected phrase for: " + howCanIHire
    );

    String findTalent = "How do you deliver high-quality talent?";
    Assert.assertTrue(
        page.faqContentContainsAfterAllottedTime(findTalent, "three steps", Duration.ofSeconds(5)),
        "FAQ content did not contain expected phrase for: " + findTalent
    );

    String whatIndustries = "What industries do you provide talent for?";
    Assert.assertTrue(
        page.faqContentContainsAfterAllottedTime(whatIndustries, "clients in many industries",
            Duration.ofSeconds(5)),
        "Requirements FAQ missing expected phrase for: " + whatIndustries
    );

    String ifNeedMore = "What if I need more than apprentice talent?";
    Assert.assertTrue(
        page.faqContentContainsAfterAllottedTime(ifNeedMore, "across all experience levels",
            Duration.ofSeconds(5)),
        "FAQ content did not contain expected phrase for: " + ifNeedMore
    );
  }

  @Test
  public void testAllFaqsButtonDisplayedAndFunctionsLenient() {
    var buttons = ButtonNavHelper.snapshotButton(driver,
        page.getAllFaqsButtonBy());
    Map<String, String> expect = Map.of("All FAQs", "/about/faqs/");

    By ALL_FAQS_SECTION = page.getAllFaqsSectionBy();
    By PAGE_DID_LOAD = page.pageLoadedIfDisplayed();

    for (var b : buttons) {
      ButtonNavHelper.verifyButton(driver, hirePageUrl, PAGE_DID_LOAD, ALL_FAQS_SECTION, b, expect,
              defaultWait)
          .ifPresent(failures()::add);
    }
    failures().forEach(logger::warning);
  }

  @Test
  public void testAboutAIButtonDisplayedAndFunctionsLenient() {
    var buttons = ButtonNavHelper.snapshotButton(driver,
        page.getAboutAIButtonBy());
    Map<String, String> expect = Map.of("About our AI", "/about/catalyte-ai/");

    By ABOUT_AI_SECTION = page.getAboutAiSectionBy();
    By PAGE_DID_LOAD = page.pageLoadedIfDisplayed();

    for (var b : buttons) {
      ButtonNavHelper.verifyButton(driver, hirePageUrl, PAGE_DID_LOAD, ABOUT_AI_SECTION, b, expect,
              defaultWait)
          .ifPresent(failures()::add);
    }
    failures().forEach(logger::warning);
  }
}