package org.catalyte.io.utils;

import java.util.Locale;

/**
 * Normalizer helper class (handles bullets/nbsp/casing/whitespace).
 */
public class StringNormalizer {

  public StringNormalizer() {
  }

  public String normalize(String s) {
    if (s == null) {
      return "";
    }
    return s.replace('\u00A0', ' ')         // nbsp -> space
        .replace('\u2022', ' ')         // bullet -> space
        .replace('\u2013', '-')         // en dash -> hyphen (optional)
        .replace('\u2014', '-')         // em dash -> hyphen (optional)
        .replaceAll("\\s+", " ")       // collapse whitespace
        .trim()
        .toLowerCase();
  }

  public String normalizeLink(String p) {
    return (p == null || p.isBlank())
        ? "/"
        : (p.endsWith("/")
            ? p
            : p + "/");
  }

  public String normalizeLocator(String s) {
    if (s == null) {
      return "";
    }
    return s.replace('\u00A0', ' ')         // nbsp -> space
        .replace('\u2022', ' ')         // bullet -> space
        .replace('\u2013', '-')         // en dash -> hyphen (optional)
        .replace('\u2014', '-')         // em dash -> hyphen (optional)
        .replaceAll("\\s+", " ")       // collapse whitespace
        .trim()
        .toLowerCase();
  }

  public String slugify(String txt) {
    String s = txt.toLowerCase(Locale.ROOT).trim();
    s = s.replace("&", "and").replace("+", "plus");
    s = s.replaceAll("[^a-z0-9\\s-]", "");
    s = s.replaceAll("\\s+", "-");
    s = s.replaceAll("-{2,}", "-");
    return s;
  }

  public String normalizeKey(String s){ return s==null?"":s.trim().toLowerCase(Locale.ROOT); }

  public String cssEscape(String s) {
    return s.replace("\\", "\\\\")
        .replace("\"", "\\\"");
  }
}