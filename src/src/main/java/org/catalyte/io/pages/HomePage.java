package org.catalyte.io.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HomePage extends Page {

  //TODO: add section checks/tests

  private final By accordionTalent = By.id("what-if-i-need-more-than-apprentice-talent");
  private final By industriesAccordion = By.id("what-industries-do-you-provide-talent-for");
  private final By hireAccordion = By.id("how-can-i-hire-catalyte-talent");
  private final By homepageHeadingSection = By.cssSelector(".elementor-element-3d4a7b62");

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
}