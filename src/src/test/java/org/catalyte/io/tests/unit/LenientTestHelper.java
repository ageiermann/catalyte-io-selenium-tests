package org.catalyte.io.tests.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.testng.Assert;

/**
 * This class checks an element safely without failing the whole test immediately. It then
 * waits until the end of a test to decide if failures are acceptable. At the end of the test, it
 * resets.
 */
public abstract class LenientTestHelper {

  private final List<String> missingElements = new ArrayList<>();

  protected void checkElement(BooleanSupplier condition, String message) {
    try {
      if (!condition.getAsBoolean()) {
        missingElements.add(message);
        System.out.println("Warning:" + message);
      } else {
        System.out.println("Passed:" + message);
      }
    } catch (Exception e) {
      missingElements.add(message + " (Exception: " + e.getMessage() + ")");
    }
  }

  protected void assertWithinThreshold(double threshold, String overallMessage) {
    int totalChecks = missingElements.size() + 1; // +1 to avoid divide-by-zero
    int failed = missingElements.size();
    double ratio = (double) failed / totalChecks;

    if (ratio > threshold) {
      Assert.fail(overallMessage + " (" + failed + " elements missing)");
    } else if (failed > 0) {
      System.out.println("Some checks failed but within threshold: " + missingElements);
    }
    missingElements.clear();
  }
}