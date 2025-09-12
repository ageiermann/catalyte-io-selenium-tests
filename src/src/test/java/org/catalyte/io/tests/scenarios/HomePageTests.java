package org.catalyte.io.tests.scenarios;

import io.qameta.allure.testng.AllureTestNg;
import java.util.ArrayList;
import java.util.List;
import org.catalyte.io.pages.HomePage;
import org.catalyte.io.tests.unit.BaseUiTest;
import org.catalyte.io.utils.TestListener;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({AllureTestNg.class, TestListener.class})
public class HomePageTests extends BaseUiTest {

  private HomePage page;
  private List<String> warnings;
  private final String homePageUrl = "https://www.catalyte.io/";

  @BeforeClass(alwaysRun = true)
  public void setUpPages() {
    if (driver == null) {
      throw new IllegalStateException("Driver is null in setUpPages()");
    }
    open(homePageUrl);
    page = new HomePage(driver);
  }

  @BeforeMethod(alwaysRun = true)
  @Override
  protected String startUrlForThisClass() { return homePageUrl; }

  @Test
  public void verifyAllHeadingElementsPresent() {
    for (WebElement e : page.getHomepageHeadingElements()) {
      checkElement(e::isDisplayed, "One or more heading elements missing");
    }
  }
  @Test
  public void testGetStartedButtonDisplayedAndFunctionsLenient() {
    checkElement(() -> page.getGetStartedButton().isDisplayed(), "'Get Started' button missing");
    page.clickGetStartedButton();
    getWait().until(ExpectedConditions.urlContains("/about/"));
    String currentUrl = driver.getCurrentUrl().toLowerCase();
    Assert.assertTrue(
        currentUrl.contains("/about/contact-sales/"),
        "Button did not navigate to expected page. Current URL: " + currentUrl
    );
  }

  @Test
  public void verifyAllInfoboxesPresent() {
    for (WebElement t : page.getTopHomepageInfoboxes()) {
      checkElement(t::isDisplayed, "One or more top row infoboxes missing");
    }
    for (WebElement b : page.getBottomHomepageInfoboxes()) {
      checkElement(b::isDisplayed, "One or more bottom row infoboxes missing");
    }
  }

  @Test
  public void verifyDividerPresentAndButtonFunctionsLenient() {
    checkElement(() -> page.getPartnerWithUsButton().isDisplayed(),
        "Homepage divider button missing; assume divider also missing");
    page.clickPartnerWithUsButton();
    getWait().until(ExpectedConditions.urlContains("/about/"));
    String currentUrl = driver.getCurrentUrl().toLowerCase();
    Assert.assertTrue(currentUrl.contains("/about/contact-sales/"),
        "Button did not navigate to expected page. Current URL: " + currentUrl);
  }

  @Test
  public void verifyAllClientTypeSectionsPresentWithChildElements() {
    List<WebElement> clientTypeSections = new ArrayList<>();
    clientTypeSections.add(page.getHomepageClientTypeHeading());
    clientTypeSections.add(page.getHomepageClientTypeEnterprise());
    clientTypeSections.add(page.getHomepageClientTypeGovernment());
    clientTypeSections.add(page.getHomepageClientTypeStartups());
    clientTypeSections.add(page.getHomepageClientTypePrivateEquity());

    for(WebElement section : clientTypeSections) {
      if(!section.isDisplayed()) {
        warnings.add("Client type section missing. Check page for specifics");
        logger.warning("Client type section missing. Check page for specifics");
      }
      else for(WebElement e : page.getChildElements(section)) {
        checkElement(e::isDisplayed, "Client type section " + section + "missing one"
            + "or more child elements");
      }
    }
  }

  @Test
  public void verifyAllEngagementModelsElementsPresent() {

    WebElement engagementHeading = page.getHomepageEngagementModelsHeading();
    WebElement engagementSection = page.getHomepageEngagementModelsSection();

    checkElement(engagementHeading::isDisplayed, "Engagement models heading text missing");

    if(!engagementSection.isDisplayed()) {
      warnings.add("Engagement models section missing");
      logger.warning("Engagement models section missing");
    }
    else for(WebElement e : page.getChildElements(engagementSection)) {
      checkElement(e::isDisplayed, "One or more engagement models infoboxes missing");
    }
  }

  @Test
  public void verifyEngagementModelsLearnMoreButtonIsDisplayedAndFunctionsLenient() {
    checkElement(() -> page.getEngagementModelsLearnMoreButton().isDisplayed(),
        "Engagement models 'learn more' button missing");
    page.clickEngagementModelsButton();
    getWait().until(ExpectedConditions.urlContains("/services/"));
    String currentUrl = driver.getCurrentUrl().toLowerCase();
    Assert.assertTrue(currentUrl.contains("/services/engagement-models"),
        "Button did not navigate to expected page. Current URL: " + currentUrl);
  }
}