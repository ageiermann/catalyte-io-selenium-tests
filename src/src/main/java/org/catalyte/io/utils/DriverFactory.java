package org.catalyte.io.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.edge.EdgeDriver;

public class DriverFactory {
  private static WebDriver driver;

  public static WebDriver getDriver(String browser) {
    if (driver == null) {
      switch (browser.toLowerCase()) {
        case "firefox":
          driver = new FirefoxDriver();
          break;
        case "edge":
          driver = new EdgeDriver();
          break;
        default:
          driver = new ChromeDriver();
          break;
      }
      driver.manage().window().maximize();
    }
    return driver;
  }

  public static void quitDriver() {
    if (driver != null) {
      driver.quit();
      driver = null;
    }
  }
}