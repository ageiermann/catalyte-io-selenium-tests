package org.catalyte.io.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PageFooter extends Page{

  public PageFooter(WebDriver driver) { super(driver); }

  private final List<String> pageURLs = Arrays.asList(
      "https://catalyte.io",
      "https://www.catalyte.io/hire-talent/hire-apprentices/",
      "https://www.catalyte.io/apprenticeships/"
  );

  public List<String> getPageURLs() { return pageURLs; }

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