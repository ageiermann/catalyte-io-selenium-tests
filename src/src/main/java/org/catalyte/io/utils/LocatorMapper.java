package org.catalyte.io.utils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.catalyte.io.pages.PageFooter;
import org.openqa.selenium.By;
import org.openqa.selenium.support.pagefactory.ByChained;

public class LocatorMapper {

  private static final Map<String, List<String>> ALLOW = Map.of(
      "Services", List.of(
          "https://www.catalyte.io/services/",
          "https://www.catalyte.io/services/solutions/",
          "https://www.catalyte.io/services/engagement-models/",
          "https://www.catalyte.io/webinar-series/",
          "https://www.catalyte.io/success-stories/client-success-stories/"
      ),
      "Company", List.of(
          "https://www.catalyte.io/about/",
          "https://www.catalyte.io/about/contact-sales/",
          "https://www.catalyte.io/about/#about-press"
      ),
      "Jobs", List.of(
          "https://www.catalyte.io/apprenticeships/",
          "https://www.catalyte.io/about/faqs#screening",
          "https://www.catalyte.io/success-stories/alumni-success-stories/"
      )
  );

  private final Map<String, By> menus; // canonical heading -> container By
  StringNormalizer normalizer;

  public LocatorMapper(PageFooter footer) {
    Objects.requireNonNull(footer, "Footer must not be null");
    this.menus = Map.of(
        "company",  footer.getPageFooterNavMenuAbout(),     // "Company" section
        "services", footer.getPageFooterNavMenuServices(),
        "jobs",     footer.getPageFooterNavMenuJobs()
    );
    normalizer = new StringNormalizer();
  }

  public List<String> headingKeys() {
    return List.of("Company", "Services", "Jobs");
  }

  public Optional<By> menuBy(String headingText) {
    String key = normalizer.normalizeKey(headingText);
    return Optional.ofNullable(menus.get(key));
  }

  /** All links under a heading, as a locator (scoped to that menu). */
  public Optional<By> linksInMenuBy(String headingText) {
    return menuBy(headingText).map(menu -> new ByChained(menu, By.cssSelector("a.menu-link[href]")));
  }

  /** A specific link (by exact href) under a heading, as a locator. */
  public Optional<By> linkInMenuBy(String headingText, String href) {
    String css = "a.menu-link[href=\"" + normalizer.cssEscape(href) + "\"]";
    return menuBy(headingText).map(menu -> new ByChained(menu, By.cssSelector(css)));
  }

  public Map<String, List<String>> getAllowedUrls() { return ALLOW; }
}