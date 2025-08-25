package org.catalyte.io.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtil {

  private static final Properties props = new Properties();

  static {
    try (InputStream input = ConfigUtil.class.getClassLoader()
        .getResourceAsStream("test.properties")) {
      if (input == null) {
        throw new RuntimeException("Cannot find test.properties in resources");
      }
      props.load(input);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load test.properties", e);
    }
  }

  public static double getThreshold(String key, double defaultValue) {
    String value = props.getProperty(key);
    if (value == null) {
      return defaultValue;
    }
    try {
      return Double.parseDouble(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
}