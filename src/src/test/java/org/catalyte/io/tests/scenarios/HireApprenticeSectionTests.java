package org.catalyte.io.tests.scenarios;

import io.qameta.allure.testng.AllureTestNg;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.catalyte.io.pages.HirePage;
import org.catalyte.io.tests.unit.BaseUiTest;
import org.catalyte.io.utils.TestListener;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({AllureTestNg.class, TestListener.class})
public class HireApprenticeSectionTests extends BaseUiTest {

  private final String hirePageUrl = "https://www.catalyte.io/hire-talent/hire-apprentices/";
  private HirePage hire;
  private List<String> warnings;

  @BeforeClass(alwaysRun = true)
  public void setUpPages() {   // <-- different name; not overriding anything
    // BaseUiTest.setUpBase() runs first and creates 'driver'
    if (driver == null) {
      throw new IllegalStateException("Driver is null in setUpPages()");
    }
    open(hirePageUrl);
    hire = new HirePage(driver);
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

  @Test
  public void testImagesExistLenient() {
    checkElement(hire::isImage3111Displayed, "Image 3111 missing");
    checkElement(hire::isImage3112Displayed, "Image 3112 missing");
    checkElement(hire::isImage3113Displayed, "Image 3113 missing");
  }

  @Test
  public void testVideosExistLenient() throws InterruptedException {
    checkElement(() -> hire.getPRVideo().isDisplayed(), "Apprenticeships PR video iframe missing");
    checkElement(() -> hire.getTestimonialsVideo().isDisplayed(),
        "Apprenticeships testimonials video iframe missing");
  }

  @Test
  public void testWorkWithUsButtonDisplayedAndFunctionsLenient() {
    checkElement(() -> hire.getWorkWithUsButton().isDisplayed(), "Main workflow button missing");
    hire.clickWorkWithUsButton();
    getWait().until(ExpectedConditions.urlContains("/about/"));
    String currentUrl = driver.getCurrentUrl().toLowerCase();
    org.testng.Assert.assertTrue(
        currentUrl.contains("/about/contact-sales"),
        "Button did not navigate to expected page. Current URL: " + currentUrl
    );
  }

  @Test
  public void testMenuTextItemsLenient() {
    if (hire.getMenuTextItems().isEmpty()) {
      warnings.add("Menu text items missing");
      logger.warning("Menu text items missing");
    }
  }

  @Test
  public void faqAccordion_ExistsAndDisplaysExpectedText() {
    String howCanIHire = "How can I hire Catalyte talent?";
    Assert.assertTrue(
        hire.faqContentContainsAfterAllottedTime(howCanIHire, "We work closely",
            Duration.ofSeconds(5)),
        "FAQ content did not contain expected phrase for: " + howCanIHire
    );

    String findTalent = "How do you deliver high-quality talent?";
    Assert.assertTrue(
        hire.faqContentContainsAfterAllottedTime(findTalent, "three steps", Duration.ofSeconds(5)),
        "FAQ content did not contain expected phrase for: " + findTalent
    );

    String whatIndustries = "What industries do you provide talent for?";
    Assert.assertTrue(
        hire.faqContentContainsAfterAllottedTime(whatIndustries, "clients in many industries",
            Duration.ofSeconds(5)),
        "Requirements FAQ missing expected phrase for: " + whatIndustries
    );

    String ifNeedMore = "What if I need more than apprentice talent?";
    Assert.assertTrue(
        hire.faqContentContainsAfterAllottedTime(ifNeedMore, "across all experience levels",
            Duration.ofSeconds(5)),
        "FAQ content did not contain expected phrase for: " + ifNeedMore
    );
  }

  @Test
  public void testAllFaqsButtonDisplayedAndFunctionsLenient() {
    checkElement(() -> hire.getAllFaqsButton().isDisplayed(), "'All FAQS' button missing");
    hire.clickAllFaqsButton();
    getWait().until(ExpectedConditions.urlContains("/about/"));
    String currentUrl = driver.getCurrentUrl().toLowerCase();
    org.testng.Assert.assertTrue(
        currentUrl.contains("/about/faqs"),
        "Button did not navigate to expected page. Current URL: " + currentUrl
    );
  }
}