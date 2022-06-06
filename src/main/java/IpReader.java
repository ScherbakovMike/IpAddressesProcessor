import com.sun.nio.file.ExtendedOpenOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IpReader extends RecursiveTask<List<String>> {
    private final Path file;
    private final int start;
    private final int end;

    private final static long THRESHOLD = 10_000_000L;
    private final static String delimiter = System.lineSeparator();

    public IpReader (Path file, int start, int end) {
        this.file = file;
        this.start = start;
        this.end = end;
    }

    @Override
    protected List<String> compute () {
        int size = end - start + 1;
        if ((end - start + 1) < THRESHOLD) {
            return Arrays.asList(new String(readBytes(file, start, end)).split(delimiter));

        }

        int middle = size / 2;
        int testBufferSize = Math.min(4096, size);
        int testBufferStart = middle - testBufferSize / 2;
        int testBufferEnd = middle + testBufferSize / 2;

        int posDelimiter = (new String(readBytes(file, testBufferStart, testBufferEnd))).indexOf(delimiter);
        if (posDelimiter == -1) {
            return List.of();
        } else {
            var leftTask = new IpReader(file, start, testBufferStart + posDelimiter);
            var rightTask = new IpReader(file, testBufferStart + posDelimiter, end);
            leftTask.fork();
            var rightResult = rightTask.compute();
            var leftResult = leftTask.join();
            leftResult.addAll(rightResult);
            return leftResult;
        }
    }

    private byte[] readBytes (Path path, int start, int end) {
        int size = end - start + 1;
        int bufferSize = (size / 4096 + 1) * 4096;
        try (var inputStream = Files.newInputStream(file, ExtendedOpenOption.DIRECT)) {
            byte[] buffer = new byte[bufferSize];
            inputStream.readNBytes(buffer, start, bufferSize);
            return buffer;
        } catch (Exception e) {
            log.error(getClass().getSimpleName(), e);
            return new byte[0];
        }
    }
}
