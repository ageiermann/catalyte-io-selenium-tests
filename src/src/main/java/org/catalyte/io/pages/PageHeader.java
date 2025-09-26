package org.catalyte.io.pages;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Header (desktop + mobile) Page Object for catalyte.io
 */
public class PageHeader extends Page {

  // ==== Roots / sections ====
  private static final By HEADER_ROOT = By.id("masthead");
  private static final By DESKTOP_HEADER = By.id("ast-desktop-header");
  private static final By MOBILE_HEADER = By.id("ast-mobile-header");

  // Branding
  private static final By LOGO_LINK = By.cssSelector(".site-branding .custom-logo-link");

  // Primary (desktop) nav (center)
  private static final By PRIMARY_MENU_UL = By.cssSelector("#ast-hf-menu-1");
  private static final By TOP_LINKS = By.cssSelector("> li > a.menu-link");
  private static final By MENU_TEXT_SPAN = By.cssSelector(".menu-text");
  private static final By MENU_TOGGLE_BTN = By.cssSelector("button.ast-menu-toggle");
  private static final By SUBMENU_LINKS = By.cssSelector("ul.sub-menu a.menu-link");

  // Secondary (desktop) nav (right)
  private static final By SECONDARY_NAV_DESKTOP = By.id("secondary_menu-site-navigation-desktop");
  private static final By SECONDARY_MENU_UL = By.cssSelector("#ast-hf-menu-2");

  // Mobile controls
  private static final By MOBILE_TOGGLE_BTN = By.cssSelector(
      "#ast-mobile-header .main-header-menu-toggle");
  private static final By SECONDARY_NAV_MOBILE = By.id("secondary_menu-site-navigation-mobile");

  public PageHeader(WebDriver driver) {
    super(driver);
  }

  // ==== Waits / readiness ====
  public void waitForHeaderReady() {
    wait.until(ExpectedConditions.presenceOfElementLocated(HEADER_ROOT));
    wait.until(ExpectedConditions.presenceOfElementLocated(PRIMARY_MENU_UL));
  }

  // ==== Simple getters ====
  public By headerRootBy() {
    return HEADER_ROOT;
  }

  public By logoBy() {
    return LOGO_LINK;
  }

  public By primaryMenuUlBy() {
    return PRIMARY_MENU_UL;
  }

  public By secondaryMenuUlBy() {
    return SECONDARY_MENU_UL;
  }

  public By mobileToggleBy() {
    return MOBILE_TOGGLE_BTN;
  }

  // ==== Top-level menu (desktop) ====

  /**
   * Snapshot top-level menu links (text + href) under the primary UL.
   */
  public List<Link> snapshotTopLinks() {
    var els = driver.findElements(new ByChained(PRIMARY_MENU_UL, TOP_LINKS));
    return els.stream()
        .map(e -> new Link(visibleText(e), e.getAttribute("href")))
        .filter(l -> l.text != null && !l.text.isBlank())
        .collect(Collectors.toList());
  }

  /**
   * Expand a top menu (by visible label, case-insensitive). Returns true if sub-menu is
   * present/visible.
   */
  public boolean expandTopMenu(String label) {
    By liBy = topMenuItemByLabel(label);
    // if already has visible submenu link, short-circuit
    if (!driver.findElements(new ByChained(liBy, SUBMENU_LINKS)).isEmpty()) {
      return true;
    }

    // click the per-item toggle (safer than hovering)
    Optional<WebElement> toggle = attempt(() ->
        driver.findElement(new ByChained(liBy, MENU_TOGGLE_BTN)));
    if (toggle.isEmpty()) {
      return false;
    }

    try {
      safeClick(toggle.get());
    } catch (Exception ignored) {
    }

    // wait: presence of at least one submenu link under this LI
    try {
      wait.withTimeout(Duration.ofSeconds(5))
          .until(ExpectedConditions.presenceOfElementLocated(new ByChained(liBy, SUBMENU_LINKS)));
      return true;
    } catch (TimeoutException te) {
      return false;
    }
  }

  /**
   * Submenu hrefs under a given top menu (expands if needed).
   */
  public List<String> submenuHrefs(String label) {
    if (!expandTopMenu(label)) {
      return List.of();
    }
    return driver.findElements(new ByChained(topMenuItemByLabel(label), SUBMENU_LINKS)).stream()
        .map(a -> a.getAttribute("href"))
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.toList());
  }

  /**
   * Click a top-level link by its label (case-insensitive). Returns true if click was attempted.
   */
  public boolean clickTopLinkByLabel(String label) {
    Optional<WebElement> anchor = findTopLinkByLabel(label);
    if (anchor.isEmpty()) {
      return false;
    }
    // same-tab
    try {
      js().executeScript("arguments[0].removeAttribute('target');", anchor.get());
    } catch (Exception ignored) {
    }
    safeClick(anchor.get());
    return true;
  }

  /**
   * Find the <a.menu-link> inside the LI that has .menu-text == label (case-insensitive).
   */
  private Optional<WebElement> findTopLinkByLabel(String label) {
    By liBy = topMenuItemByLabel(label);
    return attempt(() -> driver.findElement(new ByChained(liBy, By.cssSelector("a.menu-link"))));
  }

  /**
   * LI locator for a top-level item by its menu text (case-insensitive).
   */
  private By topMenuItemByLabel(String label) {
    String lower = label.trim().toLowerCase(Locale.ROOT).replace("'", "''");
    // Scope to the primary UL, then find an LI that contains a .menu-text matching lowercased text.
    String xp = ".//li[contains(@class,'menu-item')][.//span[contains(@class,'menu-text') " +
        "and translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='"
        + lower + "']]";
    return new ByChained(PRIMARY_MENU_UL, By.xpath(xp));
  }

  // ==== Secondary nav (Contact us) ====
  public Optional<WebElement> secondaryContactLinkDesktop() {
    return attempt(() -> driver.findElement(
        new ByChained(SECONDARY_NAV_DESKTOP, SECONDARY_MENU_UL, By.cssSelector("a.menu-link"))));
  }

  public Optional<WebElement> secondaryContactLinkMobile() {
    return attempt(() -> driver.findElement(
        new ByChained(SECONDARY_NAV_MOBILE, SECONDARY_MENU_UL, By.cssSelector("a.menu-link"))));
  }

  // ==== Mobile ====
  public boolean openMobileMenu() {
    Optional<WebElement> t = attempt(() -> driver.findElement(MOBILE_TOGGLE_BTN));
    if (t.isEmpty()) {
      return false;
    }
    try {
      safeClick(t.get());
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  // ==== Utilities ====

  private <T> Optional<T> attempt(java.util.concurrent.Callable<T> c) {
    try {
      return Optional.ofNullable(c.call());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private String visibleText(WebElement e) {
    String t = e.getText();
    return t == null ? "" : t.trim();
  }

  public void safeClickDismissCookies(WebElement e) {
    safeClick(e);
    dismissCookieIfPresent();
  }

  // Small record for snapshots
  public static class Link {

    public final String text;
    public final String href;

    public Link(String text, String href) {
      this.text = text;
      this.href = href;
    }

    @Override
    public String toString() {
      return text + " → " + href;
    }
  }
}