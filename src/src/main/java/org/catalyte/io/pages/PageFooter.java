package org.catalyte.io.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.locators.RelativeLocator;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PageFooter extends Page {

  private final List<String> pageURLs = Arrays.asList(
      "https://catalyte.io",
      "https://www.catalyte.io/hire-talent/hire-apprentices/",
      "https://www.catalyte.io/apprenticeships/"
  );
  //footer sections
  private final By pageFooterSectionTop = By.cssSelector(".elementor-element-462c93c");
  private final By pageFooterSectionDivider = By.cssSelector(".elementor-element-2eda9b3");
  private final By pageFooterSectionBottom = By.cssSelector(".elementor-element-85035af");
  private final By pageFooterSectionLegal = By.cssSelector(".elementor-element-1549158");

  private static final By H6_HEADING = By.cssSelector("h6.elementor-heading-title");

  //Nav menus
  private final By pageFooterNavMenuAbout = By.cssSelector(".elementor-element-bd4e84b");
  private final By pageFooterNavMenuServices = By.cssSelector(".elementor-element-7195032");
  private final By pageFooterNavMenuJobs = By.cssSelector(".elementor-element-06876e6");
  private final By pageFooterNavMenuFollow = By.cssSelector(".elementor-element-f14ad7f");

  public PageFooter(WebDriver driver) {
    super(driver);
  }

  public List<String> getPageURLs() {
    return pageURLs;
  }

  public List<By> getFooterLocatorsAsList() {
   return List.of(pageFooterSectionTop, pageFooterSectionDivider,
       pageFooterSectionBottom, pageFooterSectionLegal);
  }

  public By getPageFooterNavMenuAbout() {
    return pageFooterNavMenuAbout;
  }

  public By getPageFooterNavMenuServices() {
    return pageFooterNavMenuServices;
  }

  public By getPageFooterNavMenuJobs() {
    return pageFooterNavMenuJobs;
  }

  public By getPageFooterNavMenuFollow() {
    return pageFooterNavMenuFollow;
  }

  public List<By> getFooterNavMenusAsList_ExcludingFollow() {
    return List.of(pageFooterNavMenuAbout, pageFooterNavMenuJobs, pageFooterNavMenuServices);
  }

  /** All buttons in the top footer section (scoped) — works for n buttons. */
  public List<WebElement> getTopFooterSectionButtons() {
    return driver.findElements(new ByChained(
        pageFooterSectionTop,
        By.cssSelector("a.elementor-button")
    ));
  }

  public By getPageFooterSectionTopLocator(){
    return pageFooterSectionTop;
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

  public WebElement companyHeading() {
    By rel = RelativeLocator.with(H6_HEADING).above(pageFooterNavMenuAbout);
    return driver.findElement(rel); // ✅ relative locator on driver
  }
  public WebElement servicesHeading() {
    By rel = RelativeLocator.with(H6_HEADING).above(pageFooterNavMenuServices);
    return driver.findElement(rel);
  }
  public WebElement jobsHeading() {
    By rel = RelativeLocator.with(H6_HEADING).above(pageFooterNavMenuJobs);
    return driver.findElement(rel);
  }

  public List<WebElement> getNavMenuHeadings() {
    return List.of(companyHeading(), servicesHeading(), jobsHeading());
  }

  public List<By> getNavMenuHeadingBys() {
    return List.of(
        RelativeLocator.with(H6_HEADING).above(pageFooterNavMenuAbout),
        RelativeLocator.with(H6_HEADING).above(pageFooterNavMenuServices),
        RelativeLocator.with(H6_HEADING).above(pageFooterNavMenuJobs)
    );
  }

  private static final By MENU_LINKS = By.cssSelector("a.menu-link[href]");

  public List<WebElement> companyLinks()  {
    return driver.findElements(new ByChained(pageFooterNavMenuAbout, MENU_LINKS));  // ✅ scoped without RelativeLocator
  }
  public List<WebElement> servicesLinks() {
    return driver.findElements(new ByChained(pageFooterNavMenuServices, MENU_LINKS));
  }
  public List<WebElement> jobsLinks() {
    return driver.findElements(new ByChained(pageFooterNavMenuJobs, MENU_LINKS));
  }

  public WebElement linkInMenu(By menuBy, String href) {
    By linkBy = new ByChained(menuBy, By.cssSelector("a.menu-link[href=\"" + normalizer.cssEscape(href) + "\"]"));
    return driver.findElement(linkBy);
  }

  public void clickNavMenuLink(WebElement menuItem) {
    safeClick(menuItem);
    dismissCookieIfPresent();
  }

  //helper to fix timeouts
  public boolean waitForUrlChangeOrHash(WebDriverWait wait, String before, String expectedHref) {
    WebDriverWait localWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(10));
    try {
      if (expectedHref.contains("#")) {
        return localWait.until(d -> d.getCurrentUrl().contains("#"));
      }
      return localWait.until(d -> !d.getCurrentUrl().equals(before));
    } catch (TimeoutException e) {
      return false;
    }
  }

  public void clickFooterButton(WebElement button){
    safeClick(button);
    dismissCookieIfPresent();
  }

  /** Text nodes (h3–h6, p) that appear in the top footer section between two known “marker” elements. */
  public List<WebElement> getTopFooterSectionText() {
    By textRange = new ByChained(
        pageFooterSectionTop,
        By.xpath(
            ".//div[contains(@class,'elementor-element-462c93c')]" +      // start marker
                "/following::*[self::h3 or self::h4 or self::h5 or self::h6 or self::p]" +
                "[count(.|.//div[contains(@class,'elementor-element-2eda9b3')]/preceding::*)=" +  // end marker boundary
                " count(.//div[contains(@class,'elementor-element-2eda9b3')]/preceding::*)]"
        )
    );
    return driver.findElements(textRange);
  }

  /** “Connect now” button: button is after element d4fd473 and before element 165fd33 (scoped to top section). */
  public WebElement getTopFooterSectionConnectNowButton() {
    By withinTop = new ByChained(
        pageFooterSectionTop,
        By.xpath(
            ".//a[contains(@class,'elementor-button')]" +
                "[preceding::div[contains(@class,'elementor-element-d4fd473')]]" +
                "[following::div[contains(@class,'elementor-element-165fd33')]]"
        )
    );
    return driver.findElement(withinTop);
  }

  /** “Learn more” button: below 165fd33 and above the divider (scoped to top section). */
  public WebElement getTopFooterSectionLearnMoreButton() {
    // If you can’t reference the divider via XPath easily, pick the last button below 165fd33.
    By candidates = new ByChained(
        pageFooterSectionTop,
        By.xpath(
            ".//a[contains(@class,'elementor-button')]" +
                "[preceding::div[contains(@class,'elementor-element-165fd33')]]"
        )
    );
    List<WebElement> list = driver.findElements(candidates);
    if (list.isEmpty()) throw new NoSuchElementException("Top footer Learn More button not found");
    return list.get(list.size() - 1);
  }

  public List<WebElement> getChildElements(WebElement section) {
    return section.findElements(By.cssSelector(".elementor-element"));
  }
}