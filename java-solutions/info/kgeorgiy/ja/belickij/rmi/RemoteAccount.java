package info.kgeorgiy.ja.belickij.rmi;

public final class RemoteAccount extends AbstractAccount {

    public RemoteAccount(final String id, final int amount) {
        super(id, amount);
    }

    public RemoteAccount(final String id) {
        this(id, 0);
    }

}
