package com.cyverse.api.services;

import com.cyverse.api.config.IrodsServiceConfig;
import com.cyverse.api.exceptions.IrodsException;
import com.cyverse.api.exceptions.ResourceAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * iRODS Service based on commands ran via ProcessBuilder.
 */
public class IrodsService {
    private static final Logger logger = LoggerFactory.getLogger(IrodsService.class);
    private IrodsServiceConfig irodsConfig;
    private PasswordService passwordService;

    public IrodsService(IrodsServiceConfig irodsConfig, PasswordService passwordService) {
        this.irodsConfig = irodsConfig;
        this.passwordService = passwordService;
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
        runProcess(buildChangePasswordCommand(username,
                passwordService.getGeneratedPassword(username)));
    }

    /**
     * Grants access for groups to user home directory.
     *
     * @param username the username to grant access to
     */
    public void grantAccessToUser(String username) throws IOException,
            InterruptedException, IrodsException, ResourceAlreadyExistsException {
        String excMsg = "Group %s already has permission to user";

        String permission = "own";
        String group = "ipcservices";
        ResourceAlreadyExistsException exc = null;

        if (irodsConfig.getIpcServices()) {
            if (isOwnershipAlreadyPresent(username, group, permission)) {
                exc = new ResourceAlreadyExistsException(String.format(excMsg, group));
                logger.warn("ipcservices already has permission for user {}", username);
            }
            runProcess(buildChModCommand(permission, group, username));
        }

        group = "rodsadmin";
        if (isOwnershipAlreadyPresent(username, group, permission)) {
            throw new ResourceAlreadyExistsException(String.format(excMsg, group));
        }

        runProcess(buildChModCommand(permission, group, username));

        // TODO consider taking the group as request param to avoid this mess
        if (exc != null) {
            throw exc;
        }
    }

    /**
     * Generic Java command runner based on ProcessBuilder.
     *
     * @param command the command to run
     * @return stdout+stderr lines
     */
    protected List<String> runProcess(List<String> command) throws
            InterruptedException, IOException, IrodsException {
        ProcessBuilder pb = new ProcessBuilder(command.toArray(String[]::new));
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        );

        // init output reading context and read
        String line;
        String errorLine = "";
        List<String> outputLines = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            if (line.contains("ERROR")) {
                errorLine = line;
            }
            outputLines.add(line);
        }

        process.waitFor();

        // throw here: after the process terminates to not cause memory leaks
        if (!errorLine.isEmpty()) {
            throw new IrodsException(errorLine);
        }

        return outputLines;
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
                        + " | iinit; ils ../" + username
        );
    }

    private boolean isOwnershipAlreadyPresent(String username, String group, String permission)
            throws IrodsException, IOException, InterruptedException {
        List<String> output = runProcess(buildGetOwnershipCommand(username));

        for (String line: output) {
            if (line.contains(group) && line.contains(permission)) {
                return true;
            }
        }

        return false;
    }

    private List<String> buildGetOwnershipCommand(String username) {
        return Arrays.asList(
                "bash", "-c",
                "echo "
                        + irodsConfig.getPassword()
                        + " | iinit; ils -A ../" + username
        );
    }

    private List<String> buildChangePasswordCommand(String username, String password) {
        return Arrays.asList(
                "bash", "-c",
                "echo "
                        + irodsConfig.getPassword()
                        + " | iinit; moduser " + username
                        + " password -- " + password
        );
    }
}
