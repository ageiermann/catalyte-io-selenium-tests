package org.catalyte.io.pages;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * "Get Hired" / Apprenticeships Page object.
 */
public class ApprenticeshipsPage extends Page {

  /* ===== Headings ===== */
  private static final By H1 = By.tagName("h1");
  private static final By H2 = By.tagName("h2");
  private static final By PAGE_DID_LOAD = By.cssSelector(".elementor-element-33263002");

  /* ===== Apprenticeship cards ===== */
  private static final By CARD_TITLE_LINKS = By.cssSelector(
      "[data-elementor-type='loop-item'].type-apprenticeships h4.elementor-heading-title a");

  /* ===== FAQ accordions ===== */
  private static final By FAQ_SECTION = By.cssSelector("#faqs");
  private static final By FAQ_SECTION_CONTAINER = By.cssSelector(".elementor-element-29810855");
  private static final By FAQ_ACCORDION_ROOT = By.cssSelector("#eael-adv-accordion-32775cf4");
  private static final By FAQ_HEADERS = By.cssSelector(".eael-accordion-header");
  private static final By ALL_FAQS_INNER = By.cssSelector(".elementor-size-xs");
  /* ===== How It Works ===== */
  private final By iconBoxTitles = By.cssSelector(".elementor-icon-box-title");
  private final By wpImage2230 = By.cssSelector(".wp-image-2230");
  private final By element4b74d2c3SizeDefault =
      By.cssSelector(".elementor-element-4b74d2c3 .elementor-size-default");
  private final By element2570aea1ImageBoxWrapper =
      By.cssSelector(".elementor-element-2570aea1 .elementor-image-box-wrapper");
  private final By element3d21807cImageBoxWrapper =
      By.cssSelector(".elementor-element-3d21807c .elementor-image-box-wrapper");
  private final By imageBoxTitles = By.cssSelector(".elementor-image-box-title");
  /* ===== All FAQs button ===== */
  private final By allFaqsButtonDiv = By.cssSelector(".elementor-element-c47f786");

  public ApprenticeshipsPage(WebDriver driver) {
    super(driver);
  }

  public By pageLoadedIfDisplayed() {
    return PAGE_DID_LOAD;
  }

  // ==== Elements ====
  public WebElement getWpImage2230() {
    return driver.findElement(wpImage2230);
  }

  public List<WebElement> getIconBoxTitles() {
    return driver.findElements(iconBoxTitles);
  }

  public List<WebElement> getElement4b74d2c3SizeDefault() {
    return driver.findElements(element4b74d2c3SizeDefault);
  }

  public List<WebElement> getElement2570aea1ImageBoxWrappers() {
    return driver.findElements(element2570aea1ImageBoxWrapper);
  }

  public List<WebElement> getElement3d21807cImageBoxWrappers() {
    return driver.findElements(element3d21807cImageBoxWrapper);
  }

  public List<WebElement> getImageBoxTitles() {
    return driver.findElements(imageBoxTitles);
  }

  /**
   * Example of scoping with ByChained instead of element.findElements(...)
   */
  public List<WebElement> getOpportunityMetrics() {
    By section = By.cssSelector(".elementor-element-269de2dc");
    return driver.findElements(new ByChained(section, By.cssSelector(".elementor-widget-heading")));
  }

  // ==== FAQ helpers ====
  private WebElement findFaqHeaderByText(String question) {
    try {
      WebElement section = driver.findElement(FAQ_SECTION);
      ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});",
          section);
    } catch (NoSuchElementException ignored) {
    }

    String want = normalizer.normalize(question);

    List<WebElement> headers = driver.findElements(new ByChained(FAQ_ACCORDION_ROOT, FAQ_HEADERS));
    return headers.stream()
        .filter(h -> normalizer.normalize(h.getText()).equals(want))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("FAQ header not found: " + question));
  }

  public boolean faqContentContainsAfterAllottedTime(String question, String expected,
      Duration timeout) {
    WebElement header = findFaqHeaderByText(question);
    return accordionTextEventuallyContains(header, expected, timeout);
  }

  public WebElement expandFaq(String question) {
    WebElement header = findFaqHeaderByText(question);
    return expandAccordionByAriaControls(header);
  }

  public By getAllFaqsButtonBy() {
    return new ByChained(allFaqsButtonDiv, ALL_FAQS_INNER);
  }

  public By getFaqSectionBy() {
    return FAQ_SECTION_CONTAINER;
  }

  // ==== Apprenticeship cards ====
  public void waitForCards(int minCount) {
    wait.until(d -> {
      List<WebElement> els = d.findElements(CARD_TITLE_LINKS);
      return els.size() >= minCount ? els : null;
    });
  }

  public List<String> getApprenticeshipNames() {
    waitForCards(2);
    return driver.findElements(CARD_TITLE_LINKS).stream()
        .map(WebElement::getText)
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .distinct()
        .collect(Collectors.toList());
  }

  public void clickApprenticeshipType(String name) {
    String target = name.trim().toLowerCase(Locale.ROOT);
    WebElement link = driver.findElements(CARD_TITLE_LINKS).stream()
        .filter(a -> a.getText().trim().toLowerCase(Locale.ROOT).equals(target))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("No apprenticeship card titled: " + name));

    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});",
        link);
    try {
      link.click();
    } catch (ElementClickInterceptedException e) {
      ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
    }
  }

  public boolean verifyApprenticeshipPage(String expectedName) {
    wait.until(ExpectedConditions.or(
        ExpectedConditions.presenceOfElementLocated(H1),
        ExpectedConditions.presenceOfElementLocated(H2)
    ));

    String expected = expectedName.trim().toLowerCase(Locale.ROOT);
    String h1 = driver.findElements(H1).stream().findFirst()
        .map(e -> e.getText().trim().toLowerCase(Locale.ROOT)).orElse("");
    String h2 = driver.findElements(H2).stream().findFirst()
        .map(e -> e.getText().trim().toLowerCase(Locale.ROOT)).orElse("");
    String url = driver.getCurrentUrl().toLowerCase(Locale.ROOT);

    boolean headingOk = h1.contains(expected) || h2.contains(expected);
    boolean urlOk = url.contains("/" + normalizer.slugify(expected) + "/");
    return headingOk || urlOk;
  }

  public void waitUntilBackOnCards() {
    waitForCards(2);
  }

  // Fallback flaky method
  @Deprecated
  public boolean faqContentContains(String question, String expectedSubstring) {
    WebElement panel = expandFaq(question);
    return panel.getText().toLowerCase().contains(expectedSubstring.toLowerCase());
  }
}