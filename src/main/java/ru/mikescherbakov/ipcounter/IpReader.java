package ru.mikescherbakov.ipcounter;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

@Slf4j
public class IpReader extends RecursiveTask<Long> {
    private final Path file;
    private final long startPosition;
    private final long endPosition;
    private static final long THRESHOLD = 100_000;
    private static final String DELIMITER = System.lineSeparator();
    private static final int DELIMITER_LENGTH = DELIMITER.length();
    private static final int ARRAY_LEN_DIVIDER = 4;
    private static final int ARRAY_LEN = Integer.MAX_VALUE / ARRAY_LEN_DIVIDER;
    private static final int ARRAYS_COUNT = 8;
    private static final byte[][] ipCollection;
    private static final long SEEK_BUFFER_SIZE = ("255.255.255.255" + DELIMITER + "255.255.255.255").getBytes().length;

    static {
        System.out.printf(IPService.appProps.getProperty("log.memory_allocation_start"), new Date());
        var startTime = Instant.now();
        ipCollection = new byte[ARRAY_LEN][ARRAYS_COUNT];
        var endTime = Instant.now();
        System.out.printf(IPService.appProps.getProperty("log.memory_allocation_finish"), new Date(),
                (endTime.getEpochSecond() - startTime.getEpochSecond())
        );
    }

    public IpReader(Path file, long startPosition, long endPosition) {
        this.file = file;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    @Override
    protected Long compute() {
        long size = endPosition - startPosition + 1;
        if (size < THRESHOLD) {
            return extractIPNumbers();
        }

        long delimiterPosition = findDelimiterPosition(file, startPosition, endPosition);
        if (delimiterPosition < 0) {
            return extractIPNumbers();
        } else {
            var leftTask = new IpReader(file, startPosition, delimiterPosition);
            var rightTask = new IpReader(file, delimiterPosition + DELIMITER_LENGTH, endPosition);
            leftTask.fork();
            var rightResult = rightTask.compute();
            var leftResult = leftTask.join();
            return leftResult + rightResult;
        }
    }

    public static long countOfUnique() {
        System.out.printf(IPService.appProps.getProperty("log.counting_start"), new Date());
        var startTime = Instant.now();
        long sum = 0L;
        for (int i = 0; i < ARRAY_LEN; i++) {
            for (int j = 0; j < ARRAYS_COUNT; j++) {
                sum += ipCollection[i][j];
            }
        }
        var endTime = Instant.now();
        System.out.printf(IPService.appProps.getProperty("log.counting_finish"), new Date(),
                (endTime.getEpochSecond() - startTime.getEpochSecond()));
        return sum;
    }

    private long extractIPNumbers() {
        try {
            var list = new String(readBytes(file, startPosition, endPosition).array())
                    .trim().split(DELIMITER);
            setIpToArray(list);
            return list.length;
        } catch (Exception e) {
            log.error(this.getClass().getSimpleName(), e);
            return 0L;
        }
    }

    @SneakyThrows
    private ByteBuffer readBytes(Path path, long start, long end) {
        long size = end - start + 1;

        try (var reader = new RandomAccessFile(path.toFile(), "r");
             var byteChannel = reader.getChannel()) {
            byteChannel.position(start);
            var buffer = ByteBuffer.allocate((int) size);
            byteChannel.read(buffer);
            return buffer;
        } catch (Exception e) {
            log.error(getClass().getSimpleName(), e);
            return ByteBuffer.allocate(0);
        }
    }

    private long findDelimiterPosition(Path file, long start, long end) {
        var delimiterPosition = -1L;
        long middle = (start + end) / 2;
        long left = middle - SEEK_BUFFER_SIZE / 2;
        long right = middle + SEEK_BUFFER_SIZE / 2;
        var buffer = readBytes(file, left, right);
        var string = new String(buffer.array());
        var pos = string.indexOf(DELIMITER);
        if (pos >= 0) {
            delimiterPosition = left + pos;
        }
        return delimiterPosition;
    }

    private static long parseIpShift(String address) {
        long result = 0L;
        for (String part : address.split(Pattern.quote("."))) {
            result = result << 8;
            result |= Integer.parseInt(part);
        }
        return result;
    }

    private static void setIpToArray(String[] list) {
        for (String address : list) {
            long ip = parseIpShift(address);
            int arrayNumber = (int) (ip / ARRAY_LEN);
            int arrayPosition = (int) (ip % ARRAY_LEN);
            ipCollection[arrayPosition][arrayNumber] = 1;
        }
    }
}
