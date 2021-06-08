package info.kgeorgiy.ja.belickij.rmi;

import java.rmi.*;

public interface Account extends Remote {

    int getAmount() throws RemoteException;

    void setAmount(int amount) throws RemoteException;

}