package org.catalyte.io.tests.scenarios;

import io.qameta.allure.testng.AllureTestNg;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.catalyte.io.pages.HomePage;
import org.catalyte.io.tests.unit.BaseUiTest;
import org.catalyte.io.utils.ButtonNavHelper;
import org.catalyte.io.utils.TestListener;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({AllureTestNg.class, TestListener.class})
public class HomePageTests extends BaseUiTest {

  private final String homePageUrl = "https://www.catalyte.io/";
  private HomePage page;

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
  protected String startUrlForThisClass() {
    return homePageUrl;
  }

  @BeforeMethod(alwaysRun = true)
  protected List<String> failures() {
    return new ArrayList<>();
  }

  @Test
  public void verifyAllHeadingElementsPresent() {
    for (WebElement e : page.getHomepageHeadingElements()) {
      checkElement(e::isDisplayed, "One or more heading elements missing");
    }
  }

  @Test
  public void testGetStartedButtonDisplayedAndFunctionsLenient() {
    var buttons = ButtonNavHelper.snapshotButton(driver,
        page.getGetStartedButtonBy());
    Map<String, String> expect = Map.of("Connect now", "/about/contact-sales/");

    By HOME_HEADING = page.getHomepageHeadingSectionBy();
    By PAGE_DID_LOAD = page.pageLoadedIfDisplayed();

    List<String> failures = new ArrayList<>();
    for (var b : buttons) {
      ButtonNavHelper.verifyButton(driver, homePageUrl, PAGE_DID_LOAD, HOME_HEADING, b, expect,
              defaultWait)
          .ifPresent(failures::add);
    }
    failures().forEach(logger::warning);
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
    var buttons = ButtonNavHelper.snapshotButton(driver,
        page.getPartnerWithUsButtonBy());
    Map<String, String> expect = Map.of("Connect now", "/about/contact-sales/");

    By DIVIDER_SECTION = page.getHomepageMidpointDividerBy();
    By PAGE_DID_LOAD = page.pageLoadedIfDisplayed();

    for (var b : buttons) {
      ButtonNavHelper.verifyButton(driver, homePageUrl, PAGE_DID_LOAD, DIVIDER_SECTION, b, expect,
              defaultWait)
          .ifPresent(failures()::add);
    }
    failures().forEach(logger::warning);
  }

  @Test
  public void verifyAllClientTypeSectionsPresentWithChildElements() {
    for (Map.Entry<By, String> type : page.getHomepageClientTypes().entrySet()) {
      for (WebElement e : page.getChildElements(type.getKey())) {
        checkElement(e::isDisplayed,
            "Client type section " + type.getValue() + "missing one or more child elements");
      }
    }
  }

  @Test
  public void verifyAllEngagementModelsElementsPresent() {
    if (assertOrWarn(displayed(page.getHomepageEngagementModelsHeadingBy()),
        "Engagement models section missing")) {
      for (WebElement e : page.getChildElements(page.getHomepageEngagementModelsSectionBy())) {
        checkElement(e::isDisplayed, "One or more child elements missing");
      }
    }
  }

  @Test
  public void verifyEngagementModelsLearnMoreButtonIsDisplayedAndFunctionsLenient() {
    var buttons = ButtonNavHelper.snapshotButton(driver,
        page.getEngagementModelsLearnMoreButtonBy());
    Map<String, String> expect = Map.of("Learn more", "/services/engagement-models/");

    By ENGAGEMENT_MODELS_SECTION = page.getHomepageEngagementModelsSectionBy();
    By PAGE_DID_LOAD = page.pageLoadedIfDisplayed();

    for (var b : buttons) {
      ButtonNavHelper.verifyButton(driver, homePageUrl, PAGE_DID_LOAD, ENGAGEMENT_MODELS_SECTION, b,
              expect,
              defaultWait)
          .ifPresent(failures()::add);
    }
    failures().forEach(logger::warning);
  }
}