package org.catalyte.io.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HomePage extends Page {

  //TODO: add accordion checks/tests

  private final By accordionTalent = By.id("what-if-i-need-more-than-apprentice-talent");
  private final By industriesAccordion = By.id("what-industries-do-you-provide-talent-for");
  private final By hireAccordion = By.id("how-can-i-hire-catalyte-talent");

  public HomePage(WebDriver driver) {
    super(driver);
  }

  public boolean isAccordionTalentVisible() {
    return driver.findElement(accordionTalent).isDisplayed();
  }

  public boolean isIndustriesVisible() {
    return driver.findElement(industriesAccordion).isDisplayed();
  }

  public boolean isHireAccordionVisible() {
    return driver.findElement(hireAccordion).isDisplayed();
  }
}