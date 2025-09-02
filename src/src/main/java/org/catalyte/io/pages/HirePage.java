package org.catalyte.io.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HirePage extends Page {

  // Locators
  private final By textEditors = By.cssSelector(".elementor-widget-text-editor p");
  private final By accordionTalent = By.cssSelector(
      "#what-if-i-need-more-than-apprentice-talent .eael-accordion-tab-title");
  private final By accordionIndustries = By.cssSelector(
      "#what-industries-do-you-provide-talent-for .eael-accordion-tab-title");
  private final By accordionHire = By.cssSelector(
      "#how-can-i-hire-catalyte-talent .eael-accordion-tab-title");
  private final By imageBoxContents = By.cssSelector(".elementor-image-box-content");
  private final By imageBoxText = By.cssSelector(".elementor-image-box-description");
  private final By image3111 = By.cssSelector(".wp-image-3111");
  private final By image3112 = By.cssSelector(".wp-image-3112");
  private final By image3113 = By.cssSelector(".wp-image-3113");
  private final By menuText = By.cssSelector(".menu-text");
  private final By workWithUsButton = By.cssSelector(".elementor-size-sm .elementor-button-text");
  private final By prVideo = By.cssSelector(".elementor-element-f58a359");
  private final By testimonialsVideo = By.cssSelector(".elementor-element-38681986");
  private final By aboutAIButton = By.cssSelector(".elementor-button-link.elementor-size-xs");

  public HirePage(WebDriver driver) {
    super(driver);
  }

  // Actions / getters (use Page helpers to avoid NPEs)
  public List<WebElement> getTextEditors() {
    return driver.findElements(textEditors);
  }

  public WebElement getAccordionTalent() {
    return find(accordionTalent);
  }

  public WebElement getAccordionIndustries() {
    return find(accordionIndustries);
  }

  public WebElement getAccordionHire() {
    return find(accordionHire);
  }

  public List<WebElement> getImageBoxContents() {
    return driver.findElements(imageBoxContents);
  }

  public boolean isImage3111Displayed() {
    return find(image3111).isDisplayed();
  }

  public boolean isImage3112Displayed() {
    return find(image3112).isDisplayed();
  }

  public boolean isImage3113Displayed() {
    return find(image3113).isDisplayed();
  }

  public List<WebElement> getMenuTextItems() {
    return driver.findElements(menuText);
  }

  public WebElement getWorkWithUsButton() {
    return find(workWithUsButton);
  }

  public WebElement getAboutAIButton() {
    return find(aboutAIButton);
  }

  public WebElement getPRVideo() {
    return find(prVideo);
  }

  public WebElement getTestimonialsVideo() {
    return find(testimonialsVideo);
  }
}