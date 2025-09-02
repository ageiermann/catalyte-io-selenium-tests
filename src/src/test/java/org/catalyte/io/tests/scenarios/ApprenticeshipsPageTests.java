package org.catalyte.io.tests.scenarios;

import io.qameta.allure.testng.AllureTestNg;
import java.util.List;

import org.catalyte.io.pages.ApprenticeshipsPage;
import org.catalyte.io.utils.LoggerUtil;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;

@Listeners({AllureTestNg.class})
public class ApprenticeshipsPageTests extends BaseUiTest {

  private static final java.util.logging.Logger logger = LoggerUtil.getLogger(
      ApprenticeshipsPageTests.class);
  private ApprenticeshipsPage page;

  @BeforeClass(alwaysRun = true)
  @Override
  public void setUpBase() throws Exception {
    super.setUpBase();
    open("https://www.catalyte.io/apprenticeships/");
    page = new ApprenticeshipsPage(driver);
    page.waitForCards(2); // initial stabilization
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
    logger.info("Found apprenticeships: " + totalPages + "" + apprenticeships);
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
}