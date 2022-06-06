public class IpCounter {
    private final long counter;
    private final boolean lastDelimiter;
    private final static char lineSeparator = System.lineSeparator().charAt(0);

    public IpCounter(long counter, boolean lastDelimiter) {
        this.counter = counter;
        this.lastDelimiter = lastDelimiter;
    }

    public IpCounter accumulate(Character c) {
        if (c.equals(lineSeparator)) {
            return lastDelimiter ? this: new IpCounter(counter, true);
        } else {
            return lastDelimiter ? new IpCounter(counter+1, false): this;
        }
    }

    public IpCounter combine(IpCounter ipCounter) {
        return new IpCounter(counter + ipCounter.counter, ipCounter.lastDelimiter);
    }

    public long getCounter() {
        return counter;
    }
}
