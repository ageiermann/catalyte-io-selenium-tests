package org.catalyte.io.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.catalyte.io.pages.PageFooter;
import org.openqa.selenium.By;

public class LocatorMapper {

  private Map<String, By> menus = new HashMap<>();
  private PageFooter footer;

  public LocatorMapper(PageFooter footer) {
    this.footer = Objects.requireNonNull(footer, "Footer must not be null");
    this.menus = Map.of(
        "company", footer.getPageFooterNavMenuAbout(),
        "services", footer.getPageFooterNavMenuServices(),
        "jobs", footer.getPageFooterNavMenuJobs()
    );
  }

  public Optional<By> menuBy(String headingText) {
    String key = headingText == null ? "" : headingText.trim().toLowerCase();
    return Optional.ofNullable(menus.get(key));
  }

  // Allow-list destinations per heading (Services can fan out)
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
          // external Workday link is intentionally excluded since it’s offsite & slow
      )
  );

  public Map<String, List<String>> getAllowedUrls(){ return ALLOW; }

  //--- normalize helper (handles bullets/nbsp/casing/whitespace) ---
  protected static String normalize(String s) {
    if (s == null) return "";
    return s.replace('\u00A0',' ')         // nbsp -> space
        .replace('\u2022',' ')         // bullet -> space
        .replace('\u2013','-')         // en dash -> hyphen (optional)
        .replace('\u2014','-')         // em dash -> hyphen (optional)
        .replaceAll("\\s+", " ")       // collapse whitespace
        .trim()
        .toLowerCase();
  }
}