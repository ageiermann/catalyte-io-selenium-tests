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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/* Page object class for the Get Hired (Apprenticeships) page. */
public class ApprenticeshipsPage extends Page {

  // Title anchors inside each Elementor loop item for apprenticeships
  private static final By APPRENTICESHIP_TITLE_LINKS =
      By.cssSelector(
          "[data-elementor-type='loop-item'].type-apprenticeships h4.elementor-heading-title a");
  // Optional: the “View apprenticeship” action anchors
  private static final By APPRENTICESHIP_CTA_LINKS =
      By.cssSelector(
          "[data-elementor-type='loop-item'].type-apprenticeships .elementor-icon-box-title a");
  // Page headings used to confirm navigation
  private static final By H1 = By.tagName("h1");
  private static final By H2 = By.tagName("h2");
  private final WebDriver driver;
  private final WebDriverWait wait;

  public ApprenticeshipsPage(WebDriver driver) {
    super(driver);
    this.driver = driver;
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
  }

  private static String slugify(String txt) {
    // very small slug helper for this site’s patterns
    String s = txt.toLowerCase(Locale.ROOT).trim();
    s = s.replace("&", "and").replace("+", "plus");
    s = s.replaceAll("[^a-z0-9\\s-]", "");
    s = s.replaceAll("\\s+", "-");
    s = s.replaceAll("-{2,}", "-");
    return s;
  }

  /**
   * Wait until at least N apprenticeship title links exist.
   */
  public void waitForCards(int minCount) {
    wait.until(d -> {
      List<WebElement> els = d.findElements(APPRENTICESHIP_TITLE_LINKS);
      return els.size() >= minCount ? els : null;
    });
  }

  /**
   * Return the visible apprenticeship names from card titles.
   */
  public List<String> getApprenticeshipNames() {
    waitForCards(2); // there are several; 2 is a safe minimum
    return driver.findElements(APPRENTICESHIP_TITLE_LINKS)
        .stream()
        .map(WebElement::getText)
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .distinct()
        .collect(Collectors.toList());
  }

  /**
   * Click a card by its visible title (case-insensitive).
   */
  public void clickApprenticeshipType(String name) {
    String target = name.trim().toLowerCase(Locale.ROOT);

    // Prefer the title anchors
    List<WebElement> titleLinks = driver.findElements(APPRENTICESHIP_TITLE_LINKS);
    WebElement match = titleLinks.stream()
        .filter(a -> a.getText().trim().toLowerCase(Locale.ROOT).equals(target))
        .findFirst()
        .orElse(null);

    // Fallback to the action anchors if needed
    if (match == null) {
      List<WebElement> ctas = driver.findElements(APPRENTICESHIP_CTA_LINKS);
      match = ctas.stream()
          .filter(a -> {
            // action link text is "View apprenticeship", so compare by href ending if titles failed
            String href = a.getAttribute("href");
            return href != null && href.toLowerCase(Locale.ROOT)
                .endsWith("/" + slugify(target) + "/");
          })
          .findFirst()
          .orElse(null);
    }

    if (match == null) {
      throw new NoSuchElementException("No apprenticeship card found for: " + name);
    }

    // Scroll then click (with JS fallback)
    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});",
        match);
    try {
      match.click();
    } catch (ElementClickInterceptedException e) {
      ((JavascriptExecutor) driver).executeScript("arguments[0].click();", match);
    }
  }

  /**
   * Verify we navigated to the correct apprenticeship detail page.
   */
  public boolean verifyApprenticeshipPage(String name) {
    String target = name.trim().toLowerCase(Locale.ROOT);

    // Wait for any H1/H2 to render
    wait.until(ExpectedConditions.or(
        ExpectedConditions.presenceOfElementLocated(H1),
        ExpectedConditions.presenceOfElementLocated(H2)
    ));

    // Check heading contains the target (loose match) OR URL contains slug
    String h1 = driver.findElements(H1).stream().findFirst()
        .map(e -> e.getText().trim().toLowerCase(Locale.ROOT)).orElse("");
    String h2 = driver.findElements(H2).stream().findFirst()
        .map(e -> e.getText().trim().toLowerCase(Locale.ROOT)).orElse("");
    String url = driver.getCurrentUrl().toLowerCase(Locale.ROOT);

    boolean headingOk = h1.contains(target) || h2.contains(target);
    boolean urlOk =
        url.endsWith("/" + slugify(target) + "/") || url.contains("/" + slugify(target) + "/");

    return headingOk || urlOk;
  }

  /**
   * Re-wait for the cards after navigating back.
   */
  public void waitUntilBackOnCards() {
    waitForCards(2);
  }
}