package org.catalyte.io.tests.scenarios;

import io.qameta.allure.testng.AllureTestNg;
import java.time.Duration;
import java.util.List;
import org.catalyte.io.pages.ApprenticeshipsPage;
import org.catalyte.io.utils.LoggerUtil;
import org.catalyte.io.utils.TestListener;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

@Listeners({AllureTestNg.class, TestListener.class})
public class ApprenticeshipsPageTests {

  private static final java.util.logging.Logger logger = LoggerUtil.getLogger(
      ApprenticeshipsPageTests.class);
  private WebDriver driver;
  private WebDriverWait wait;
  private ApprenticeshipsPage page;
  private SoftAssert softAssert;
  private int checks = 0;

  @BeforeClass
  public void setUp() {
    driver = new ChromeDriver();
    TestListener.setDriver(driver);
    driver.manage().window().maximize();
    wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    driver.get("https://www.catalyte.io/apprenticeships/");
    page = new ApprenticeshipsPage(driver);
    page.waitForCards(2); // initial stabilization
  }

  @BeforeMethod
  public void initSoftAssert() {
    softAssert = new SoftAssert();
  }

  @Test
  public void regressionTestForAllApprenticeshipLinks_CheckNavigationFunctionality() {
    // Read the names directly from card titles
    List<String> apprenticeships = page.getApprenticeshipNames();
    System.out.println("Found apprenticeships (titles): " + apprenticeships);

    softAssert.assertTrue(!apprenticeships.isEmpty(), "No apprenticeship cards found on page.");
    checks++;

    for (String apprenticeship : apprenticeships) {
      System.out.println("Testing apprenticeship: " + apprenticeship);

      page.clickApprenticeshipType(apprenticeship);
      boolean valid = page.verifyApprenticeshipPage(apprenticeship);

      softAssert.assertTrue(
          valid,
          "Apprenticeship page validation failed for: " + apprenticeship + " | URL: "
              + driver.getCurrentUrl()
      );

      driver.navigate().back();
      page.waitUntilBackOnCards(); // <-- critical to avoid timeouts
    }
    softAssert.assertAll();
  }

  @AfterClass(alwaysRun = true)
  public void tearDown() {
    logger.info("Checks run: " + checks);
    if (driver != null) {
      driver.quit();
    }
  }
}