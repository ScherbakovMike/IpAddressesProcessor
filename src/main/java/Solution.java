import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import java.time.Instant;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import lombok.SneakyThrows;

class Solution {

    private static final Random random = new Random();
    private static final String lineSeparator = System.lineSeparator();

    @SneakyThrows
    public static void main (String[] args) {

        generateTestFile();
        var path = getTestFilePath();
        var startTime  = Instant.now();
        try (var reader = new RandomAccessFile(path.toFile(), "r"); var byteChannel = reader.getChannel()) {
            var ipReader = new IpReader(path, 0, Files.size(path) - 1);
            System.out.printf("%s: Reading process has been started.\n", new Date());
            new ForkJoinPool().invoke(ipReader);
            System.out.printf("%s: Reading process has been finished.\n", new Date());
            System.out.printf("Result: %d unique IPs.\n",IpReader.countOfUnique());
        }
        var endTime = Instant.now();
        System.out.printf("File scanned  in %d seconds.\n",
            (endTime.getEpochSecond()-startTime.getEpochSecond()));
    }

    @SneakyThrows
    private static void generateTestFile () {
        System.out.printf("Test file generating has been started.\n");
        int count = 1000_000;
        Path tesFileResource = getTestFilePath();
        var startTime  = Instant.now();
        try (var outputStream = Files.newOutputStream(tesFileResource, CREATE, APPEND);
             var bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            for (int i = 0; i < count; i++) {
                bufferedWriter.write(getRandomIp()+lineSeparator);
            }
        }
        var endTime = Instant.now();
        System.out.printf("Test file generated with %d IPs in %d seconds.\n",
            count, (endTime.getEpochSecond()-startTime.getEpochSecond()));
    }

    private static Path getTestFilePath () throws URISyntaxException {
        var resourcesPath = Paths.get(Solution.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        return resourcesPath.resolve(Paths.get("testFile.txt"));
    }

    private static String getRandomIp () {
        return (random.nextInt(255)
            + "."
            + random.nextInt(255)
            + "." +
            random.nextInt(255)
            + "." + random.nextInt(255));
    }
}