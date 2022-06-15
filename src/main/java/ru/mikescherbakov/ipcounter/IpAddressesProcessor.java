package ru.mikescherbakov.ipcounter;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IpAddressesProcessor {
    public static Path getConfigurationFilePath() throws URISyntaxException {
        Path resourcesPath = getResourcesPath();
        return resourcesPath.resolve(Paths.get("application.properties"));
    }
    public static Path getResourcesPath() throws URISyntaxException {
        return Paths.get(IPService.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    public static Path getTestFilePath() throws URISyntaxException {
        Path resourcesPath = getResourcesPath();
        return resourcesPath.resolve(Paths.get("testFile.txt"));
    }

    public static void main(String[] args) {
        SpringApplication.run(IpAddressesProcessor.class, args);
    }
}
