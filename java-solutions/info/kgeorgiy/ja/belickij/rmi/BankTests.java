package info.kgeorgiy.ja.belickij.rmi;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ClientTest.class, ServerTest.class})
public final class BankTests {

    public static void main(final String[] args) {

        final Result result = JUnitCore.runClasses(BankTests.class);
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
