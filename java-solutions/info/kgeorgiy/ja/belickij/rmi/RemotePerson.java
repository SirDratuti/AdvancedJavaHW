package info.kgeorgiy.ja.belickij.rmi;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RemotePerson extends AbstractPerson {

    private final Map<String, Account> accounts;

    public RemotePerson(final String name, final String surname, final String passportId) {
        super(name, surname, passportId);
        this.accounts = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized Account getAccountById(final String id) {
        System.out.println("Getting account for :" + name + " " + surname + " with id: " + id);
        return accounts.get(id);
    }

    @Override
    public synchronized Map<String, Account> getAllAccounts() {
        return accounts;
    }

    @Override
    public synchronized void addAccount(final String id, final int amount) {
        accounts.put(id, new RemoteAccount(id, amount));
    }
}
