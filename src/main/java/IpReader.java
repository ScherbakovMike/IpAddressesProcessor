import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IpReader extends RecursiveTask<Long> {
    private final Path file;
    private final long start;
    private final long end;

    private static int arrayLenDivider = 4;
    private static int arrayLen = Integer.MAX_VALUE / arrayLenDivider;
    private static int arraysCount = 8;
    private final static byte[][] byteArray = new byte[arrayLen][arraysCount];
    private final static long THRESHOLD = 100_000L;
    private final static String delimiter = System.lineSeparator();

    private final static long seekBufferSize = ("255.255.255.255" + delimiter + "255.255.255.255").getBytes().length;

    public IpReader (Path file, long start, long end) {
        this.file = file;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute () {
        long size = end - start + 1;
        if (size < THRESHOLD) {
            try {
                var list = new ArrayList<>(Arrays.asList(new String(readBytes(file,
                    start,
                    end
                ).array()).split(delimiter)));
                setIpToArray(list);
                return (long) list.size();
            } catch (Exception e) {
                log.error(this.getClass().getSimpleName(), e);
                return 0L;
            }
        }

        long delimiterPosition = findDelimiterPosition(file, start, end);
        if (delimiterPosition < 0) {
            try {
                var list = new ArrayList<>(Arrays.asList(new String(readBytes(file,
                    start,
                    end
                ).array()).split(delimiter)));
                setIpToArray(list);
                return (long) list.size();
            } catch (Exception e) {
                log.error(this.getClass().getSimpleName(), e);
                return 0L;
            }
        } else {
            var leftTask = new IpReader(file, start, delimiterPosition);
            var rightTask = new IpReader(file, delimiterPosition + 1, end);
            leftTask.fork();
            var rightResult = rightTask.compute();
            var leftResult = leftTask.join();
            return leftResult + rightResult;
        }
    }

    @SneakyThrows
    private ByteBuffer readBytes (Path path, long start, long end) {
        long size = end - start + 1;

        try (var reader = new RandomAccessFile(path.toFile(), "r"); var byteChannel = reader.getChannel()) {
            byteChannel.position(start);
            var buffer = ByteBuffer.allocate((int) size);
            byteChannel.read(buffer);
            return buffer;
        } catch (Exception e) {
            log.error(getClass().getSimpleName(), e);
            return null;
        }
    }

    private long findDelimiterPosition (Path file, long start, long end) {
        var delimiterPosition = -1L;
        long middle = (start + end) / 2;
        long left = middle - seekBufferSize / 2;
        long right = middle + seekBufferSize / 2;
        var buffer = readBytes(file, left, right);
        var string = new String(buffer.array());
        var pos = string.indexOf(delimiter);
        if (pos >= 0) {
            delimiterPosition = left + pos;
        }
        return delimiterPosition;
    }

    private long parseIp (String address) {
        long result = 0L;
        for (String part : address.split(Pattern.quote("."))) {
            result = result << 8;
            result |= Integer.parseInt(part);
        }
        if(result<0L) System.out.println(address);
        return result;
    }

    private void setIpToArray (List<String> list) {
        for(int i = 0; i< list.size();i++){
            var address = list.get(i);
            long ip = parseIp(address);
            int arrayNumber = (int) (ip / arrayLen);
            int arrayPosition = (int) (ip % arrayLen);
            byteArray[arrayPosition][arrayNumber] = 1;
        }
    }

    public static long countOfUnique () {
        long sum = 0L;
        for (int i = 0; i < arrayLen; i++) {
            for (int j = 0; j < arraysCount; j++) {
                sum += byteArray[i][j];
            }
        }
        return sum;
    }
}
