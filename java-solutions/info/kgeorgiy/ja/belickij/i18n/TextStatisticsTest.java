package info.kgeorgiy.ja.belickij.i18n;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({EnglishTest.class, RussianTest.class, UkrainianTest.class})
public class TextStatisticsTest {

    public static void main(final String[] args) {

        final Result result = JUnitCore.runClasses(TextStatisticsTest.class);
        for (final Failure failure : result.getFailures()) {
            System.out.println("Failure is " + failure.toString());
        }
        if (result.wasSuccessful()) {
            System.out.println("PASSED TESTS");
            System.exit(0);
        } else {
            System.out.println("FAILED TESTS");
            System.exit(1);
        }
    }
}
