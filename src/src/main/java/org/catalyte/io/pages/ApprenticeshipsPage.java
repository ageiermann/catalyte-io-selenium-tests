package org.catalyte.io.pages;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * "Get Hired" Page object class (focused on key XPath selectors).
 */
public class ApprenticeshipsPage extends Page {

  /* Locators (from your XPath list) */

  // Titles inside icon boxes
  private final By iconBoxTitles = By.cssSelector(".elementor-icon-box-title");

  // Specific image
  private final By wpImage2230 = By.cssSelector(".wp-image-2230");

  // Elementor element with size-default (scoped under a specific parent)
  private final By element4b74d2c3SizeDefault =
      By.cssSelector(".elementor-element-4b74d2c3 .elementor-size-default");

  // Image box wrappers inside elementor-element-2570aea1
  private final By element2570aea1ImageBoxWrapper =
      By.cssSelector(".elementor-element-2570aea1 .elementor-image-box-wrapper");

  // Image box wrappers inside elementor-element-3d21807c
  private final By element3d21807cImageBoxWrapper =
      By.cssSelector(".elementor-element-3d21807c .elementor-image-box-wrapper");

  // Image box titles
  private final By imageBoxTitles = By.cssSelector(".elementor-image-box-title");

  protected WebDriver driver;
  protected WebDriverWait wait;

  public ApprenticeshipsPage(WebDriver driver) {
    super(driver);
    this.driver = driver;
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
  }

  /* Actions and Assertions */

  // Icon box titles
  public List<WebElement> getIconBoxTitles() {
    return driver.findElements(iconBoxTitles);
  }

  // WP image 2230
  public WebElement getWpImage2230() {
    return driver.findElement(wpImage2230);
  }

  // Size default under element 4b74d2c3
  public List<WebElement> getElement4b74d2c3SizeDefault() {
    return driver.findElements(element4b74d2c3SizeDefault);
  }

  // Image box wrappers
  public List<WebElement> getElement2570aea1ImageBoxWrappers() {
    return driver.findElements(element2570aea1ImageBoxWrapper);
  }

  public List<WebElement> getElement3d21807cImageBoxWrappers() {
    return driver.findElements(element3d21807cImageBoxWrapper);
  }

  // Image box titles
  public List<WebElement> getImageBoxTitles() {
    return driver.findElements(imageBoxTitles);
  }
}
