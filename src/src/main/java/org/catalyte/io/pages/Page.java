package org.catalyte.io.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Helper methods for locators in Page objects.
 */
public class Page {

  protected WebDriver driver;
  protected WebDriverWait wait;

  public Page(WebDriver driver) {
    this.driver = driver;
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
  }


  protected WebElement find(By locator) {
    return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
  }

  protected void click(By locator) {
    find(locator).click();
  }

  protected void type(By locator, String text) {
    WebElement element = find(locator);
    element.clear();
    element.sendKeys(text);
  }

  protected String getText(By locator) {
    return find(locator).getText();
  }

  /**
   * Plays a Vimeo video inside an Elementor widget and verifies playback.
   * @param widgetCssLocator CSS selector for the widget container (e.g. ".elementor-widget-video")
   */
  public void playVimeoVideo(String widgetCssLocator) throws InterruptedException {

    WebElement overlay = wait.until(
        ExpectedConditions.elementToBeClickable(
            By.cssSelector(widgetCssLocator + " .elementor-wrapper")
        )
    );
    overlay.click();

    WebElement vimeoFrame = wait.until(
        ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(widgetCssLocator + " iframe.elementor-video-iframe")
        )
    );

    driver.switchTo().frame(vimeoFrame);

    WebElement playButton = wait.until(
        ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Play']"))
    );
    playButton.click();

    //Give video a chance to start
    Thread.sleep(2000);

    WebElement pauseButton = wait.until(
        ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button[aria-label='Pause']"))
    );
    if (!pauseButton.isDisplayed()) {
      throw new AssertionError("Video did not start playing!");
    }
    driver.switchTo().defaultContent();
  }
}