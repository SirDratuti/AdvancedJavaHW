package info.kgeorgiy.ja.belickij.rmi;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class LocalPerson extends AbstractPerson implements Externalizable {

    private Map<String, Account> accounts;

    public LocalPerson(final String name, final String surname, final String passportId, final Map<String, Account> accounts) {
        super(name, surname, passportId);
        this.accounts = accounts;
    }

    public LocalPerson(final String name, final String surname, final String passportId) {
        this(name, surname, passportId, new ConcurrentHashMap<>());
    }

    public LocalPerson() {
        this(null, null, null);
    }

    @Override
    public Account getAccountById(final String id) {
        return accounts.get(id);
    }

    @Override
    public Map<String,Account> getAllAccounts() {
        return accounts;
    }

    @Override
    public void addAccount(final String id, final int amount) {
        accounts.put(id, new LocalAccount(id, amount));
    }

    @Override
    public void writeExternal(final ObjectOutput objectOutput) throws IOException {
        objectOutput.writeObject(name);
        objectOutput.writeObject(surname);
        objectOutput.writeObject(passportId);
        objectOutput.writeInt(accounts.size());
        for (final String accountId : accounts.keySet()) {
            objectOutput.writeObject(accountId);
            objectOutput.writeObject(accounts.get(accountId));
        }
    }

    @Override
    public void readExternal(final ObjectInput objectInput) throws IOException, ClassNotFoundException {
        name = (String) objectInput.readObject();
        surname = (String) objectInput.readObject();
        passportId = (String) objectInput.readObject();
        accounts = new HashMap<>();
        final int size = objectInput.readInt();
        for (int i = 0; i < size; i++) {
            accounts.put((String) objectInput.readObject(), (LocalAccount) objectInput.readObject());
        }
    }

}
