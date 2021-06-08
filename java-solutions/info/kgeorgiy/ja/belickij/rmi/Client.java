package info.kgeorgiy.ja.belickij.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Objects;

public final class Client {
    /**
     * Utility class.
     */
    private Client() {
    }

    public static void main(final String... args) throws IllegalArgumentException {
        if (!checkArgs(args)) {
            System.out.println(Arrays.toString(args));
            throw new IllegalArgumentException("Illegal input, must be 5 non-null arguments");
        }
        try {
            final String personName = args[0];
            final String personSurname = args[1];
            final String personPassportId = args[2];
            final String personAccountId = args[3];
            final int amountDelta = Integer.parseInt(args[4]);

            final Bank bank = (Bank) Naming.lookup("//localhost/bank");
            final Person remotePerson = bank.createPerson(personName, personSurname, personPassportId);
            bank.createAccount(personAccountId, remotePerson);
            final Account account = bank.getRemoteAccount(personAccountId, remotePerson);
            if (account != null) {
                final int amount = account.getAmount();
                System.out.println("Updating balance.., current is " + amount);
                account.setAmount(amount + amountDelta);
                System.out.println("Updated, current is " + account.getAmount());
            } else {
                System.out.println("No account in bank");
            }

        } catch (final NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid value of money to add");
        } catch (final NotBoundException exception) {
            System.out.println("Bank is not bound");
        } catch (final MalformedURLException exception) {
            System.out.println("Bank URL is invalid");
        } catch (final RemoteException exception) {
            System.out.println("Can't connect to bank ");
            exception.printStackTrace();
        }

    }

    private static boolean checkArgs(final String[] args) {
        if (args != null && args.length == 5) {
            return Arrays.stream(args).noneMatch(Objects::isNull);
        } else {
            return false;
        }
    }

}
