package org.catalyte.io.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.locators.RelativeLocator;

public class HomePage extends Page {

  private final By accordionTalent = By.id("what-if-i-need-more-than-apprentice-talent");
  private final By industriesAccordion = By.id("what-industries-do-you-provide-talent-for");
  private final By hireAccordion = By.id("how-can-i-hire-catalyte-talent");

  private final By homepageHeadingSection = By.cssSelector(".elementor-element-3d4a7b62");

  private final By homepageInfoboxesSectionTop = By.cssSelector(".elementor-element-3447365");
  private final By homepageInfoboxesSectionBottom = By.cssSelector(".elementor-element-b69b593");
  private final By homepageMidpointDivider = By.cssSelector(".elementor-element-500b262f");

  private final By homepageClientTypeSectionHeading = By.cssSelector(".elementor-element-dc546c3");
  private final By homepageClientTypeSectionEnterprise = By.cssSelector(".elementor-element-13797889");
  private final By homepageClientTypeSectionGovernment = By.cssSelector(".elementor-element-47e9e0f7");
  private final By homepageClientTypeSectionStartups = By.cssSelector(".elementor-element-783863a");
  private final By homepageClientTypeSectionPrivateEquity = By.cssSelector(".elementor-element-50afc0c");

  private final By homepageEngagementModelsHeading = By.cssSelector(".elementor-element-635718f9");
  private final By homepageEngagementModelsSection = By.cssSelector(".elementor-element-1b51c35d");

  public HomePage(WebDriver driver) {
    super(driver);
  }

  public WebElement getAccordionTalent() {
    return driver.findElement(accordionTalent);
  }

  public WebElement getAccordionIndustries() {
    return driver.findElement(industriesAccordion);
  }

  public WebElement getAccordionHire() {
    return driver.findElement(hireAccordion);
  }

  public List<WebElement> getHomepageHeadingElements() {
    return driver.findElement(homepageHeadingSection).findElements(By.cssSelector(".elementor-element"));
  }

  public WebElement getGetStartedButton(){
    return driver.findElement(homepageHeadingSection).findElement(By.cssSelector(".elementor-button"));
  }

  public void clickGetStartedButton(){
    safeClick(getGetStartedButton());
    dismissCookieIfPresent();
  }

  public List<WebElement> getTopHomepageInfoboxes() {
    return driver.findElement(homepageInfoboxesSectionTop).findElements(By.cssSelector(".elementor-column"));
  }

  public List<WebElement> getBottomHomepageInfoboxes() {
    return driver.findElement(homepageInfoboxesSectionBottom).findElements(By.cssSelector(".elementor-column"));
  }

  public WebElement getPartnerWithUsButton() {
    return driver.findElement(homepageMidpointDivider)
        .findElement(By.cssSelector(".elementor-widget-button")).findElement(By.cssSelector(".elementor-button"));
  }

  public void clickPartnerWithUsButton() {
    safeClick(getPartnerWithUsButton());
    dismissCookieIfPresent();
  }

  public WebElement getHomepageClientTypeHeading() {
    return driver.findElement(homepageClientTypeSectionHeading);
  }

  public WebElement getHomepageClientTypeEnterprise() {
    return driver.findElement(homepageClientTypeSectionEnterprise);
  }

  public WebElement getHomepageClientTypeGovernment() {
    return driver.findElement(homepageClientTypeSectionGovernment);
  }

  public WebElement getHomepageClientTypeStartups() {
    return driver.findElement(homepageClientTypeSectionStartups);
  }

  public WebElement getHomepageClientTypePrivateEquity() {
    return driver.findElement(homepageClientTypeSectionPrivateEquity);
  }

  public WebElement getHomepageEngagementModelsHeading() {
    return driver.findElement(homepageEngagementModelsHeading);
  }

  public WebElement getHomepageEngagementModelsSection() {
    return driver.findElement(homepageEngagementModelsSection);
  }

  public WebElement getEngagementModelsLearnMoreButton() {
    return driver.findElement(RelativeLocator.with(By.cssSelector(".elementor-button"))
        .below(getHomepageEngagementModelsSection()));
  }

  public void clickEngagementModelsButton() {
    safeClick(getEngagementModelsLearnMoreButton());
    dismissCookieIfPresent();
  }

  //Helper method for client type and engagement sections
  public List<WebElement> getChildElements(WebElement section){
    return section.findElements(By.cssSelector(".elementor-element"));
  }
}