package info.kgeorgiy.ja.belickij.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {

    Account createAccount(String id, Person owner) throws RemoteException;

    Account getLocalAccount(String id, Person owner) throws RemoteException;

    Account getRemoteAccount(String id, Person owner) throws RemoteException;

    Person getLocalPersonById(String id) throws RemoteException;

    Person getRemotePersonById(String id) throws RemoteException;

    Person createPerson(String name, String surname, String passportId) throws RemoteException;
}
