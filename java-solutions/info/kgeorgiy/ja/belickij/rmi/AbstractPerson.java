package info.kgeorgiy.ja.belickij.rmi;

import java.util.Objects;

public abstract class AbstractPerson implements Person {

    protected String name;
    protected String surname;
    protected String passportId;

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getPassportId() {
        return passportId;
    }

    public AbstractPerson(final String name, final String surname, final String passportId) {
        this.name = name;
        this.surname = surname;
        this.passportId = passportId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractPerson)) return false;
        final AbstractPerson that = (AbstractPerson) o;
        return getName().equals(that.getName()) &&
                getSurname().equals(that.getSurname()) &&
                getPassportId().equals(that.getPassportId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getSurname(), getPassportId());
    }

    @Override
    public String print() {
        return (name + " " + surname + " {" + passportId + "}");
    }
}
