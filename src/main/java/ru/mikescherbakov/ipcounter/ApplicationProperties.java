package ru.mikescherbakov.ipcounter;

import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Properties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationProperties {

    private final String TEST_FILE_NAME = "testFile.txt";

    @Getter
    private final Properties appProps = new Properties();

    @Getter
    private Path testFilePath;

    public ApplicationProperties () {
        try {
            appProps.load(new FileInputStream(String.valueOf(getConfigurationFilePath())));
            testFilePath = resolveTestFilePath();
        } catch (Exception e) {
            log.error(IPService.class.getSimpleName(), e);
            System.exit(1);
        }
    }

    private Path getConfigurationFilePath () throws URISyntaxException {
        Path resourcesPath = getResourcesPath();
        return resourcesPath.resolve(Path.of("application.properties"));
    }

    private Path getResourcesPath () throws URISyntaxException {
        return Path.of(IPService.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    private Path resolveTestFilePath () throws URISyntaxException {
        Path resourcesPath = getResourcesPath();
        return resourcesPath.resolve(Path.of(TEST_FILE_NAME));
    }

}
