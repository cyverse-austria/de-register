package com.cyverse.irods.services;

import com.cyverse.irods.config.IrodsClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class IrodsService {
    private static final Logger logger = LoggerFactory.getLogger(IrodsService.class);
    private IrodsClientConfig config;

    public IrodsService(IrodsClientConfig config) {
        this.config = config;
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
                                + config.getIrodsPassword()
                                + " | iinit; iadmin mkuser "
                                + username
                                + " rodsuser");
        runProcess(command);
    }
}
