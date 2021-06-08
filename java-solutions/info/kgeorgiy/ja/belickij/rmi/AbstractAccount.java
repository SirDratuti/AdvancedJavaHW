package info.kgeorgiy.ja.belickij.rmi;

import java.rmi.RemoteException;

public abstract class AbstractAccount implements Account{

    protected int amount;
    protected final String id;

    public AbstractAccount(final String id, final int amount) {
        this.id = id;
        this.amount = amount;
    }

    @Override
    public synchronized int getAmount() throws RemoteException {
        System.out.println("Getting amount of money for account " + id + " : " + amount);
        return amount;
    }

    @Override
    public synchronized void setAmount(final int amount) throws RemoteException {
        System.out.println("Setting amount of money for account " + id + " : " + amount);
        this.amount = amount;
    }

}
