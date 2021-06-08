package info.kgeorgiy.ja.belickij.concurrent;

public final class Problem {

    private final Runnable problem;
    private final int schedulerIndex;

    public Problem(final Runnable problem, final int schedulerIndex) {
        this.problem = problem;
        this.schedulerIndex = schedulerIndex;
    }

    public int getSchedulerIndex() {
        return schedulerIndex;
    }

    public void begin() {
        problem.run();
    }
}
