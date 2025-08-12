package com.cyverse.api;

import com.cyverse.api.config.ApiServiceConfig;
import com.cyverse.api.config.IrodsServiceConfig;
import com.cyverse.api.config.LdapServiceConfig;
import com.cyverse.api.controllers.HealthController;
import com.cyverse.api.controllers.IrodsController;
import com.cyverse.api.controllers.LdapController;
import com.cyverse.api.exceptions.ExceptionHandler;
import com.cyverse.api.services.IrodsService;
import com.cyverse.api.services.LdapService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        ApiServiceConfig appConfig = null;
        try {
            appConfig = loadConfig(args[0]);
            appConfig.verifyFieldsAreSet();
        } catch (Exception e) {
            logger.error("Config could not be loaded. {}", e.getMessage());
            System.exit(1);
        }
        
        Javalin app = Javalin.create()
                .start(appConfig.getPort());

        try {
            initControllers(app, appConfig);
        } catch (Exception e) {
            logger.error("Error at initialization: {}", e.getMessage());
            System.exit(1);
        }

        ExceptionHandler.handle(app);
        logger.info("API started.");
    }

    private static void initControllers(Javalin app, ApiServiceConfig appConfig) throws Exception {
        logger.info("Initializing controllers");
        HealthController healthController = new HealthController();
        app.get("/", healthController::getHealthy);

        // services
        IrodsService irodsService = new IrodsService(appConfig.getIrodsServiceConfig());
        LdapService ldapService = new LdapService(appConfig.getLdapServiceConfig());
        ldapService.init();

        // controllers
        IrodsController irodsController = new IrodsController(irodsService);
        app.post("/api/users/irods", irodsController::addIrodsUser);
        app.put("/api/users/irods", irodsController::grantUserAccess);

        LdapController ldapController = new LdapController(ldapService);
        app.post("/api/users/ldap", ldapController::addLdapUser);
        app.put("/api/users/ldap", ldapController::updateLdapUser);
        app.put("/api/groups/ldap", ldapController::addLdapUserToGroup);
    }

    private static ApiServiceConfig loadConfig(String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(filePath), ApiServiceConfig.class);
    }
}