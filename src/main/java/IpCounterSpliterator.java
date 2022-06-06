import java.util.Spliterator;
import java.util.function.Consumer;

public class IpCounterSpliterator implements Spliterator<Character> {
    private final static char lineSeparator = System.lineSeparator().charAt(0);
    private final String string;
    private int currentChar = 0;

    public IpCounterSpliterator (String string) {
        this.string = string;
    }

    @Override
    public boolean tryAdvance (Consumer<? super Character> action) {
        action.accept(string.charAt(currentChar++));
        return currentChar < string.length();
    }

    @Override
    public Spliterator<Character> trySplit () {
        int currentSize = string.length() - currentChar;
        if (currentSize < 10) {
            return null;
        }

        for (int splitPos = currentSize / 2 + currentChar; splitPos < string.length(); splitPos++) {
            if (string.charAt(splitPos) == lineSeparator) {
                Spliterator<Character> spliterator = new IpCounterSpliterator(string.substring(currentChar, splitPos));
                currentChar = splitPos;
                return spliterator;
            }
        }
        return null;
    }

    @Override
    public long estimateSize () {
        return string.length() - currentChar;
    }

    @Override
    public int characteristics () {
        return ORDERED + SIZED + SUBSIZED + NONNULL + IMMUTABLE;
    }
}
