package org.catalyte.io.pages;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * "Get Hired" / Apprenticeships Page object.
 **/
public class ApprenticeshipsPage extends Page {

  private static final By CARD_TITLE_LINKS = By.cssSelector(
      "[data-elementor-type='loop-item'].type-apprenticeships h4.elementor-heading-title a"
  );
  private static final By FAQ_SECTION = By.cssSelector("#faqs");
  private static final By ACCORDION_HEADERS = By.cssSelector(
      "#eael-adv-accordion-32775cf4 .eael-accordion-header");
  /* ===== Destination page headings ===== */
  private static final By H1 = By.tagName("h1");
  private static final By H2 = By.tagName("h2");
  /* ===== How It Works locators ===== */
  private final By iconBoxTitles = By.cssSelector(".elementor-icon-box-title");
  private final By wpImage2230 = By.cssSelector(".wp-image-2230");
  private final By element4b74d2c3SizeDefault =
      By.cssSelector(".elementor-element-4b74d2c3 .elementor-size-default");
  private final By element2570aea1ImageBoxWrapper =
      By.cssSelector(".elementor-element-2570aea1 .elementor-image-box-wrapper");
  private final By element3d21807cImageBoxWrapper =
      By.cssSelector(".elementor-element-3d21807c .elementor-image-box-wrapper");
  private final By imageBoxTitles = By.cssSelector(".elementor-image-box-title");
  /**
   * FAQ accordions
   **/
  private final By accordionHowItWorks = By.cssSelector(
      "#how-does-a-catalyte-apprenticeship-work .eael-accordion-tab-title");
  private final By accordionCost = By.cssSelector(
      "#how-much-does-an-apprenticeship-cost .eael-accordion-tab-title");
  private final By accordionCommitment = By.cssSelector(
      "#are-apprenticeships-a-full-time-or-part-time-commitment .eael-accordion-tab-title");
  private final By accordionNeedDegree = By.cssSelector(
      "#do-i-need-a-college-degree-to-apply .eael-accordion-tab-title");
  private final By accordionApplyRequirements = By.cssSelector(
      "#what-are-the-requirements-to-apply .eael-accordion-tab-title");

  public ApprenticeshipsPage(WebDriver driver) {
    super(driver);
  }

  private static String slugify(String txt) {
    String s = txt.toLowerCase(Locale.ROOT).trim();
    s = s.replace("&", "and").replace("+", "plus");
    s = s.replaceAll("[^a-z0-9\\s-]", "");
    s = s.replaceAll("\\s+", "-");
    s = s.replaceAll("-{2,}", "-");
    return s;
  }

  public List<WebElement> getIconBoxTitles() {
    return driver.findElements(iconBoxTitles);
  }

  public WebElement getWpImage2230() {
    return driver.findElement(wpImage2230);
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
  /* === FAQ Accordion Helpers === */

  public List<WebElement> getOpportunityMetrics() {
    WebElement opportunityMetricsSection = driver.findElement(
        By.cssSelector(".elementor-element-269de2dc"));
    return opportunityMetricsSection.findElements(By.cssSelector(".elementor-widget-heading"));
  }

  /**
   * Find the FAQ header by its visible text (case-insensitive).
   */
  private org.openqa.selenium.WebElement findFaqHeaderByText(String question) {
    try {
      org.openqa.selenium.WebElement section = driver.findElement(FAQ_SECTION);
      ((org.openqa.selenium.JavascriptExecutor) driver)
          .executeScript("arguments[0].scrollIntoView({block:'center'});", section);
    } catch (org.openqa.selenium.NoSuchElementException ignored) {}

    return driver.findElements(ACCORDION_HEADERS).stream()
        .filter(h -> normalize(h.getText()).equals(normalize(question)))
        .findFirst()
        .orElseThrow(() -> new org.openqa.selenium.NoSuchElementException("FAQ header not found: " + question));
  }
  //Non-flaky replacement for faqContentContains()
  public boolean faqContentContainsAfterAllottedTime(String question, String expected, java.time.Duration timeout) {
    org.openqa.selenium.WebElement header = findFaqHeaderByText(question);
    return accordionTextEventuallyContains(header, expected, timeout);
  }

  /**
   * Expand a FAQ by its question text and return the visible content panel element.
   */
  public WebElement expandFaq(String question) {
    WebElement header = findFaqHeaderByText(question);
    return expandAccordionByAriaControls(header); // uses base Page helper
  }

  /* ===== Card list helpers ===== */

  /**
   * FLAKY: Expand a FAQ and check that its content contains the expected text.
   */
  public boolean faqContentContains(String question, String expectedSubstring) {
    WebElement panel = expandFaq(question);
    return panel.getText().toLowerCase().contains(expectedSubstring.toLowerCase());
  }

  /**
   * Wait until at least N apprenticeship cards visible (title anchors).
   */
  public void waitForCards(int minCount) {
    wait.until(d -> {
      List<WebElement> els = d.findElements(CARD_TITLE_LINKS);
      return els.size() >= minCount ? els : null;
    });
  }

  /**
   * Returns visible apprenticeship names from the card titles.
   */
  public List<String> getApprenticeshipNames() {
    waitForCards(2); // safe minimum
    return driver.findElements(CARD_TITLE_LINKS).stream()
        .map(WebElement::getText)
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .distinct()
        .collect(Collectors.toList());
  }

  /**
   * Click a specific apprenticeship card by its visible title (case-insensitive).
   */
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

  /**
   * Verify browser is on right apprenticeship detail page (heading or URL slug match).
   */
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
    boolean urlOk = url.contains("/" + slugify(expected) + "/");
    return headingOk || urlOk;
  }

  /**
   * After navigate().back(), re-wait for the cards.
   */
  public void waitUntilBackOnCards() {
    waitForCards(2);
  }
}