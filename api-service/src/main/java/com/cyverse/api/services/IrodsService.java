package com.cyverse.api.services;

import com.cyverse.api.config.IrodsServiceConfig;
import com.cyverse.api.exceptions.IrodsException;
import com.cyverse.api.exceptions.ResourceAlreadyExistsException;
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
    public void addIrodsUser(String username)
            throws IOException, InterruptedException,
            IrodsException, ResourceAlreadyExistsException {
        try {
            runProcess(buildGetCommand(username));
            // if GET command runs without errors, the user is already in iRODS
            throw new ResourceAlreadyExistsException("User already registered in iRODS");
        } catch (IrodsException e) {
            logger.debug("User doesn't exist, will continue creation process");
        }

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
    public void grantAccessToUser(String username) throws IOException,
            InterruptedException, IrodsException {
        if (irodsConfig.getIpcServices()) {
            runProcess(buildChModCommand("own", "ipcservices", username));
        }
        runProcess(buildChModCommand("own", "rodsadmin", username));
    }

    /**
     * Generic Java command runner based on ProcessBuilder.
     *
     * @param command the command to run
     */
    private void runProcess(List<String> command) throws
            InterruptedException, IOException, IrodsException {
        ProcessBuilder pb = new ProcessBuilder(command.toArray(String[]::new));
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        );
        String line;
        String errorLine = "";
        while ((line = reader.readLine()) != null) {
            if (line.contains("ERROR")) {
                errorLine = line;
            }
        }

        process.waitFor();

        if (!errorLine.isEmpty()) {
            throw new IrodsException(errorLine);
        }
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

    private List<String> buildGetCommand(String username) {
        return Arrays.asList(
                "bash", "-c",
                "echo "
                        + irodsConfig.getPassword()
                        + " | iinit; ils .. "
                        + "/" + irodsConfig.getZone() + "/home/" + username
        );
    }
}
