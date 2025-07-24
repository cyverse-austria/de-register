package com.cyverse.api.services;

import com.cyverse.api.config.IrodsServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class IrodsService {
    private static final Logger logger = LoggerFactory.getLogger(IrodsService.class);
    private IrodsServiceConfig irodsConfig;

    public IrodsService(IrodsServiceConfig irodsConfig) {
        this.irodsConfig = irodsConfig;
    }

    private void runProcess(List<String> command) throws
            InterruptedException, IOException {
        ProcessBuilder pb = new ProcessBuilder(command.toArray(String[]::new));
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        );
        String line;
        while ((line = reader.readLine()) != null) {
            logger.debug("iRODS Stdout + Stderr: {}", line);
        }

        process.waitFor();
    }

    public void addIrodsUser(String username) throws IOException, InterruptedException {
        List<String> command =
                Arrays.asList(
                        "bash", "-c",
                        "echo "
                                + irodsConfig.getPassword()
                                + " | iinit; iadmin mkuser "
                                + username
                                + " rodsuser");
        runProcess(command);
    }
}
