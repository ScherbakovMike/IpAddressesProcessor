package ru.mikescherbakov.ipcounter;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IPServiceTest {

    @Test
    @SneakyThrows
    void parseFile() {
        var testFilePath = IPService.getTestFilePath();
        var linesCount = 1000L;
        IPService.generateTestFile(testFilePath.toString(), linesCount);
        assertTrue(IPService.parseFile(testFilePath.toString()) > linesCount / 2);
    }

    @Test
    @SneakyThrows
    void generateTestFile() {
        var tempFile = Files.createTempFile("test", "test");

        var linesCount = 1000L;
        IPService.generateTestFile(tempFile.toString(), linesCount);
        assertEquals(Files.readAllLines(tempFile).size(), linesCount);
        Files.delete(tempFile);
    }

    @Test
    @SneakyThrows
    void getTestFilePath() {
        var testFilePath = IPService.getTestFilePath();
        IPService.generateTestFile(testFilePath.toString(), 1000L);
        assertTrue(testFilePath.toFile().exists());
    }
}
