package info.kgeorgiy.ja.belickij.rmi;

import org.junit.jupiter.api.DisplayName;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Client Test")
public final class ClientTest {

    public ClientTest() {

    }

    private final static String name = "Andrey";
    private final static String surname = "Belitsckiy";
    private final static String passportId = "Sir_Dratuti";
    private final static String accountId = "11";

    @Test
    public void test01_initializationMore() {
        assertThrows(IllegalArgumentException.class, () -> Client.main(generateSimpleArgs(6)));
    }

    @Test
    public void test02_initializationLess() {
        assertThrows(IllegalArgumentException.class, () -> Client.main(generateSimpleArgs(4)));
    }

    @Test
    public void test03_nullInitialization() {
        assertThrows(IllegalArgumentException.class, () -> Client.main((String) null));
    }

    @Test
    public void test04_incorrectAmount() {
        assertThrows(IllegalArgumentException.class, () -> Client.main(generateNormalArgs()));
    }


    private String[] generateSimpleArgs(final int size) {
        final String[] args = new String[size];
        for (int i = 0; i < size; i++) {
            args[i] = String.valueOf(i);
        }
        return args;
    }

    private String[] generateNormalArgs() {
        return new String[]{name, surname, passportId, accountId, "18*9!%;"};
    }

}
