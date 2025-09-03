package org.catalyte.io.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
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
  protected void click(By locator) { find(locator).click(); }
  protected void type(By locator, String text) { var e = find(locator); e.clear(); e.sendKeys(text); }
  protected String getText(By locator) { return find(locator).getText(); }

  public void playVimeoVideo(String widgetCssLocator) throws InterruptedException {
    WebElement overlay = wait.until(ExpectedConditions.elementToBeClickable(
        By.cssSelector(widgetCssLocator + " .elementor-wrapper")));
    overlay.click();

    WebElement vimeoFrame = wait.until(ExpectedConditions.presenceOfElementLocated(
        By.cssSelector(widgetCssLocator + " iframe.elementor-video-iframe")));
    driver.switchTo().frame(vimeoFrame);

    WebElement playButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Play']")));
    playButton.click();

    Thread.sleep(2000);
    WebElement pauseButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button[aria-label='Pause']")));
    if (!pauseButton.isDisplayed()) throw new AssertionError("Video did not start playing!");
    driver.switchTo().defaultContent();
  }

  /** Helper methods to fix flaky clicks. **/
  /* ------------------------------------- */

  /** Scroll element to center of viewport. */
  protected void scrollToCenter(WebElement el) {
    ((JavascriptExecutor) driver).executeScript(
        "const r=arguments[0].getBoundingClientRect();" +
            "window.scrollBy({top: r.top - (window.innerHeight/2) + r.height/2, left: 0, behavior: 'instant'});",
        el
    );
  }

  /** Try a real click; fall back to JS click if intercepted; retries once. */
  protected void safeClick(By locator) {
    // wait for presence & visibility & clickability
    WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));

    // bring to a safe position (avoid sticky header/footers)
    scrollToCenter(el);

    // small settle to let CSS/animation finish
    try { Thread.sleep(75); } catch (InterruptedException ignored) {}

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
      WebElement accept = driver.findElement(By.cssSelector("#onetrust-accept-btn-handler, .ot-sdk-container [data-handler='accept']"));
      if (accept.isDisplayed()) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", accept);
        Thread.sleep(100); // brief settle
      }
    } catch (NoSuchElementException | StaleElementReferenceException | InterruptedException ignored) {}
  }
}