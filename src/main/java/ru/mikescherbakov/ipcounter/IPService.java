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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;
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

    public long countOfUnique (BitSet[] ipCollection) {
        log.info(String.format(applicationProperties.getAppProps().getProperty("log.counting_start"), new Date()));
        var startTime = Instant.now();
        long sum = 0L;
        sum += ipCollection[0].cardinality();
        sum += ipCollection[1].cardinality();
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

            var ipSetsQueue = new ConcurrentLinkedQueue<String[]>();
            var ipReader = new IpReader(sourcePath, 0, Files.size(sourcePath) - 1, ipSetsQueue);
            log.info(String.format(applicationProperties.getAppProps().getProperty("log.reading_start"), new Date()));
            CompletableFuture<Long> parsingTask = CompletableFuture.supplyAsync(() -> new ForkJoinPool().invoke(ipReader));
            CompletableFuture<Long> countingTask = parsingTask.thenApplyAsync(parsed -> {
                var ipBitSet = fillBitSet(ipSetsQueue, parsingTask);
                return countOfUnique(ipBitSet);
            });
            var countIPs = countingTask.get();

            log.info(String.format(applicationProperties.getAppProps().getProperty("log.reading_finish"), new Date()));
            var endTime = Instant.now();
            var executionTime = endTime.getEpochSecond() - startTime.getEpochSecond();
            log.info(String.format(applicationProperties.getAppProps().getProperty("log.whole_time"),
                new Date(),
                executionTime
            ));

            System.out.format(applicationProperties.getAppProps().getProperty("log.result"), countIPs);
            return countIPs;
        } catch (IOException | ExecutionException | InterruptedException e) {
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

    private BitSet[] fillBitSet (ConcurrentLinkedQueue<String[]> ipSetsQueue, CompletableFuture<Long> parsingTask) {
        BitSet[] ipCollection = new BitSet[]{new BitSet(BITSET_LEN), new BitSet(BITSET_LEN)};

        while (!ipSetsQueue.isEmpty() || !parsingTask.isDone()) {
            var ipSet = ipSetsQueue.poll();
            if (ipSet != null) {
                setIpToArray(ipSet, ipCollection);
            }
        }
        return ipCollection;
    }

    private void setIpToArray (String[] list, BitSet[] ipCollection) {
        for (String address : list) {
            if (address.isBlank()) {
                continue;
            }
            long ip = parseIpShift(address);
            int arrayNumber = (int) (ip / BITSET_LEN);
            int arrayPosition = (int) (ip % BITSET_LEN);
            ipCollection[arrayNumber].set(arrayPosition);
        }
    }

    private long parseIpShift (String address) {
        long result = 0L;
        // iterate over each octet
        for (String part : address.split(Pattern.quote("."))) {
            // shift the previously parsed bits over by 1 byte
            result = result << 8;
            // set the low order bits to the current octet
            result |= Integer.parseInt(part);
        }
        return result;
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
