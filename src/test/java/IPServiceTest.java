import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import ru.mikescherbakov.ipcounter.IPService;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import ru.mikescherbakov.ipcounter.IpAddressesProcessor;

class IPServiceTest {

    @InjectMocks
    private IPService ipService = new IPService();

    @Test
    @SneakyThrows
    void parseFile() {
        var testFilePath = IpAddressesProcessor.getTestFilePath();
        var linesCount = 1000L;
        ipService.generateTestFile(testFilePath.toString(), linesCount);
        assertTrue(ipService.parseFile(testFilePath.toString()) > linesCount / 2);
    }

    @Test
    @SneakyThrows
    void generateTestFile() {
        var tempFile = Files.createTempFile("test", "test");

        var linesCount = 1000L;
        ipService.generateTestFile(tempFile.toString(), linesCount);
        assertEquals(Files.readAllLines(tempFile).size(), linesCount);
        Files.delete(tempFile);
    }

    @Test
    @SneakyThrows
    void getTestFilePath() {
        var testFilePath = IpAddressesProcessor.getTestFilePath();
        ipService.generateTestFile(testFilePath.toString(), 1000L);
        assertTrue(testFilePath.toFile().exists());
    }
}
