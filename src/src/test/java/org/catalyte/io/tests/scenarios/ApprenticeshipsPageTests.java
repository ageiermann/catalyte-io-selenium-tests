package org.catalyte.io.tests.scenarios;

import io.qameta.allure.testng.AllureTestNg;
import java.time.Duration;
import org.catalyte.io.utils.TestListener;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;
import org.catalyte.io.pages.ApprenticeshipsPage;
import org.catalyte.io.utils.LoggerUtil;

@Listeners({AllureTestNg.class, TestListener.class})
public class ApprenticeshipsPageTests {
  protected WebDriver driver;
  protected WebDriverWait wait;
  protected ApprenticeshipsPage apprenticeshipsPage;
  protected SoftAssert softAssert;
  private static final java.util.logging.Logger logger = LoggerUtil.getLogger(
      ApprenticeshipsPageTests.class);
  protected int checks = 0;

  @BeforeClass
  public void setUp() {
    driver = new ChromeDriver();
    TestListener.setDriver(driver);
    driver.manage().window().maximize();
    wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    driver.get("https://www.catalyte.io/apprenticeships");
    apprenticeshipsPage = new ApprenticeshipsPage(driver);
  }

  @BeforeMethod
  public void initSoftAssert() {
    softAssert = new SoftAssert();
  }

  @Test
  public void verifyAllKeyElementsOfHowItWorksSectionPresent() {
    // 1. Icon box titles
    softAssert.assertTrue(!apprenticeshipsPage.getIconBoxTitles().isEmpty(),
        "No elements found for: elementor-icon-box-title");
    checks++;

    // 2. WP image 2230
    softAssert.assertTrue(apprenticeshipsPage.getWpImage2230().isDisplayed(),
        "WP image with class 'wp-image-2230' not displayed");
    checks++;

    // 3. element-4b74d2c3 with size-default
    softAssert.assertTrue(!apprenticeshipsPage.getElement4b74d2c3SizeDefault().isEmpty(),
        "No size-default elements under elementor-element-4b74d2c3");
    checks++;

    // 4. element-2570aea1 image box wrappers
    softAssert.assertTrue(!apprenticeshipsPage.getElement2570aea1ImageBoxWrappers().isEmpty(),
        "No image box wrappers under elementor-element-2570aea1");
    checks++;
    // 5. element-3d21807c image box wrappers
    softAssert.assertTrue(!apprenticeshipsPage.getElement3d21807cImageBoxWrappers().isEmpty(),
        "No image box wrappers under elementor-element-3d21807c");
    checks++;
    // 6. Image box titles
    softAssert.assertTrue(!apprenticeshipsPage.getImageBoxTitles().isEmpty(),
        "No elements found for: elementor-image-box-title");
    checks++;
    // Collate all failures at once
    softAssert.assertAll();
  }

  @AfterClass(alwaysRun = true)
  public void tearDown() {
    logger.info("Checks run: " + checks);
    if (driver != null) driver.quit();
  }
}

