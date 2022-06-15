package ru.mikescherbakov.ipcounter;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import java.time.Instant;
import java.util.BitSet;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import static ru.mikescherbakov.ipcounter.IpAddressesProcessor.getConfigurationFilePath;

@Slf4j
@Service
public class IPService {

    private static final String DELIMITER = "\n";
    private static final int BITSET_LEN = Integer.MAX_VALUE;

    private static final Random random = new Random();
    public static final Properties appProps = new Properties();

    static {
        try {
            appProps.load(new FileInputStream(String.valueOf(getConfigurationFilePath())));
        } catch (Exception e) {
            log.error(IPService.class.getSimpleName(), e);
            System.exit(1);
        }
    }

    private BitSet[] fillBitSet (
        ConcurrentLinkedQueue<String[]> ipSetsQueue, CompletableFuture<Long> parsingTask
    )
    {
        try {
            BitSet[] ipCollection = new BitSet[2];
            ipCollection[0] = new BitSet(BITSET_LEN);
            ipCollection[1] = new BitSet(BITSET_LEN);

            while (!ipSetsQueue.isEmpty() || !parsingTask.isDone()) {
                var ipSet = ipSetsQueue.poll();

                if (ipSet != null) {
                    setIpToArray(ipSet, ipCollection);
                }
            }
            return ipCollection;
        } catch (Exception e) {
            log.error(getClass().getSimpleName(), e);
            throw e;
        }
    }

    private void setIpToArray (String[] list, BitSet[] ipCollection) {
        for (String address : list) {
            if(address.isBlank()) {
                continue;
            }
            long ip = parseIpShift(address);
            int arrayNumber = (int) (ip / BITSET_LEN);
            int arrayPosition = (int) (ip % BITSET_LEN);
            ipCollection[arrayNumber].set(arrayPosition);
        }
    }

    public long countOfUnique (BitSet[] ipCollection) {
        log.info(String.format(IPService.appProps.getProperty("log.counting_start"), new Date()));
        var startTime = Instant.now();
        long sum = 0L;
        sum += ipCollection[0].cardinality();
        sum += ipCollection[1].cardinality();
        var endTime = Instant.now();
        var executionTime = endTime.getEpochSecond() - startTime.getEpochSecond();
        log.info(String.format(IPService.appProps.getProperty("log.counting_finish"), new Date(), executionTime));
        return sum;
    }

    private long parseIpShift (String address) {
        long result = 0L;
        for (String part : address.split(Pattern.quote("."))) {
            result = result << 8;
            result |= Integer.parseInt(part);
        }
        return result;
    }

    public Long parseFile (String path) {
        var startTime = Instant.now();
        var sourcePath = Path.of(path);

        try {
            var ipSetsQueue = new ConcurrentLinkedQueue<String[]>();
            var ipReader = new IpReader(sourcePath, 0, Files.size(sourcePath) - 1, ipSetsQueue);
            log.info(String.format(appProps.getProperty("log.reading_start"), new Date()));
            CompletableFuture<Long> parsingTask = CompletableFuture.supplyAsync(() -> new ForkJoinPool().invoke(ipReader));
            CompletableFuture<Long> countingTask = parsingTask.thenApplyAsync(parsed -> {
                var ipBitSet = fillBitSet(ipSetsQueue, parsingTask);
                return countOfUnique(ipBitSet);
            });
            var countIPs = countingTask.get();

            log.info(String.format(appProps.getProperty("log.reading_finish"), new Date()));
            var endTime = Instant.now();
            var executionTime = endTime.getEpochSecond() - startTime.getEpochSecond();
            log.info(String.format(appProps.getProperty("log.whole_time"), new Date(), executionTime));

            System.out.format(appProps.getProperty("log.result"), countIPs);
            return countIPs;
        } catch (IOException | ExecutionException | InterruptedException e) {
            log.error(IPService.class.getSimpleName(), e);
            throw new RuntimeException(e);
        }
    }

    public void generateTestFile (String path, Long count) throws IOException {
        log.info(String.format(appProps.getProperty("log.generating_start"), new Date()));
        Path testFileResource = Path.of(path);
        var startTime = Instant.now();
        try (var outputStream = Files.newOutputStream(testFileResource,
            CREATE, APPEND
        ); var bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream)))
        {
            for (int i = 0; i < count; i++) {
                bufferedWriter.write(getRandomIp() + DELIMITER);
            }
        }
        var endTime = Instant.now();
        var executionTime = endTime.getEpochSecond() - startTime.getEpochSecond();
        log.info(String.format(appProps.getProperty("log.generating_finish"), new Date(), path, count, executionTime));
    }

    private static String getRandomIp () {
        return (random.nextInt(255) + "." + random.nextInt(255) + "." + random.nextInt(255) + "." + random.nextInt(255));
    }
}
