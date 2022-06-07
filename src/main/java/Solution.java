import java.io.BufferedReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.SneakyThrows;

class Solution {

    private static Random random = new Random();
    private static String lineSeparator = System.lineSeparator();

    @SneakyThrows
    public static void main (String[] args) {
        //generateTestFile();
        try (var reader = Files.newBufferedReader(getTestFilePath())) {
            //System.out.println(reader.lines().skip(1000_0000).limit(1000_000).distinct().count());
            String line = null;
            AtomicLong counter = new AtomicLong();
            Stream<String> stream = breakStream(
                Stream.generate(() -> new IntStream().map(operand -> )lineGenerator(reader, counter.getAndIncrement())),
                Objects::isNull);
//            Stream<String> stream = Stream.generate(() -> lineGenerator(reader, counter.getAndIncrement()))
//                                          .takeWhile(Objects::nonNull)
//                                          .distinct();
            System.out.println(stream.distinct().count());
        }

        //var count = Files.lines(getTestFilePath()).distinct().count();
        //System.out.println(count);
        //        Path testFile = Path.of(Objects.requireNonNull(Solution.class.getResource("testFile.txt")).getFile());
        //        var pool = new ForkJoinPool();
        //        var result = pool.invoke(new IpReader(testFile, 0, (int) Files.size(testFile) - 1));
        //        System.out.println(result.parallelStream().unordered().distinct().count());
    }

    @SneakyThrows
    private static String lineGenerator (BufferedReader reader, long counter) {
        if ((counter % 1000_000) == 0) {
            System.gc();
        }
        return reader.readLine();
    }

    public static <T> Stream<T> breakStream(Stream<T> stream, Predicate<T> terminate) {
        final Iterator<T> original = stream.iterator();
        Iterable<T> iter = () -> new Iterator<T>() {
            T t;
            boolean hasValue = false;

            @Override
            public boolean hasNext() {
                if (!original.hasNext()) {
                    return false;
                }
                t = original.next();
                hasValue = true;
                return !terminate.test(t);
            }

            @Override
            public T next() {
                if (hasValue) {
                    hasValue = false;
                    return t;
                }
                return t;
            }
        };

        return StreamSupport.stream(iter.spliterator(), false);
    }
    @SneakyThrows
    private static void generateTestFile () {
        int count = 1000_000;
        Path tesFileResource = getTestFilePath();

        try (var outputStream = Files.newOutputStream(tesFileResource, CREATE, APPEND)) {
            for (int i = 0; i < count; i++) {
                outputStream.write(getRandomIp());
                outputStream.write(lineSeparator.getBytes());
            }
        }
    }

    private static Path getTestFilePath () throws URISyntaxException {
        var resourcesPath = Paths.get(Solution.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        return resourcesPath.resolve(Paths.get("testFile.txt"));
    }

    private static byte[] getRandomIp () {
        return (random.nextInt(255) + "." + random.nextInt(255) + "." + random.nextInt(255) + "." + random.nextInt(255)).getBytes();
    }
}