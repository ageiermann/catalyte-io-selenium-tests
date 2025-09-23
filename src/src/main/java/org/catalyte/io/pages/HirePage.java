package org.catalyte.io.pages;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;

public class HirePage extends Page {

  // ===== Containers / top-level locators =====
  private static final By PAGE_DID_LOAD = By.cssSelector(".elementor-element-556fd4da");
  private static final By HIRE_FAQ_ACCORDION_ROOT = By.cssSelector("#eael-adv-accordion-493eab1");
  private static final By HIRE_FAQ_HEADERS = By.cssSelector(
      "#eael-adv-accordion-493eab1 .eael-accordion-header");
  private static final By HIRE_FAQ_HEADER_TEXT_SPAN = By.cssSelector(".eael-accordion-tab-title");
  private static final By HIRE_FAQ_SECTION_HEADING = By.xpath(
      "//h3[normalize-space()='Hire Catalyte talent FAQs']");
  private static final By BUTTON_TEXT_ANCHOR = By.cssSelector(".elementor-button-text");
  private static final By BUTTON_ANCHOR = By.cssSelector("a.elementor-button");
  private static final By ALL_FAQS_SECTION = By.cssSelector(".elementor-element-594296b");
  private static final By ALL_FAQS_BUTTON_INNER = By.cssSelector(".elementor-size-xs");
  private static final By ABOUT_AI_SECTION = By.cssSelector(".elementor-element-0e3088f");
  private final By textEditors = By.cssSelector(".elementor-widget-text-editor p");
  private final By imageBoxContents = By.cssSelector(".elementor-image-box-content");
  private final By imageBoxText = By.cssSelector(
      ".elementor-image-box-description"); // (kept for completeness)
  private final By image3111 = By.cssSelector(".wp-image-3111");
  private final By image3112 = By.cssSelector(".wp-image-3112");
  private final By image3113 = By.cssSelector(".wp-image-3113");
  private final By menuText = By.cssSelector(".menu-text");
  // Buttons / videos
  private final By workWithUsButtonScope = By.cssSelector(
      ".elementor-size-sm");           // container for button text
  private final By prVideo = By.cssSelector(".elementor-element-f58a359");
  private final By testimonialsVideo = By.cssSelector(".elementor-element-38681986");
  private final By aboutAIButtonDiv = By.cssSelector(".elementor-element-e3f8064");
  private final By allFaqsButtonDiv = By.cssSelector(".elementor-element-457cc16");

  public HirePage(WebDriver driver) {
    super(driver);
  }

  public By pageLoadedIfDisplayed() {
    return PAGE_DID_LOAD;
  }

  // ===== Simple lists =====
  public List<WebElement> getTextEditors() {
    return driver.findElements(textEditors);
  }

  public List<WebElement> getImageBoxContents() {
    return driver.findElements(imageBoxContents);
  }

  public List<WebElement> getMenuTextItems() {
    return driver.findElements(menuText);
  }

  // ===== Images =====
  public WebElement getImage3111() {
    return driver.findElement(image3111);
  }

  public WebElement getImage3112() {
    return driver.findElement(image3112);
  }

  public WebElement getImage3113() {
    return driver.findElement(image3113);
  }

  // ===== Buttons / videos (driver-scoped, not element.findElement(...)) =====

  /**
   * Button inside a known “small size” scope; uses ByChained to scope and reach the text anchor.
   */
  public WebElement getWorkWithUsButton() {
    return driver.findElement(new ByChained(workWithUsButtonScope, BUTTON_TEXT_ANCHOR));
  }

  public By getWorkWithUsButtonBy() {
    return new ByChained(workWithUsButtonScope, BUTTON_TEXT_ANCHOR);
  }

  public By getWorkWithUsButtonScope() {
    return workWithUsButtonScope;
  }

  public By getAllFaqsSectionBy() {
    return ALL_FAQS_SECTION;
  }

  public WebElement getAboutAIButton() {
    return driver.findElement(new ByChained(aboutAIButtonDiv, BUTTON_ANCHOR));
  }

  public By getAboutAiSectionBy() {
    return ABOUT_AI_SECTION;
  }

  public WebElement getAboutAISection() {
    return driver.findElement(ABOUT_AI_SECTION);
  }

  public By getAboutAIButtonBy() {
    return new ByChained(aboutAIButtonDiv, BUTTON_ANCHOR);
  }

  public By getAboutAIButtonDivBy() {
    return aboutAIButtonDiv;
  }

  public WebElement getPRVideo() {
    return driver.findElement(prVideo);
  }

  public WebElement getTestimonialsVideo() {
    return driver.findElement(testimonialsVideo);
  }

  // ===== FAQ helpers =====

  /**
   * Find the FAQ header by visible text (case/space-insensitive). Uses driver-scoped search +
   * ByChained for header text span (no element-context relative locators).
   */
  private WebElement findFaqHeaderByText(String question) {
    // bring section heading into view if present (optional QoL)
    try {
      WebElement heading = driver.findElement(HIRE_FAQ_SECTION_HEADING);
      scrollToCenter(heading);
    } catch (Exception ignored) {
    }

    String want = normalizer.normalize(question);

    // Get all headers under the accordion root via ByChained
    List<WebElement> headers = driver.findElements(
        new ByChained(HIRE_FAQ_ACCORDION_ROOT, HIRE_FAQ_HEADERS));

    return headers.stream()
        .filter(h -> {
          // scope to the title span inside each header
          WebElement titleSpan = h.findElement(HIRE_FAQ_HEADER_TEXT_SPAN);
          String txt = normalizer.normalize(titleSpan.getText());
          return txt.equals(want);
        })
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("FAQ header not found: " + question));
  }

  /**
   * Wait until the accordion content under a given header eventually contains expected text.
   * Delegates to Page helper: accordionTextEventuallyContains(WebElement, String, Duration)
   */
  public boolean faqContentContainsAfterAllottedTime(String question, String expected,
      Duration timeout) {
    WebElement header = findFaqHeaderByText(question);
    return accordionTextEventuallyContains(header, expected, timeout);
  }

  // ===== All FAQs button (scoped with ByChained) =====
  public WebElement getAllFaqsButton() {
    return driver.findElement(new ByChained(allFaqsButtonDiv, ALL_FAQS_BUTTON_INNER));
  }

  public By getAllFaqsButtonBy() {
    return new ByChained(allFaqsButtonDiv, ALL_FAQS_BUTTON_INNER);
  }
}