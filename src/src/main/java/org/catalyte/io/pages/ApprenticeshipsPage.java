package org.catalyte.io.pages;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * "Get Hired" / Apprenticeships Page object.
 * **/
public class ApprenticeshipsPage extends Page {

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

  private static final By CARD_TITLE_LINKS = By.cssSelector(
      "[data-elementor-type='loop-item'].type-apprenticeships h4.elementor-heading-title a"
  );

  /* ===== Destination page headings ===== */
  private static final By H1 = By.tagName("h1");
  private static final By H2 = By.tagName("h2");

  protected WebDriver driver;
  protected WebDriverWait wait;

  public ApprenticeshipsPage(WebDriver driver) {
    super(driver);
    this.driver = driver;
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
  }

  public List<WebElement> getIconBoxTitles() { return driver.findElements(iconBoxTitles); }
  public WebElement getWpImage2230() { return driver.findElement(wpImage2230); }
  public List<WebElement> getElement4b74d2c3SizeDefault() { return driver.findElements(element4b74d2c3SizeDefault); }
  public List<WebElement> getElement2570aea1ImageBoxWrappers() { return driver.findElements(element2570aea1ImageBoxWrapper); }
  public List<WebElement> getElement3d21807cImageBoxWrappers() { return driver.findElements(element3d21807cImageBoxWrapper); }
  public List<WebElement> getImageBoxTitles() { return driver.findElements(imageBoxTitles); }

  /* ===== Card list helpers ===== */

  /** Wait until at least N apprenticeship cards visible (title anchors). */
  public void waitForCards(int minCount) {
    wait.until(d -> {
      List<WebElement> els = d.findElements(CARD_TITLE_LINKS);
      return els.size() >= minCount ? els : null;
    });
  }

  /** Returns visible apprenticeship names from the card titles. */
  public List<String> getApprenticeshipNames() {
    waitForCards(2); // safe minimum
    return driver.findElements(CARD_TITLE_LINKS).stream()
        .map(WebElement::getText)
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .distinct()
        .collect(Collectors.toList());
  }

  /** Click a specific apprenticeship card by its visible title (case-insensitive). */
  public void clickApprenticeshipType(String name) {
    String target = name.trim().toLowerCase(Locale.ROOT);

    WebElement link = driver.findElements(CARD_TITLE_LINKS).stream()
        .filter(a -> a.getText().trim().toLowerCase(Locale.ROOT).equals(target))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("No apprenticeship card titled: " + name));

    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", link);
    try {
      link.click();
    } catch (ElementClickInterceptedException e) {
      ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
    }
  }

  /** Verify browser is on right apprenticeship detail page (heading or URL slug match). */
  public boolean verifyApprenticeshipPage(String expectedName) {
    wait.until(ExpectedConditions.or(
        ExpectedConditions.presenceOfElementLocated(H1),
        ExpectedConditions.presenceOfElementLocated(H2)
    ));

    String expected = expectedName.trim().toLowerCase(Locale.ROOT);
    String h1 = driver.findElements(H1).stream().findFirst().map(e -> e.getText().trim().toLowerCase(Locale.ROOT)).orElse("");
    String h2 = driver.findElements(H2).stream().findFirst().map(e -> e.getText().trim().toLowerCase(Locale.ROOT)).orElse("");
    String url = driver.getCurrentUrl().toLowerCase(Locale.ROOT);

    boolean headingOk = h1.contains(expected) || h2.contains(expected);
    boolean urlOk = url.contains("/" + slugify(expected) + "/");
    return headingOk || urlOk;
  }

  /** After navigate().back(), re-wait for the cards. */
  public void waitUntilBackOnCards() {
    waitForCards(2);
  }

  private static String slugify(String txt) {
    String s = txt.toLowerCase(Locale.ROOT).trim();
    s = s.replace("&", "and").replace("+", "plus");
    s = s.replaceAll("[^a-z0-9\\s-]", "");
    s = s.replaceAll("\\s+", "-");
    s = s.replaceAll("-{2,}", "-");
    return s;
  }
}