import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import lombok.SneakyThrows;

class Solution {
    @SneakyThrows
    public static void main (String[] args) {
        Path testFile = Path.of(Objects.requireNonNull(Solution.class.getResource("testFile.txt")).getFile());
        var pool = new ForkJoinPool();
        var result = pool.invoke(new IpReader(testFile, 0, (int) Files.size(testFile) - 1));
        System.out.println(result.parallelStream().unordered().distinct().count());
    }
}