package info.kgeorgiy.ja.belickij.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Person extends Remote {

    String getName() throws RemoteException;

    String getSurname() throws RemoteException;

    String getPassportId() throws RemoteException;

    Account getAccountById(String id) throws RemoteException;

    Map<String, ? extends Account> getAllAccounts() throws RemoteException;

    void addAccount(String id, int amount) throws RemoteException;

    String print() throws RemoteException;
}
