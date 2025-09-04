package org.catalyte.io.utils;

import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

  private static WebDriver driver;

  public static void setDriver(WebDriver driverInstance) {
    driver = driverInstance;
  }

  @Override
  public void onTestFailure(ITestResult result) {
    if (driver != null) {
      ScreenshotUtil.takeScreenshot(driver);
    }
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    if (driver != null) {
      ScreenshotUtil.takeScreenshot(driver);
    }
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    if (driver != null) {
      ScreenshotUtil.takeScreenshot(driver);
    }
  }
}