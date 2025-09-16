package org.catalyte.io.pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.catalyte.io.utils.LocatorMapper;
import org.openqa.selenium.support.locators.RelativeLocator;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PageFooter extends Page{

  public PageFooter(WebDriver driver) { super(driver); }

  private final List<String> pageURLs = Arrays.asList(
      "https://catalyte.io",
      "https://www.catalyte.io/hire-talent/hire-apprentices/",
      "https://www.catalyte.io/apprenticeships/"
  );

  public List<String> getPageURLs() { return pageURLs; }

  //footer sections
  private final By pageFooterSectionTop = By.cssSelector(".elementor-element-462c93c");
  private final By pageFooterSectionDivider = By.cssSelector(".elementor-element-2eda9b3");
  private final By pageFooterSectionBottom = By.cssSelector(".elementor-element-85035af");
  private final By pageFooterSectionLegal = By.cssSelector(".elementor-element-1549158");

  public List<By> getFooterLocatorsAsList() {
    List<By> locators = new ArrayList<>();
    locators.add(pageFooterSectionTop);
    locators.add(pageFooterSectionDivider);
    locators.add(pageFooterSectionBottom);
    locators.add(pageFooterSectionLegal);

    return locators;
  }

  //Nav menus
  private final By pageFooterNavMenuAbout = By.cssSelector(".elementor-element-bd4e84b");
  private final By pageFooterNavMenuServices = By.cssSelector(".elementor-element-7195032");
  private final By pageFooterNavMenuJobs = By.cssSelector(".elementor-element-06876e6");
  private final By pageFooterNavMenuSocials = By.cssSelector(".elementor-element-f14ad7f");

  public By getPageFooterNavMenuAbout() { return pageFooterNavMenuAbout; }
  public By getPageFooterNavMenuServices() { return pageFooterNavMenuServices; }
  public By getPageFooterNavMenuJobs() { return pageFooterNavMenuJobs; }
  public By getPageFooterNavMenuSocials() { return pageFooterNavMenuSocials; }

  public List<By> getFooterNavMenusAsList_ExcludingFollow(){
    List<By> menus = new ArrayList<>();
    menus.add(pageFooterNavMenuAbout);
    menus.add(pageFooterNavMenuJobs);
    menus.add(pageFooterNavMenuServices);

    return menus;
  }

  public List<WebElement> getNavMenuHeadings(){
    List<WebElement> headings = new ArrayList<>();
    for(By menuLocator : getFooterNavMenusAsList_ExcludingFollow()){
      WebElement heading = driver.findElement(RelativeLocator.with
          (By.cssSelector(".elementor-widget-heading")).above(menuLocator));
      System.out.println(heading.getText());
      headings.add(heading);
    }
    return headings;
  }

  public void clickNavMenuLink(WebElement menuItem){
    safeClick(menuItem);
    dismissCookieIfPresent();
  }

  //helper to fix timeouts
  public boolean waitForUrlChangeOrHash(WebDriverWait wait, String before, String expectedHref) {
    try {
      if (expectedHref.contains("#")) {
        return wait.until(d -> d.getCurrentUrl().contains("#"));
      }
      return wait.until(d -> !d.getCurrentUrl().equals(before));
    } catch (TimeoutException e) {
      return false;
    }
  }

  public WebElement getPageFooterSectionTop() {
    return driver.findElement(pageFooterSectionTop);
  }

  public WebElement getPageFooterSectionDivider() {
    return driver.findElement(pageFooterSectionDivider);
  }

  public WebElement getPageFooterSectionBottom() {
    return driver.findElement(pageFooterSectionBottom);
  }

  public WebElement getPageFooterSectionLegal() {
    return driver.findElement(pageFooterSectionLegal);
  }

  public List<WebElement> getChildElements(WebElement section){
    return section.findElements(By.cssSelector(".elementor-element"));
  }
}