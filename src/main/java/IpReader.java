import com.sun.nio.file.ExtendedOpenOption;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.RecursiveTask;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static java.nio.file.StandardOpenOption.READ;

@Slf4j
public class IpReader extends RecursiveTask<List<String>> {
    private final Path file;
    private final int start;
    private final int end;

    private final static long THRESHOLD = 10_000_000L;
    private final static String delimiter = System.lineSeparator();

    private final static long seekBufferSize = ("255.255.255.255" + delimiter + "255.255.255.255").getBytes().length;

    public IpReader(Path file, int start, int end) {
        this.file = file;
        this.start = start;
        this.end = end;
    }

    @Override
    protected List<String> compute() {
        int size = end - start + 1;
        if ((end - start + 1) < THRESHOLD) {
            return Arrays.asList(new String(readBytes(file, start, end).array()).split(delimiter));
        }

        ByteBuffer buffer = readBytes(file, start, end);
        long delimiterPosition = findDelimiterPosition();
        if (delimiterPosition<0) {
            return Arrays.asList(new String(buffer.array()).split(delimiter));
        } else {
            var leftTask = new IpReader(file, start, delimiterPosition);
            var rightTask = new IpReader(file, delimiterPosition+1, end);
            leftTask.fork();
            var rightResult = rightTask.compute();
            var leftResult = leftTask.join();
            leftResult.addAll(rightResult);
            return leftResult;
        }
    }

    @SneakyThrows
    private ByteBuffer readBytes(Path path, long start, long end) {
        long size = end - start + 1;

        try (var byteChannel = Files.newByteChannel(path, READ)) {
            byteChannel.position(start);
            var buffer = ByteBuffer.allocate((int) size);
            byteChannel.read(buffer);
            return buffer;
        } catch (Exception e) {
            log.error(getClass().getSimpleName(), e);
            return null;
        }
//
//        int size = end - start + 1;
//        int bufferSize = (size / 4096 + 1) * 4096;
//        try (var inputStream = Files.newInputStream(file, ExtendedOpenOption.DIRECT)) {
//            byte[] buffer = new byte[bufferSize];
//            inputStream.readNBytes(buffer, start, bufferSize);
//            return buffer;
//        } catch (Exception e) {
//            log.error(getClass().getSimpleName(), e);
//            return new byte[0];
//        }
    }

    private long findDelimiterPosition(ByteBuffer buffer) {
        long bufferSize = buffer.capacity();
        long middle = bufferSize / 2;
        long testBufferSize = Math.min(seekBufferSize, bufferSize);
        long testBufferStart = middle - testBufferSize / 2;
        long testBufferEnd = middle + testBufferSize / 2;
        long seekBufferSize = testBufferEnd - testBufferSize + 1;
        byte[] seekBuffer = new byte[(int) seekBufferSize];
        buffer.get(seekBuffer, (int) testBufferStart, (int) seekBufferSize);
        int delimiterPosition = (new String(seekBuffer)).indexOf(delimiter);
        if (delimiterPosition >= 0) {
            delimiterPosition += testBufferStart;
        }
        return delimiterPosition;
    }
}
