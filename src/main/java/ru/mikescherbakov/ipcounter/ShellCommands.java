package ru.mikescherbakov.ipcounter;

import java.io.IOException;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class ShellCommands {
    private final IPService ipService;
    private final ApplicationProperties applicationProperties;

    public ShellCommands (IPService ipService, ApplicationProperties applicationProperties) {
        this.ipService = ipService;
        this.applicationProperties = applicationProperties;
    }

    @ShellMethod(value = "Generate test file")
    public void generate(@ShellOption(value = "-d", defaultValue = "testFile.txt") String pathToFile,
                         @ShellOption(value = "-c", defaultValue = "1000000") Long count) throws IOException
    {
        if (pathToFile.equals("testFile.txt")) {
            pathToFile = applicationProperties.getTestFilePath().toString();
        }
        ipService.generateTestFile(pathToFile, count);
    }

    @ShellMethod(value = "Parse IPs from file")
    public void parse(@ShellOption(value = "-s", defaultValue = "testFile.txt") String pathToFile)
    {
        if (pathToFile.equals("testFile.txt")) {
            pathToFile = applicationProperties.getTestFilePath().toString();
        }
        System.out.format
                (applicationProperties.getAppProps().getProperty("log.result"),
                        ipService.parseFile(pathToFile));
    }
}
