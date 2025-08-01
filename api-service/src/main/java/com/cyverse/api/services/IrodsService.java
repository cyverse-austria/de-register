package com.cyverse.api.services;

import com.cyverse.api.config.IrodsServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * iRODS Service based on commands ran via ProcessBuilder.
 */
public class IrodsService {
    private static final Logger logger = LoggerFactory.getLogger(IrodsService.class);
    private IrodsServiceConfig irodsConfig;

    public IrodsService(IrodsServiceConfig irodsConfig) {
        this.irodsConfig = irodsConfig;
    }

    /**
     * Add user in iRODS via iRODS commands.
     *
     * @param username the user to create account for in iRODS
     */
    public void addIrodsUser(String username) throws IOException, InterruptedException {
        List<String> addUsercommand =
                Arrays.asList(
                        "bash", "-c",
                        "echo "
                                + irodsConfig.getPassword()
                                + " | iinit; iadmin mkuser "
                                + username
                                + " rodsuser");
        runProcess(addUsercommand);
    }

    /**
     * Grants access for groups to user home directory.
     *
     * @param username the username to grant access to
     */
    public void grantAccessToUser(String username) throws IOException, InterruptedException {
        if (irodsConfig.getIpcServices()) {
            runProcess(buildChModCommand("own", "ipcservices", username));
        }
        runProcess(buildChModCommand("own", "rodsadmin", username));
    }

    /**
     * Generic Java command runner based on ProcessBuilder.
     * TODO Revise error handling by not merging stdout and stderr.
     *
     * @param command the command to run
     */
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

    private List<String> buildChModCommand(String permission, String group, String username) {
        return Arrays.asList(
                "bash", "-c",
                "echo "
                        + irodsConfig.getPassword()
                        + " | iinit; ichmod "
                        + permission + " " + group
                        + " /" + irodsConfig.getZone() + "/home/" + username
        );
    }
}
