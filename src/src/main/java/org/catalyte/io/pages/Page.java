package org.catalyte.io.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Page {

  protected final WebDriver driver;
  protected final WebDriverWait wait;

  public Page(WebDriver driver) {
    if (driver == null) {
      throw new IllegalArgumentException("WebDriver must not be null");
    }
    this.driver = driver;
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
  }

  protected WebElement find(By locator) {
    return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
  }

  protected void click(By locator) {
    find(locator).click();
  }

  protected void type(By locator, String text) {
    var e = find(locator);
    e.clear();
    e.sendKeys(text);
  }

  protected String getText(By locator) {
    return find(locator).getText();
  }

  public void playVimeoVideo(String widgetCssLocator) throws InterruptedException {
    WebElement overlay = wait.until(ExpectedConditions.elementToBeClickable(
        By.cssSelector(widgetCssLocator + " .elementor-wrapper")));
    overlay.click();

    WebElement vimeoFrame = wait.until(ExpectedConditions.presenceOfElementLocated(
        By.cssSelector(widgetCssLocator + " iframe.elementor-video-iframe")));
    driver.switchTo().frame(vimeoFrame);

    WebElement playButton = wait.until(
        ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Play']")));
    playButton.click();

    Thread.sleep(2000);
    WebElement pauseButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
        By.cssSelector("button[aria-label='Pause']")));
    if (!pauseButton.isDisplayed()) {
      throw new AssertionError("Video did not start playing!");
    }
    driver.switchTo().defaultContent();
  }

  /** Helper methods to fix flaky clicks. **/
  /* ------------------------------------- */

  /**
   * Scroll element to center of viewport.
   */
  protected void scrollToCenter(WebElement el) {
    ((JavascriptExecutor) driver).executeScript(
        "const r=arguments[0].getBoundingClientRect();" +
            "window.scrollBy({top: r.top - (window.innerHeight/2) + r.height/2, left: 0, behavior: 'instant'});",
        el
    );
  }

  /**
   * Try a real click; fall back to JS click if intercepted; retries once.
   */
  protected void safeClick(By locator) {
    // wait for presence & visibility & clickability
    WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));

    // bring to a safe position (avoid sticky header/footers)
    scrollToCenter(el);

    // small settle to let CSS/animation finish
    try {
      Thread.sleep(75);
    } catch (InterruptedException ignored) {
    }

    try {
      el.click();
    } catch (ElementClickInterceptedException e) {
      // one more scroll + move + JS click fallback
      try {
        scrollToCenter(el);
        new Actions(driver).moveToElement(el).pause(java.time.Duration.ofMillis(50)).perform();
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
      } catch (Exception e2) {
        throw e; // surface the original intercepted error if JS click also fails
      }
    }
  }

  protected void dismissCookieIfPresent() {
    try {
      WebElement accept = driver.findElement(By.cssSelector(
          "#onetrust-accept-btn-handler, .ot-sdk-container [data-handler='accept']"));
      if (accept.isDisplayed()) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", accept);
        Thread.sleep(100); // brief settle
      }
    } catch (NoSuchElementException | StaleElementReferenceException |
             InterruptedException ignored) {
    }
  }

  /**
   * Click a specific WebElement safely: centers, retries with JS on intercept.
   */
  protected void safeClick(WebElement el) {
    scrollToCenter(el);
    try {
      Thread.sleep(60);
    } catch (InterruptedException ignored) {
    }
    try {
      el.click();
    } catch (ElementClickInterceptedException e) {
      try {
        scrollToCenter(el);
        new Actions(driver).moveToElement(el).pause(Duration.ofMillis(40)).perform();
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
      } catch (Exception e2) {
        throw e; // bubble original intercepted click if JS also failed
      }
    }
  }

  /**
   * Generic accordion expander when header and locator are known. Clicks header and waits for the
   * panel to be visible and (optionally) non-empty.
   */
  protected WebElement expandAccordion(WebElement headerEl, By panelLocator, boolean requireText) {
    safeClick(headerEl);
    return wait.until(d -> {
      WebElement panel = d.findElement(panelLocator);
      if (!panel.isDisplayed()) {
        return null;
      }
      if (requireText && panel.getText().trim().isEmpty()) {
        return null;
      }
      return panel;
    });
  }

  /**
   * Generic accordion expander when header's aria-controls points to the panel id. Clicks header,
   * resolves panel via aria-controls, then waits for visibility (+text if required).
   */
  protected WebElement expandAccordionByAriaControls(WebElement headerEl, boolean requireText) {
    String controlsId = headerEl.getAttribute("aria-controls");
    if (controlsId == null || controlsId.isBlank()) {
      throw new IllegalStateException("Accordion header has no aria-controls attribute");
    }
    By panelBy = By.id(controlsId.trim());
    return expandAccordion(headerEl, panelBy, requireText);
  }

  /**
   * Convenience overloads (default: require non-empty text).
   */
  protected WebElement expandAccordion(WebElement headerEl, By panelLocator) {
    return expandAccordion(headerEl, panelLocator, true);
  }

  protected WebElement expandAccordionByAriaControls(WebElement headerEl) {
    return expandAccordionByAriaControls(headerEl, true);
  }

  /**
   * Assert-like helper: expand via aria-controls and check content contains a substring.
   */
  protected boolean accordionContains(WebElement headerEl, String expectedSubstring) {
    WebElement panel = expandAccordionByAriaControls(headerEl, true);
    return panel.getText().toLowerCase().contains(expectedSubstring.toLowerCase());
  }

  // --- normalize helper (handles bullets/nbsp/casing/whitespace) ---
  protected static String normalize(String s) {
    if (s == null) return "";
    return s.replace('\u00A0',' ')         // nbsp -> space
        .replace('\u2022',' ')         // bullet -> space
        .replace('\u2013','-')         // en dash -> hyphen (optional)
        .replace('\u2014','-')         // em dash -> hyphen (optional)
        .replaceAll("\\s+", " ")       // collapse whitespace
        .trim()
        .toLowerCase();
  }
  /** Helper to fix flaky accordions.
  * Expand an accordion header whose panel is linked via aria-controls, and wait until expanded. */
  protected WebElement expandAccordionStable(WebElement headerEl, java.time.Duration timeout) {
    String controlsId = headerEl.getAttribute("aria-controls");
    if (controlsId == null || controlsId.isBlank()) {
      throw new IllegalStateException("Accordion header missing aria-controls");
    }
    org.openqa.selenium.By panelBy = org.openqa.selenium.By.id(controlsId.trim());

    // If not expanded, click it (some themes toggle aria-expanded on header)
    String expanded = String.valueOf(headerEl.getAttribute("aria-expanded"));
    if (!"true".equalsIgnoreCase(expanded)) {
      safeClick(headerEl);
    }

    // Wait for the panel to be visible and have non-zero height
    org.openqa.selenium.support.ui.WebDriverWait localWait =
        new org.openqa.selenium.support.ui.WebDriverWait(driver, timeout);
    return localWait.until(d -> {
      org.openqa.selenium.WebElement panel = d.findElement(panelBy);
      boolean visible = panel.isDisplayed() && panel.getRect().height > 0;
      return visible ? panel : null;
    });
  }

/** Expand via aria-controls, then wait until panel text contains expected (normalized) */
protected boolean accordionTextEventuallyContains(WebElement headerEl, String expected, java.time.Duration timeout) {
  WebElement panel = expandAccordionStable(headerEl, timeout);
  String want = normalize(expected);
  long end = System.nanoTime() + java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(timeout.toMillis());
  while (System.nanoTime() < end) {
    String got = normalize(panel.getText());
    if (got.contains(want)) return true;
    try { Thread.sleep(75); } catch (InterruptedException ignored) {}
  }
  return false;
}
}