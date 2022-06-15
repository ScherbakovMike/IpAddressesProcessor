import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import lombok.SneakyThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import ru.mikescherbakov.ipcounter.ApplicationProperties;
import ru.mikescherbakov.ipcounter.IPService;

class IPServiceTest {

    @InjectMocks
    private ApplicationProperties applicationProperties = new ApplicationProperties();

    @InjectMocks
    private IPService ipService = new IPService(applicationProperties);

    @Test
    @SneakyThrows
    void parseFile () {
        var testFilePath = applicationProperties.getTestFilePath();
        var linesCount = 1000L;
        ipService.generateTestFile(testFilePath.toString(), linesCount);
        assertTrue(ipService.parseFile(testFilePath.toString()) > linesCount / 2);
    }

    @Test
    @SneakyThrows
    void generateTestFile_1000lines () {
        var tempFile = Files.createTempFile("test", "test");

        var linesCount = 1000L;
        ipService.generateTestFile(tempFile.toString(), linesCount);
        assertEquals(Files.readAllLines(tempFile).size(), linesCount);
        Files.delete(tempFile);
    }

    @Test
    @SneakyThrows
    void generateTestFile_0lines () {
        var tempFile = Files.createTempFile("test", "test");
        var linesCount = 0L;
        ipService.generateTestFile(tempFile.toString(), linesCount);
        assertEquals(Files.readAllLines(tempFile).size(), linesCount);
        Files.delete(tempFile);
    }

    @Test
    @SneakyThrows
    void generateTestFile_wrongPath () {
        var tempFile = Path.of("test");
        assertThrows(RuntimeException.class, () -> ipService.parseFile(tempFile.toString()));
    }

    @Test
    @SneakyThrows
    void getTestFilePath () {
        var testFilePath = applicationProperties.getTestFilePath();
        ipService.generateTestFile(testFilePath.toString(), 1000L);
        assertTrue(testFilePath.toFile().exists());
    }
}
