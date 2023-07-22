package ru.mikescherbakov.ipcounter;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IpReader extends RecursiveTask<Long> {
    private static final long THRESHOLD = 100_000;
    private static final String DELIMITER = "\n";
    private static final int DELIMITER_LENGTH = DELIMITER.length();
    private static final String MAX_IP = "255.255.255.255";
    private static final long SEEK_BUFFER_SIZE = (MAX_IP + DELIMITER + MAX_IP).getBytes().length;
    private static final int BITSET_LEN = Integer.MAX_VALUE;

    private final Path file;
    private final long startPosition;
    private final long endPosition;
    private final BitSet[] ipSet;

    public IpReader (Path file, long startPosition, long endPosition, BitSet[] ipSet) {
        this.file = file;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.ipSet = ipSet;
    }

    @Override
    protected Long compute () {
        long size = endPosition - startPosition + 1;
        if (size < THRESHOLD) {
            return extractIPNumbers();
        }

        long delimiterPosition = findDelimiterPosition(file, startPosition, endPosition);
        if (delimiterPosition < 0) {
            return extractIPNumbers();
        } else {
            var leftTask = new IpReader(file, startPosition, delimiterPosition, ipSet);
            var rightTask = new IpReader(file, delimiterPosition + DELIMITER_LENGTH, endPosition, ipSet);
            leftTask.fork();
            var rightResult = rightTask.compute();
            var leftResult = leftTask.join();
            return leftResult + rightResult;
        }
    }

    private long extractIPNumbers () {
        try {
            var ipArray = new String(readBytes(file, startPosition, endPosition).array()).trim().split(DELIMITER);
            fillBitSet(ipArray);
            var ipArrayLength = ipArray.length;
            ipArray = null;
            return ipArrayLength;
        } catch (Exception e) {
            log.error(this.getClass().getSimpleName(), e);
            throw new RuntimeException(e);
        }
    }

    private void fillBitSet (String[] ipArray) {
        for (String address : ipArray) {
            if (address.isBlank()) {
                continue;
            }
            long ip = parseIpShift(address);
            int arrayNumber = (int) (ip / BITSET_LEN);
            int arrayPosition = (int) (ip % BITSET_LEN);
            ipSet[arrayNumber].set(arrayPosition);
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
    private ByteBuffer readBytes (Path path, long start, long end) {
        long size = end - start + 1;

        try (var reader = new RandomAccessFile(path.toFile(), "r"); var byteChannel = reader.getChannel()) {
            byteChannel.position(start);
            var buffer = ByteBuffer.allocate((int) size);
            byteChannel.read(buffer);
            return buffer;
        } catch (Exception e) {
            log.error(getClass().getSimpleName(), e);
            throw new RuntimeException(e);
        }
    }

    private long findDelimiterPosition (Path file, long start, long end) {
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
}
