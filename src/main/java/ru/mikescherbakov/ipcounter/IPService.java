package ru.mikescherbakov.ipcounter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import java.time.Instant;
import java.util.BitSet;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IPService {
    private static final String DELIMITER = "\n";
    private static final int BITSET_LEN = Integer.MAX_VALUE;

    private static final Random random = new Random();
    private final ApplicationProperties applicationProperties;

    public IPService (ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public long countOfUnique (BitSet[] ipBitSet) {
        log.info(String.format(applicationProperties.getAppProps().getProperty("log.counting_start"), new Date()));
        var startTime = Instant.now();
        long sum = 0L;
        sum += ipBitSet[0].cardinality();
        sum += ipBitSet[1].cardinality();
        var endTime = Instant.now();
        var executionTime = endTime.getEpochSecond() - startTime.getEpochSecond();
        log.info(String.format(applicationProperties.getAppProps().getProperty("log.counting_finish"),
            new Date(),
            executionTime
        ));
        return sum;
    }

    public Long parseFile (String path) {
        try {
            var startTime = Instant.now();
            var sourcePath = Path.of(path);
            BitSet[] ipBitSet = new BitSet[]{new BitSet(BITSET_LEN), new BitSet(BITSET_LEN)};

            var ipReader = new IpReader(sourcePath, 0, Files.size(sourcePath) - 1, ipBitSet);
            log.info(String.format(applicationProperties.getAppProps().getProperty("log.reading_start"), new Date()));

            new ForkJoinPool().invoke(ipReader);
            var countIPs = countOfUnique(ipBitSet);

            log.info(String.format(applicationProperties.getAppProps().getProperty("log.reading_finish"), new Date()));
            var endTime = Instant.now();
            var executionTime = endTime.getEpochSecond() - startTime.getEpochSecond();
            log.info(String.format(applicationProperties.getAppProps().getProperty("log.whole_time"),
                new Date(),
                executionTime
            ));

            return countIPs;
        } catch (IOException e) {
            log.error(IPService.class.getSimpleName(), e);
            throw new RuntimeException(e);
        }
    }

    public void generateTestFile (String path, Long count) throws IOException {
        log.info(String.format(applicationProperties.getAppProps().getProperty("log.generating_start"), new Date()));
        Path testFileResource = Path.of(path);
        var startTime = Instant.now();
        try (var outputStream = Files.newOutputStream(testFileResource,
            CREATE,
            APPEND
        ); var bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream)))
        {
            for (int i = 0; i < count; i++) {
                bufferedWriter.write(getRandomIp() + DELIMITER);
            }
        }
        var endTime = Instant.now();
        var executionTime = endTime.getEpochSecond() - startTime.getEpochSecond();
        log.info(String.format(applicationProperties.getAppProps().getProperty("log.generating_finish"),
            new Date(),
            path,
            count,
            executionTime
        ));
    }

    private String getRandomIp () {
        return
            new StringBuilder()
                .append(random.nextInt(255))
                .append(".")
                .append(random.nextInt(255))
                .append(".")
                .append(random.nextInt(255))
                .append(".")
                .append(random.nextInt(255))
                .toString();
    }
}
