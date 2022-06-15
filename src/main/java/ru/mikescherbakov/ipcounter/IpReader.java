package ru.mikescherbakov.ipcounter;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IpReader extends RecursiveTask<Long> {
    private final Path file;
    private final long startPosition;
    private final long endPosition;
    private static final long THRESHOLD = 100_000;
    private static final String DELIMITER = "\n";
    private static final int DELIMITER_LENGTH = DELIMITER.length();
    private static final String MAX_IP = "255.255.255.255";
    private static final long SEEK_BUFFER_SIZE = (MAX_IP + DELIMITER + MAX_IP).getBytes().length;

    private final Queue<String[]> ipSetsQueue;

    public IpReader (Path file, long startPosition, long endPosition, Queue<String[]> ipSetsQueue) {
        this.file = file;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.ipSetsQueue = ipSetsQueue;
    }

    @Override
    protected Long compute () {
        long size = endPosition - startPosition + 1;
        if (size < THRESHOLD) {
            return (long) extractIPNumbers().length;
        }

        long delimiterPosition = findDelimiterPosition(file, startPosition, endPosition);
        if (delimiterPosition < 0) {
            return (long) extractIPNumbers().length;
        } else {
            var leftTask = new IpReader(file, startPosition, delimiterPosition, ipSetsQueue);
            var rightTask = new IpReader(file, delimiterPosition + DELIMITER_LENGTH, endPosition, ipSetsQueue);
            leftTask.fork();
            var rightResult = rightTask.compute();
            var leftResult = leftTask.join();
            return leftResult + rightResult;
        }
    }

    private String[] extractIPNumbers () {
        try {
            var list = new String(readBytes(file, startPosition, endPosition).array()).trim().split(DELIMITER);
            ipSetsQueue.add(list);
            return list;
        } catch (Exception e) {
            log.error(this.getClass().getSimpleName(), e);
            throw new RuntimeException(e);
        }
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
