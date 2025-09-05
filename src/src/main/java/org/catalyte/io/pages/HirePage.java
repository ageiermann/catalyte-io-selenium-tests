package org.catalyte.io.pages;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HirePage extends Page {

  // Locators

  private final By textEditors = By.cssSelector(".elementor-widget-text-editor p");
  private final By imageBoxContents = By.cssSelector(".elementor-image-box-content");
  private final By imageBoxText = By.cssSelector(".elementor-image-box-description");
  private final By image3111 = By.cssSelector(".wp-image-3111");
  private final By image3112 = By.cssSelector(".wp-image-3112");
  private final By image3113 = By.cssSelector(".wp-image-3113");
  private final By menuText = By.cssSelector(".menu-text");
  private final By workWithUsButton = By.cssSelector(".elementor-size-sm .elementor-button-text");
  private final By prVideo = By.cssSelector(".elementor-element-f58a359");
  private final By testimonialsVideo = By.cssSelector(".elementor-element-38681986");
  private final By aboutAIButton = By.cssSelector(".elementor-button-link .elementor-size-xs");
  private final By allFaqsButtonDiv = By.cssSelector(".elementor-element-457cc16");

  /* FAQ Accordions */
  private static final By HIRE_FAQ_ACCORDION_ROOT = By.cssSelector("#eael-adv-accordion-493eab1");
  private static final By HIRE_FAQ_HEADERS = By.cssSelector("#eael-adv-accordion-493eab1 .eael-accordion-header");
  private static final By HIRE_FAQ_HEADER_TEXT_SPAN = By.cssSelector(".eael-accordion-tab-title");
  private static final By HIRE_FAQ_SECTION_HEADING = By.xpath("//h3[normalize-space()='Hire Catalyte talent FAQs']");

  public HirePage(WebDriver driver) {
    super(driver);
  }

  // Actions / getters (use Page helpers to avoid NPEs)
  public List<WebElement> getTextEditors() {
    return driver.findElements(textEditors);
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

  //fixing flaky clicks
  public void clickWorkWithUsButton() {
    dismissCookieIfPresent();
    safeClick(workWithUsButton);
  }

  /* === FAQ Accordion Helpers === */

  /** Find the FAQ header by its visible question text (case/space-insensitive). */
  private WebElement findFaqHeaderByText(String question) {
    try {
      WebElement heading = find(HIRE_FAQ_SECTION_HEADING);
      scrollToCenter(heading);
    } catch (Exception ignored) {}

    WebElement root = driver.findElement(HIRE_FAQ_ACCORDION_ROOT);
    var headers = root.findElements(HIRE_FAQ_HEADERS);

    String want = normalize(question);
    return headers.stream()
        .filter(h -> {
          String txt = h.findElement(HIRE_FAQ_HEADER_TEXT_SPAN).getText();
          return normalize(txt).equals(want);
        })
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("FAQ header not found: " + question));
  }
  //Non-flaky replacement for faqContentContains()
  public boolean faqContentContainsAfterAllottedTime(String question, String expected, java.time.Duration timeout) {
    WebElement header = findFaqHeaderByText(question);
    return accordionTextEventuallyContains(header, expected, timeout);
  }

  /* Locators for FAQs Button */
  public WebElement getAllFaqsButton(){
    return find(allFaqsButtonDiv).findElement(By.cssSelector(".elementor-size-xs"));
  }

  public void clickAllFaqsButton(){
    safeClick(getAllFaqsButton());
    dismissCookieIfPresent();
  }
}