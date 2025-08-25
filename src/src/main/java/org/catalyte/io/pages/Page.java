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
}