/**
 * Module for 4th, 5th and 6th of
 * <a href="https://www.kgeorgiy.info/courses/java-advanced/">Java Advanced</a> course. homework solutions.
 *
 * @author Andrey Belickij
 */
module info.kgeorgiy.ja.belickij {

    requires info.kgeorgiy.java.advanced.implementor;

    opens info.kgeorgiy.ja.belickij.implementor;
    exports info.kgeorgiy.ja.belickij.implementor;

    requires java.compiler;
}