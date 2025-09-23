package org.catalyte.io.utils;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Helper class to safely click and navigate using Elementor buttons.
 */
public final class ButtonNavHelper {

  private static final StringNormalizer normalizer = new StringNormalizer();

  public ButtonNavHelper() {
  }

  private static <T> Optional<T> attempt(ThrowingSupplier<T> op) {
    try {
      return Optional.ofNullable(op.get());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private static boolean attemptRun(ThrowingRunnable op) {
    try {
      op.run();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private static WebDriverWait wait(WebDriver d, Duration t) {
    return new WebDriverWait(d, t);
  }

  /**
   * Always get a JS executor from the *real* driver, even if it’s wrapped (e.g., Serenity).
   */
  public static JavascriptExecutor js(WebDriver driver) {
    if (driver instanceof JavascriptExecutor) {
      return (JavascriptExecutor) driver;
    }
    if (driver instanceof WrapsDriver wraps
        && wraps.getWrappedDriver() instanceof JavascriptExecutor) {
      return (JavascriptExecutor) wraps.getWrappedDriver();
    }
    throw new IllegalStateException("Driver does not support JS: " + driver.getClass().getName());
  }

  /**
   * Snapshot a single button (text+href) from elements already found on the page.
   */
  public static List<Btn> snapshotButton(List<WebElement> elements) {
    return elements.stream()
        .map(el -> new Btn(safeText(el), el.getAttribute("href")))
        .filter(b -> b.href != null && !b.href.isBlank())
        .collect(Collectors.toMap(b -> b.href, b -> b, (a, b) -> a)) // de-dupe same href
        .values().stream().collect(Collectors.toList());
  }

  /**
   * Snapshot a single button (text+href) using a locator on the current page.
   */
  public static List<Btn> snapshotButton(WebDriver driver, By buttonBy) {
    return snapshotButton(driver.findElements(buttonBy));
  }

  /**
   * Snapshot multiple buttons (text+href) from elements already found on the page (de-dupes by
   * href).
   */
  public static List<Btn> snapshotButtons(List<WebElement> elements) {
    return elements.stream()
        .map(el -> new Btn(safeText(el), el.getAttribute("href")))
        .filter(b -> b.href != null && !b.href.isBlank())
        .collect(Collectors.toMap(b -> b.href, b -> b, (a, b) -> a)) // de-dupe same href
        .values().stream().collect(Collectors.toList());
  }

  /* ================= Snapshot utilities ================= */

  /**
   * Snapshot multiple buttons (text+href) using a locator on the current page.
   */
  public static List<Btn> snapshotButtons(WebDriver driver, By buttonsBy) {
    return snapshotButtons(driver.findElements(buttonsBy));
  }

  /**
   * Open startUrl and wait for a "ready" marker (returns false on failure).
   */
  public static boolean openAndWait(WebDriver driver, String startUrl, By isPageLoaded,
      Duration timeout) {
    return attemptRun(() -> driver.navigate().to(startUrl))
        && attempt(() -> wait(driver, timeout).until(
        ExpectedConditions.presenceOfElementLocated(isPageLoaded))).isPresent();
  }

  /**
   * Build a locator for a specific link by exact href anywhere on the page.
   */
  public static By linkByHref(String href) {
    return By.cssSelector("a[href=\"" + normalizer.cssEscape(href) + "\"]");
  }

  /**
   * Build a *menu-scoped* locator for a specific link by href (menuBy is the container).
   */
  public static By linkInMenuByHref(By menuBy, String href) {
    return new ByChained(menuBy, linkByHref(href));
  }

  /**
   * Find a clickable link by locator (returns Optional).
   */
  public static Optional<WebElement> findClickable(WebDriver driver, By by, Duration timeout) {
    return attempt(() -> wait(driver, timeout).until(ExpectedConditions.elementToBeClickable(by)));
  }

  /**
   * Click in the same tab via JS — JS is executed on the *driver*, with the element as an
   * argument.
   */
  public static boolean clickSameTab(WebDriver driver, WebElement link) {
    JavascriptExecutor executor = js(driver); // <- ALWAYS use driver here
    return !attemptRun(() -> {
      executor.executeScript("arguments[0].removeAttribute('target');", link);
      executor.executeScript("arguments[0].click();", link);
    });
  }

  /**
   * Wait for URL change or hash appearance within timeout.
   */
  public static boolean waitForUrlChangeOrHash(WebDriver driver, Duration timeout, String before,
      String expectedHref) {
    return attempt(() -> {
      WebDriverWait w = wait(driver, timeout);
      if (expectedHref != null && expectedHref.contains("#")) {
        return w.until(d -> d.getCurrentUrl().contains("#"));
      }
      return w.until(d -> !d.getCurrentUrl().equals(before));
    }).orElse(false);
  }

  /**
   * Validate destination: per-text fragment rule OR lenient href match (host+path, tolerate
   * redirects).
   */
  public static boolean validateDestination(String buttonText, String href, String finalUrl,
      Map<String, String> expectContains) {
    String frag = expectContains.get(buttonText);
    return (frag != null)
        ? finalUrl.toLowerCase().contains(frag.toLowerCase())
        : urlsMatchLenient(href, finalUrl);
  }

  /**
   * Verify a single button by href (page-wide, not menu-scoped). Returns Optional failure message
   * (empty = success).
   */
  public static Optional<String> verifyButton(
      WebDriver driver, String startUrl, By isPageLoaded, By locator,
      Btn b, Map<String, String> expectContains, Duration timeout) {

    if (!openAndWait(driver, startUrl, isPageLoaded, timeout)) {
      return Optional.of("Open/wait failed: %s (%s)".formatted(startUrl, b));
    }

    List<By> candidates = linkCandidatesByHrefAndText(b.href, b.text);

    String before = driver.getCurrentUrl();
    boolean clicked = clickCandidateSameTabWithRetry(driver, locator, candidates, timeout, 3);
    if (!clicked) {
      return Optional.of("Click failed: href=%s (btn='%s')".formatted(b.href, b.text));
    }

    boolean changed = waitForUrlChangeOrHash(driver, timeout, before, b.href);
    if (changed) {
      attemptRun(() -> js(driver).executeScript("window.stop();"));
    }

    String finalUrl = driver.getCurrentUrl();
    boolean ok = changed && validateDestination(b.text, b.href, finalUrl, expectContains);
    return ok ? Optional.empty()
        : Optional.of("Button '%s' href=%s → final=%s".formatted(b.text, b.href, finalUrl));
  }

  /**
   * Verify a single button *scoped to a menu container* (safer when there are duplicates). Returns
   * Optional failure message (empty = success).
   */
  public static Optional<String> verifyButtonInMenu(
      WebDriver driver, String startUrl, By isPageLoaded, By menuBy, Btn b,
      Map<String, String> expectContains, Duration timeout) {

    if (!openAndWait(driver, startUrl, isPageLoaded, timeout)) {
      return Optional.of(String.format("Open/wait failed: %s (%s)", startUrl, b));
    }

    Optional<WebElement> linkOpt = findClickable(driver, linkInMenuByHref(menuBy, b.href), timeout);
    if (linkOpt.isEmpty()) {
      return Optional.of(String.format("Menu link not found: href=%s (btn='%s')", b.href, b.text));
    }

    String before = driver.getCurrentUrl();
    if (clickSameTab(driver, linkOpt.get())) {
      return Optional.of(String.format("Click failed (menu): href=%s (btn='%s')", b.href, b.text));
    }

    boolean changed = waitForUrlChangeOrHash(driver, timeout, before, b.href);
    if (changed) {
      attemptRun(() -> ((JavascriptExecutor) driver).executeScript("window.stop();"));
    }

    String finalUrl = driver.getCurrentUrl();
    boolean ok = changed && validateDestination(b.text, b.href, finalUrl, expectContains);
    return ok ? Optional.empty()
        : Optional.of(String.format("Button '%s' href=%s → final=%s", b.text, b.href, finalUrl));
  }

  /**
   * Verify many buttons (page-wide); returns all failure messages (empty = all OK).
   */
  public static List<String> verifyButtons(
      WebDriver driver, String startUrl, By locator, By readyBy,
      List<Btn> buttons, Map<String, String> expectContains, Duration timeout) {
    List<String> failures = new ArrayList<>();
    for (Btn b : buttons) {
      verifyButton(driver, startUrl, readyBy, locator, b, expectContains, timeout).ifPresent(
          failures::add);
    }
    return failures;
  }

  /**
   * Verify many buttons under a specific menu container (safer when duplicates exist).
   */
  public static List<String> verifyButtonsInMenu(
      WebDriver driver, String startUrl, By readyBy, By menuBy,
      List<Btn> buttons, Map<String, String> expectContains, Duration timeout) {
    List<String> failures = new ArrayList<>();
    for (Btn b : buttons) {
      verifyButtonInMenu(driver, startUrl, readyBy, menuBy, b, expectContains, timeout).ifPresent(
          failures::add);
    }
    return failures;
  }

  /* ================= Helpers ================= */
  private static String safeText(WebElement el) {
    try {
      return el.getText().trim();
    } catch (Exception e) {
      return "";
    }
  }

  private static boolean urlsMatchLenient(String expected, String actual) {
    try {
      URL e = new URL(expected), a = new URL(actual);
      boolean host = a.getHost().equalsIgnoreCase(e.getHost());
      String ep = normalizer.normalize(e.getPath()), ap = normalizer.normalize(a.getPath());
      return host && (ap.startsWith(ep) || ep.startsWith(ap));
    } catch (Exception ignore) {
      return actual.startsWith(expected) || actual.startsWith(expected + "/");
    }
  }

  private static String pathOf(String href) {
    try {
      java.net.URI u = java.net.URI.create(href);
      String p = Optional.ofNullable(u.getPath()).orElse("/");
      return u.getFragment() == null ? p : p + "#" + u.getRawFragment();
    } catch (Exception e) {
      return href;
    }
  }

  /**
   * Build robust link candidates for a given href (absolute/relative/path, with/without trailing
   * slash).
   */
  public static List<By> linkCandidatesByHref(String href) {
    String path = pathOf(href);
    String pathNoSlash =
        path.endsWith("/") && path.length() > 1 ? path.substring(0, path.length() - 1) : path;
    String pathWithSlash = path.endsWith("/") ? path : path + "/";

    return List.of(
        // exact absolute (if DOM uses absolute)
        By.cssSelector("a[href=\"" + normalizer.cssEscape(href) + "\"]"),
        // exact relative
        By.cssSelector("a[href=\"" + normalizer.cssEscape(path) + "\"]"),
        By.cssSelector("a[href=\"" + normalizer.cssEscape(pathNoSlash) + "\"]"),
        By.cssSelector("a[href=\"" + normalizer.cssEscape(pathWithSlash) + "\"]"),
        // ends-with path (tolerate query params/utm)
        By.cssSelector("a[href$=\"" + normalizer.cssEscape(path) + "\"]"),
        By.cssSelector("a[href$=\"" + normalizer.cssEscape(pathNoSlash) + "\"]"),
        By.cssSelector("a[href$=\"" + normalizer.cssEscape(pathWithSlash) + "\"]")
    );
  }

  public static List<By> linkCandidatesByHrefAndText(String href, String buttonText) {
    String path = pathOf(href);
    String noSlash =
        path.endsWith("/") && path.length() > 1 ? path.substring(0, path.length() - 1) : path;
    String withSlash = path.endsWith("/") ? path : path + "/";
    String txt = Optional.ofNullable(buttonText).orElse("").trim().toLowerCase();

    List<By> list = new ArrayList<>(List.of(
        // exact absolute + relative variants
        By.cssSelector("a[href=\"" + normalizer.cssEscape(href) + "\"]"),
        By.cssSelector("a[href=\"" + normalizer.cssEscape(path) + "\"]"),
        By.cssSelector("a[href=\"" + normalizer.cssEscape(noSlash) + "\"]"),
        By.cssSelector("a[href=\"" + normalizer.cssEscape(withSlash) + "\"]"),
        // ends-with path (tolerate query/utm)
        By.cssSelector("a[href$=\"" + normalizer.cssEscape(path) + "\"]"),
        By.cssSelector("a[href$=\"" + normalizer.cssEscape(noSlash) + "\"]"),
        By.cssSelector("a[href$=\"" + normalizer.cssEscape(withSlash) + "\"]")
    ));
    if (!txt.isEmpty()) {
      // text fallback (case-insensitive), restricted to elementor buttons
      list.add(By.xpath(".//a[contains(@class,'elementor-button') and " +
          "translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')="
          +
          "'" + txt.replace("'", "''") + "']"));
    }
    return list;
  }

  /**
   * Try multiple locators; return the first clickable element found.
   */
  public static Optional<WebElement> findClickable(WebDriver driver, List<By> candidates,
      Duration timeout) {
    for (By by : candidates) {
      Optional<WebElement> el = attempt(() -> wait(driver, timeout)
          .ignoring(NoSuchElementException.class)
          .ignoring(StaleElementReferenceException.class)
          .until(ExpectedConditions.elementToBeClickable(by)));
      if (el.isPresent()) {
        return el;
      }
    }
    return Optional.empty();
  }

  /**
   * Same as above, but scoped to a container (e.g., top-footer section) via ByChained.
   */
  public static Optional<WebElement> findClickableInContainerOld(WebDriver driver, By container,
      List<By> candidates, Duration timeout) {
    for (By by : candidates) {
      By scoped = new ByChained(container, by);
      Optional<WebElement> el = attempt(() -> wait(driver, timeout)
          .ignoring(NoSuchElementException.class)
          .ignoring(StaleElementReferenceException.class)
          .until(ExpectedConditions.elementToBeClickable(scoped)));
      if (el.isPresent()) {
        return el;
      }
    }
    return Optional.empty();
  }

  public static Optional<WebElement> findClickableInContainer(
      WebDriver driver, By container, By candidate, Duration timeout) {
    By scoped = new ByChained(container, candidate);
    return attempt(() -> wait(driver, timeout)
        .ignoring(NoSuchElementException.class)
        .ignoring(StaleElementReferenceException.class)
        .until(ExpectedConditions.elementToBeClickable(scoped)));
  }

  private static void scrollCenter(WebDriver driver, WebElement el) {
    try {
      js(driver).executeScript("arguments[0].scrollIntoView({block:'center',inline:'center'})", el);
    } catch (Exception ignored) {
    }
  }

  public static boolean clickCandidateSameTabWithRetry(
      WebDriver driver, By container, List<By> candidates, Duration timeout, int attempts) {

    for (int i = 0; i < attempts; i++) {
      for (By by : candidates) {
        var elOpt = findClickableInContainer(driver, container, by, timeout);
        if (elOpt.isEmpty()) {
          continue;
        }
        WebElement el = elOpt.get();
        try {
          scrollCenter(driver, el);
          JavascriptExecutor exec = js(driver);
          exec.executeScript("arguments[0].removeAttribute('target');", el);
          try {
            el.click(); // try native first
          } catch (ElementClickInterceptedException ignored) {
            exec.executeScript("arguments[0].click();", el); // fallback JS click
          }
          return true;
        } catch (StaleElementReferenceException | JavascriptException e) {
          // re-loop to re-resolve & retry
        }
      }
      try {
        Thread.sleep(120);
      } catch (InterruptedException ignored) {
      }
    }
    return false;
  }

  @FunctionalInterface
  interface ThrowingSupplier<T> {

    T get() throws Exception;
  }

  @FunctionalInterface
  interface ThrowingRunnable {

    void run() throws Exception;
  }

  public static final class Btn {

    public final String text, href;

    public Btn(String text, String href) {
      this.text = text;
      this.href = href;
    }

    @Override
    public String toString() {
      return "Btn{text='" + text + "', href='" + href + "'}";
    }
  }
}