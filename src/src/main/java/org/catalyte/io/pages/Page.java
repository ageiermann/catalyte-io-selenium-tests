package org.catalyte.io.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
}