package ru.mikescherbakov.ipcounter;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

@Slf4j
@UtilityClass
class IPService {

    private static final String DELIMITER = System.lineSeparator();
    private static final Random random = new Random();
    public static final Properties appProps = new Properties();

    static {
        try {
            appProps.load(new FileInputStream(String.valueOf(getConfigurationFilePath())));
        } catch (Exception e) {
            log.error(IPService.class.getSimpleName(), e);
        }
    }

    @SneakyThrows
    public static void parseFile(String path) {
        var sourcePath = Path.of(path);
        var startTime = Instant.now();
        try {
            var ipReader = new IpReader(sourcePath, 0, Files.size(sourcePath) - 1);
            System.out.printf(appProps.getProperty("log.reading_start"), new Date());
            new ForkJoinPool().invoke(ipReader);
            System.out.printf(appProps.getProperty("log.reading_finish"), new Date());
            System.out.printf(appProps.getProperty("log.result"), IpReader.countOfUnique());
            var endTime = Instant.now();
            System.out.printf(appProps.getProperty("log.whole_time"), new Date(),
                    (endTime.getEpochSecond() - startTime.getEpochSecond()));
        } catch (Exception e) {
            log.error(IPService.class.getSimpleName(), e);
        }
    }

    @SneakyThrows
    public static void generateTestFile(String path, Long count) {
        System.out.printf(appProps.getProperty("log.generating_start"), new Date());
        Path testFileResource = Path.of(path);
        var startTime = Instant.now();
        try (var outputStream = Files.newOutputStream(testFileResource, CREATE);
             var bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            for (int i = 0; i < count; i++) {
                bufferedWriter.write(getRandomIp() + DELIMITER);
            }
        }
        var endTime = Instant.now();
        System.out.printf(appProps.getProperty("log.generating_finish"), new Date(), path,
                count, (endTime.getEpochSecond() - startTime.getEpochSecond()));
    }

    private static Path getResourcesPath() throws URISyntaxException {
        return Paths.get(IPService.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    public static Path getTestFilePath() throws URISyntaxException {
        Path resourcesPath = getResourcesPath();
        return resourcesPath.resolve(Paths.get("testFile.txt"));
    }

    private static Path getConfigurationFilePath() throws URISyntaxException {
        Path resourcesPath = getResourcesPath();
        return resourcesPath.resolve(Paths.get("application.properties"));
    }

    private static String getRandomIp() {
        return (random.nextInt(255)
                + "."
                + random.nextInt(255)
                + "." +
                random.nextInt(255)
                + "." + random.nextInt(255));
    }
}