package info.kgeorgiy.ja.belickij.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();

    public RemoteBank(final int port) throws RemoteException {
        this.port = port;
    }

    @Override
    public synchronized Account createAccount(final String id, final Person owner) throws RemoteException {
        if (owner == null || owner.getPassportId() == null) {
            System.out.println("Incorrect input for creating");
            return null;
        }
        final RemoteAccount account = new RemoteAccount(id);
        if (accounts.putIfAbsent(owner.getPassportId() + ":" + id, account) == null) {
            System.out.println("Creating account for " + owner.print());
            UnicastRemoteObject.exportObject(account, port);
            owner.addAccount(id, 0);
            return account;
        } else {
            System.out.println("Account is already created");
            return getRemoteAccount(id, owner);
        }
    }

    @Override
    public synchronized Account getLocalAccount(final String id, final Person owner) throws RemoteException {
        System.out.println("Getting local account for " + owner.print());
        return new LocalAccount(id, accounts.get(owner.getPassportId() + ":" + id).getAmount());
    }

    @Override
    public synchronized Account getRemoteAccount(final String id, final Person owner) throws RemoteException {
        System.out.println("Getting remote account for " + owner.print());
        return accounts.get(owner.getPassportId() + ":" + id);
    }

    @Override
    public synchronized Person createPerson(final String name, final String surname, final String passportId) throws RemoteException {
        final RemotePerson remotePerson = new RemotePerson(name, surname, passportId);
        if (persons.putIfAbsent(passportId, remotePerson) == null) {
            System.out.println("Registring new person " + remotePerson.print());
            UnicastRemoteObject.exportObject(remotePerson, port);
            return remotePerson;
        } else {
            System.out.println("This person already registered " + remotePerson.print());
            return getRemotePersonById(passportId);
        }
    }

    @Override
    public synchronized Person getRemotePersonById(final String passportId) {
        System.out.println("Retrieving remote person by passport ID: " + passportId);
        return persons.get(passportId);
    }

    @Override
    public synchronized Person getLocalPersonById(final String passportId) throws RemoteException {
        if (passportId != null) {
            System.out.println("Retrieving local person by passport ID: " + passportId);
            final Person person = persons.get(passportId);
            final Map<String, ? extends Account> currentAccounts = person.getAllAccounts();
            final Map<String, Account> localAccounts = new ConcurrentHashMap<>();
            for (final String key : currentAccounts.keySet()) {
                final Account account = getLocalAccount(key, person);
                localAccounts.put(key, account);
            }
            return new LocalPerson(person.getName(), person.getSurname(), person.getPassportId(), localAccounts);
        } else {
            return null;
        }
    }

}
