package org.catalyte.io.pages;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;

public class HomePage extends Page {

  // ==== common child selectors ====
  private static final By PAGE_DID_LOAD = By.cssSelector(".elementor-element-478292ab");
  private static final By CHILD_ELEMENT = By.cssSelector(".elementor-element");
  private static final By BUTTON_ANCHOR = By.cssSelector("a.elementor-button");
  private static final By WIDGET_BUTTON = By.cssSelector(".elementor-widget-button");
  // ==== top-level locators (containers/sections/markers) ====
  private final By accordionTalent = By.id("what-if-i-need-more-than-apprentice-talent");
  private final By industriesAccordion = By.id("what-industries-do-you-provide-talent-for");
  private final By hireAccordion = By.id("how-can-i-hire-catalyte-talent");
  private final By homepageHeadingSection = By.cssSelector(".elementor-element-3d4a7b62");
  private final By homepageInfoboxesSectionTop = By.cssSelector(".elementor-element-3447365");
  private final By homepageInfoboxesSectionBottom = By.cssSelector(".elementor-element-b69b593");
  private final By homepageMidpointDivider = By.cssSelector(".elementor-element-500b262f");
  private final By homepageClientTypeSectionHeading = By.cssSelector(".elementor-element-dc546c3");
  private final By homepageClientTypeSectionEnterprise = By.cssSelector(
      ".elementor-element-13797889");
  private final By homepageClientTypeSectionGovernment = By.cssSelector(
      ".elementor-element-47e9e0f7");
  private final By homepageClientTypeSectionStartups = By.cssSelector(".elementor-element-783863a");
  private final By homepageClientTypeSectionPrivateEquity = By.cssSelector(
      ".elementor-element-50afc0c");
  private final By homepageEngagementModelsHeading = By.cssSelector(".elementor-element-635718f9");
  private final By homepageEngagementModelsSection = By.cssSelector(".elementor-element-1b51c35d");

  public HomePage(WebDriver driver) {
    super(driver);
  }

  // ===== simple elements (top-level By) =====
  public By pageLoadedIfDisplayed() {
    return PAGE_DID_LOAD;
  }

  public WebElement getAccordionTalent() {
    return driver.findElement(accordionTalent);
  }

  public WebElement getAccordionIndustries() {
    return driver.findElement(industriesAccordion);
  }

  public WebElement getAccordionHire() {
    return driver.findElement(hireAccordion);
  }

  // ===== scoped queries via ByChained (no RelativeLocator) =====
  public List<WebElement> getHomepageHeadingElements() {
    return driver.findElements(new ByChained(homepageHeadingSection, CHILD_ELEMENT));
  }

  public By getHomepageHeadingSectionBy() {
    return homepageHeadingSection;
  }

  public By getHomepageMidpointDividerBy() {
    return homepageMidpointDivider;
  }

  public By getHomepageEngagementModelsHeadingBy() {
    return homepageEngagementModelsHeading;
  }

  public By getHomepageEngagementModelsSectionBy() {
    return homepageEngagementModelsSection;
  }

  public WebElement getGetStartedButton() {
    return driver.findElement(new ByChained(homepageHeadingSection, BUTTON_ANCHOR));
  }

  public By getGetStartedButtonBy() {
    return new ByChained(homepageHeadingSection, BUTTON_ANCHOR);
  }

  public List<WebElement> getTopHomepageInfoboxes() {
    return driver.findElements(new ByChained(homepageInfoboxesSectionTop, CHILD_ELEMENT));
  }

  public List<WebElement> getBottomHomepageInfoboxes() {
    return driver.findElements(new ByChained(homepageInfoboxesSectionBottom, CHILD_ELEMENT));
  }

  public WebElement getPartnerWithUsButton() {
    return driver.findElement(new ByChained(homepageMidpointDivider, WIDGET_BUTTON, BUTTON_ANCHOR));
  }

  public By getPartnerWithUsButtonBy() {
    return new ByChained(homepageMidpointDivider, WIDGET_BUTTON, BUTTON_ANCHOR);
  }

  public WebElement getHomepageClientTypeHeading() {
    return driver.findElement(homepageClientTypeSectionHeading);
  }

  public WebElement getHomepageClientTypeEnterprise() {
    return driver.findElement(homepageClientTypeSectionEnterprise);
  }

  public WebElement getHomepageClientTypeGovernment() {
    return driver.findElement(homepageClientTypeSectionGovernment);
  }

  public WebElement getHomepageClientTypeStartups() {
    return driver.findElement(homepageClientTypeSectionStartups);
  }

  public WebElement getHomepageClientTypePrivateEquity() {
    return driver.findElement(homepageClientTypeSectionPrivateEquity);
  }

  public List<WebElement> getHomepageClientTypeSections() {
    return List.of(
        getHomepageClientTypeHeading(),
        getHomepageClientTypeEnterprise(),
        getHomepageClientTypeGovernment(),
        getHomepageClientTypeStartups(),
        getHomepageClientTypePrivateEquity());
  }

  public List<By> getHomepageClientTypeSectionBys() {
    return List.of(
        homepageClientTypeSectionHeading,
        homepageClientTypeSectionEnterprise,
        homepageClientTypeSectionGovernment,
        homepageClientTypeSectionStartups,
        homepageClientTypeSectionPrivateEquity);
  }

  public Map<By, String> getHomepageClientTypes() {
    List<By> sections = getHomepageClientTypeSectionBys();
    List<String> headings = getHomepageClientTypeSections().stream().map(WebElement::getText)
        .toList();
    return sections.stream().collect(Collectors.toMap(
        key -> key,
        key -> headings.get(sections.indexOf(key))
    ));
  }

  /**
   * Engagement Models "Learn More" button. Use a container-scoped query (no RelativeLocator);
   * returns the first <a.elementor-button> inside the section. If there are multiple, choose
   * appropriately (first/last) based on DOM.
   */
  public WebElement getEngagementModelsLearnMoreButton() {
    List<WebElement> buttons = driver.findElements(
        new ByChained(homepageEngagementModelsSection, BUTTON_ANCHOR));
    if (buttons.isEmpty()) {
      throw new NoSuchElementException("No engagement model buttons found");
    }
    return buttons.get(0); // or buttons.get(buttons.size() - 1) if needed
  }

  public By getEngagementModelsLearnMoreButtonBy() {
    return new ByChained(homepageEngagementModelsSection, BUTTON_ANCHOR);
  }

  // ===== Helper: child elements by section (By-based to avoid stale) =====
  public List<WebElement> getChildElements(By sectionBy) {
    return driver.findElements(new ByChained(sectionBy, CHILD_ELEMENT));
  }

  /**
   * (Legacy) Overload if you still have a WebElement section; delegates to the By-based method.
   */
  @Deprecated
  public List<WebElement> getChildElements(WebElement section) {
    // Prefer the By-based version above in new code.
    return section.findElements(CHILD_ELEMENT);
  }
}