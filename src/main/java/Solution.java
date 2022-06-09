import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
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

        try (var reader = new RandomAccessFile(path.toFile(), "r"); var byteChannel = reader.getChannel()) {
            var ipReader = new IpReader(path, 0, Files.size(path) - 1);
            new ForkJoinPool().invoke(ipReader);
            System.out.println(IpReader.countOfUnique());
        }
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