package ru.mikescherbakov.ipcounter;

import lombok.SneakyThrows;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class ShellCommands {

    @ShellMethod(value = "Generate test file")
    @SneakyThrows
    public void generate(@ShellOption(value = "-d", defaultValue = "testFile.txt") String pathToFile,
                         @ShellOption(value = "-c", defaultValue = "1000000") Long count) {
        if (pathToFile.equals("testFile.txt")) {
            pathToFile = IPService.getTestFilePath().toString();
        }
        IPService.generateTestFile(pathToFile, count);
    }

    @ShellMethod(value = "Parse IPs from file")
    @SneakyThrows
    public void parse(@ShellOption(value = "-s", defaultValue = "testFile.txt") String pathToFile) {
        if (pathToFile.equals("testFile.txt")) {
            pathToFile = IPService.getTestFilePath().toString();
        }
        IPService.parseFile(pathToFile);
    }
}