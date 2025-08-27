package org.catalyte.io.pages;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * "Get Hired" Page object class.
 */
public class HirePage extends Page{

  /* Locators */

  // Hero / text editor sections
  private final By textEditors = By.cssSelector(".elementor-widget-text-editor p");
  // Accordions
  private final By accordionTalent = By.cssSelector(
      "#what-if-i-need-more-than-apprentice-talent .eael-accordion-tab-title");
  private final By accordionIndustries = By.cssSelector(
      "#what-industries-do-you-provide-talent-for .eael-accordion-tab-title");
  private final By accordionHire = By.cssSelector(
      "#how-can-i-hire-catalyte-talent .eael-accordion-tab-title");
  // Image box content (these usually group image + title + description)
  private final By imageBoxContents = By.cssSelector(".elementor-image-box-content");
  private final By imageBoxText = By.cssSelector(".elementor-image-box-description");
  // Specific images (stable class names only)
  private final By image3111 = By.cssSelector(".wp-image-3111");
  private final By image3112 = By.cssSelector(".wp-image-3112");
  private final By image3113 = By.cssSelector(".wp-image-3113");
  // Menu text
  private final By menuText = By.cssSelector(".menu-text");
  // Main workflow button
  private final By workWithUsButton = By.cssSelector(".elementor-size-sm .elementor-button-text");
  // Apprenticeships PR video
  private final By prVideo = By.cssSelector(".elementor-element-f58a359");
  // Apprenticeships testimonials video
  private final By testimonialsVideo = By.cssSelector(".elementor-element-38681986");
  // Secondary workflow button
  private final By aboutAIButton = By.cssSelector(".elementor-button-link elementor-size-xs");

  protected WebDriver driver;
  protected WebDriverWait wait;

  public HirePage(WebDriver driver) {
    super(driver);
    this.driver = driver;
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
  }

  /* Actions and Assertions */

  // Get all paragraph texts from text editor widgets
  public List<WebElement> getTextEditors() {
    return driver.findElements(textEditors);
  }

  // Accordions
  public WebElement getAccordionTalent() {
    return driver.findElement(accordionTalent);
  }
  public WebElement getAccordionIndustries() {
    return driver.findElement(accordionIndustries);
  }
  public WebElement getAccordionHire() {
    return driver.findElement(accordionHire);
  }

  // Image box contents
  public List<WebElement> getImageBoxContents() {
    return driver.findElements(imageBoxContents);
  }

  // Specific images
  public boolean isImage3111Displayed() {
    return driver.findElement(image3111).isDisplayed();
  }
  public boolean isImage3112Displayed() {
    return driver.findElement(image3112).isDisplayed();
  }
  public boolean isImage3113Displayed() {
    return driver.findElement(image3113).isDisplayed();
  }

  // Menu text
  public List<WebElement> getMenuTextItems() {
    return driver.findElements(menuText);
  }

  // Workflow buttons
  public WebElement getWorkWithUsButton() {
    return driver.findElement(workWithUsButton);
  }
  public WebElement getAboutAIButton() {
    return driver.findElement(aboutAIButton);
  }

  //videos
  public WebElement getPRVideo() {
    return driver.findElement(prVideo);
  }
  public WebElement getTestimonialsVideo() {
    return driver.findElement(testimonialsVideo);
  }
}