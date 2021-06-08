package info.kgeorgiy.ja.belickij.rmi;

import org.junit.BeforeClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.Test;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Server Test")
public final class ServerTest {

    private static Bank bank;

    public ServerTest() {

    }

    @BeforeClass
    public static void initialize() throws RemoteException, NotBoundException {
        Registry registry;
        try {
            registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        } catch (final ExportException ignored) {
            registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
        }
        registry.rebind("//localhost/bank", new RemoteBank(Registry.REGISTRY_PORT));
        bank = (Bank) registry.lookup("//localhost/bank");
    }


    @Test
    public void test01_sorryNobody() throws RemoteException {
        assertNull(bank.getRemotePersonById("-49"));
    }

    @Test
    public void test02_ohHello() throws RemoteException {
        bank.createPerson("Nikolay", "Vedernikov", "1111");
        assertNotNull(bank.getRemotePersonById("1111"));
    }

    @Test
    public void test03_newAccount() throws RemoteException {
        final Account account = bank.createAccount("123", bank.createPerson("Anton", "Dubrovin", "2002"));
        assertEquals(0, account.getAmount());
    }

    @Test
    public void test04_remoteMoneyOperations() throws RemoteException {
        bank.createPerson("Andrey", "Belitsckiy", "1515");
        final Person remotePerson = bank.getRemotePersonById("1515");
        bank.createAccount("0607", remotePerson);
        final Account account = bank.getRemoteAccount("0607", remotePerson);
        account.setAmount(account.getAmount() + 150);
        assertEquals(150, bank.getRemoteAccount("0607", remotePerson).getAmount());
    }

    @Test
    public void test05_localMoneyOperations() throws RemoteException {
        bank.createPerson("Milena", "Bulkina", "1337");
        final Person localPerson = bank.getLocalPersonById("1337");
        bank.createAccount("15000", localPerson);
        final Account account = bank.getLocalAccount("15000", localPerson);
        account.setAmount(account.getAmount() + 200);
        assertEquals(0, bank.getRemoteAccount("15000", localPerson).getAmount());
    }

    @Test
    public void test06_selfIdentification() throws RemoteException {
        final Person person = bank.createPerson("Andrey", "Belitsckiy", "1515");
        final Person remotePerson = bank.getRemotePersonById("1515");
        assertEquals(person, remotePerson);
    }

    @Test
    public void test07_localSelfIdentification() throws RemoteException {
        final Person person = bank.createPerson("Nikolay", "Rakov", "3336");
        final Person localPerson = bank.getLocalPersonById("3336");
        assertEquals(person, localPerson);
    }

    @Test
    public void test08_parallelSameIncreasing() throws RemoteException {
        bank.createPerson("Polina", "Stankevich", "5746");
        final Person person = bank.getRemotePersonById("5746");
        bank.createAccount("010302", person);
        assertEquals(0, bank.getRemoteAccount("010302", person).getAmount());
        final CountDownLatch latch = new CountDownLatch(10);
        final ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 1; i <= 10; i++) {
            final int finalI = i;
            executorService.submit(() -> {
                try {
                    final Account account = bank.getRemoteAccount("010302", bank.getRemotePersonById("5746"));
                    account.setAmount(account.getAmount() + finalI);
                    latch.countDown();
                } catch (final RemoteException ignored) {
                }
            });
        }
        try {
            latch.await();
        } catch (final InterruptedException ignored) {
        } finally {
            assertEquals(55, bank.getRemoteAccount("010302", person).getAmount());
        }
    }

    @Test
    public void test09_parallelDifferentIncreasing() throws RemoteException {
        final CountDownLatch latch = new CountDownLatch(10);
        final ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            final int finalI = i;
            executorService.submit(() -> {
                try {
                    final Person person = bank.createPerson(String.valueOf(finalI), String.valueOf(finalI), String.valueOf(finalI));
                    final Account account = bank.createAccount(String.valueOf(finalI), person);
                    for (int j = 0; j < 3; j++) {
                        account.setAmount(account.getAmount() + 150);
                    }
                    latch.countDown();
                } catch (final RemoteException exception) {
                    exception.printStackTrace();
                }
            });
        }

        try {
            latch.await();
        } catch (final InterruptedException ignored) {
        } finally {
            for (int i = 0; i < 10; i++) {
                final Person person = bank.getRemotePersonById(String.valueOf(i));
                assertEquals(450, bank.getRemoteAccount(String.valueOf(i), person).getAmount());
            }
        }
    }

    @Test
    public void test10_remoteEquality() throws RemoteException {
        bank.createAccount("1", bank.createPerson("Andrew", "Stankevich", "1111"));
        final Person remotePerson = bank.getRemotePersonById("1111");
        final Account account = bank.getRemoteAccount("1", remotePerson);
        account.setAmount(account.getAmount() + 150);
        assertEquals(bank.getRemoteAccount("1", remotePerson).getAmount(), account.getAmount());
    }

}
