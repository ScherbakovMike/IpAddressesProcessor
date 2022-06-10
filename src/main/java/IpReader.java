import com.google.common.collect.Sets;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IpReader extends RecursiveTask<Long> {
    private final Path file;
    private final long start;
    private final long end;

    private final static long THRESHOLD = 100_000;
    private final static String delimiter = System.lineSeparator();

    private static int arrayLenDivider = 4;
    private static int arrayLen = Integer.MAX_VALUE / arrayLenDivider;
    private static int arraysCount = 8;
    private final static byte[][] byteArray;
    static {
        System.out.printf("Memory allocation has been started.\n");
        var startTime = Instant.now();
        byteArray = new byte[arrayLen][arraysCount];
        var endTime = Instant.now();
        System.out.printf("Memory has been allocated in %d seconds.\n",
            (endTime.getEpochSecond() - startTime.getEpochSecond())
        );
    }
//
//    private static final Set<Long> set = Sets.newConcurrentHashSet();

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
                var list = new String(readBytes(file, start, end).array()).split(delimiter);
                setIpToArray(list);
                return (long) list.length;
            } catch (Exception e) {
                log.error(this.getClass().getSimpleName(), e);
                return 0L;
            }
        }

        long delimiterPosition = findDelimiterPosition(file, start, end);
        if (delimiterPosition < 0) {
            try {
                var list = new String(readBytes(file, start, end).array()).split(delimiter);
                setIpToArray(list);
                return (long) list.length;
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

    public static long parseIpShift (String address) {
        long result = 0L;
        for (String part : address.split(Pattern.quote("."))) {
            result = result << 8;
            result |= Integer.parseInt(part);
        }
        if (result < 0L) System.out.println(address);
        return result;
    }

//    @SneakyThrows
//    public static long parseIpInetAddr (String address) {
//        return
//            (ByteBuffer.allocate(Integer.BYTES)
//                      .put(InetAddress.getByName(address).getAddress())
//                      .getInt(0)
//                +Integer.MAX_VALUE);
//    }

    private void setIpToArray (String[] list) {
        for (String address : list) {
            long ip = parseIpShift(address);
//            long ip = parseIpInetAddr(address);
//            set.add(ip);
            int arrayNumber = (int) (ip / arrayLen);
            int arrayPosition = (int) (ip % arrayLen);
            byteArray[arrayPosition][arrayNumber] = 1;
        }
    }

    public static long countOfUnique () {
        System.out.printf("Counting of result has been started.\n");
        var startTime = Instant.now();
        long sum = 0L;
        for (int i = 0; i < arrayLen; i++) {
            for (int j = 0; j < arraysCount; j++) {
                sum += byteArray[i][j];
            }
        }
        //long sum = set.size();
        var endTime = Instant.now();
        System.out.printf("Counting of result has been finished in %d seconds.\n",
            (endTime.getEpochSecond() - startTime.getEpochSecond())
        );
        return sum;
    }
}
