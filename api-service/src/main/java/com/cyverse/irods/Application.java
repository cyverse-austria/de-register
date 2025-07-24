package com.cyverse.irods;

import com.cyverse.irods.config.IrodsClientConfig;
import com.cyverse.irods.controllers.HealthController;
import com.cyverse.irods.controllers.IrodsController;
import com.cyverse.irods.exceptions.ExceptionHandler;
import com.cyverse.irods.services.IrodsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        Javalin app = Javalin.create()
                .start(7000);
        try {
            initControllers(app, args[0]);
        } catch (Exception e) {
            logger.error("Error at initialization {}", e.getMessage());
            System.exit(1);
        }

        ExceptionHandler.handle(app);
        logger.info("API started.");
    }

    private static void initControllers(Javalin app, String configFile) throws Exception {
        logger.info("Initializing controllers");
        HealthController healthController = new HealthController();
        app.get("/", healthController::getHealthy);

        IrodsService irodsService = new IrodsService(loadConfig(configFile));
        IrodsController irodsController = new IrodsController(irodsService);
        app.post("/api/users/", irodsController::addIrodsUser);
    }

    private static IrodsClientConfig loadConfig(String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(filePath), IrodsClientConfig.class);
    }
}