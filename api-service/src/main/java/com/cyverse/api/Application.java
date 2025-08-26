package com.cyverse.api;

import com.cyverse.api.config.ApiServiceConfig;
import com.cyverse.api.config.IrodsServiceConfig;
import com.cyverse.api.config.LdapServiceConfig;
import com.cyverse.api.controllers.AuthController;
import com.cyverse.api.controllers.HealthController;
import com.cyverse.api.controllers.IrodsController;
import com.cyverse.api.controllers.LdapController;
import com.cyverse.api.exceptions.ExceptionHandler;
import com.cyverse.api.exceptions.UnauthorizedAccessException;
import com.cyverse.api.services.AuthService;
import com.cyverse.api.services.IrodsService;
import com.cyverse.api.services.LdapService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.javalin.Javalin;
import io.javalin.http.Handler;
import io.javalin.openapi.ApiKeyAuth;
import io.javalin.openapi.BearerAuth;
import io.javalin.openapi.OpenApiSecurity;
import io.javalin.openapi.SecurityScheme;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.SecurityComponentConfiguration;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.swagger.models.auth.In;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static io.javalin.http.Header.AUTHORIZATION;

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

        Javalin app = Javalin.create(
                config -> {
                    // swagger-openapi configuration
                    config.registerPlugin(getNewOpenApiPlugin());
                    config.registerPlugin(new SwaggerPlugin(swaggerConfig ->
                            swaggerConfig.setDocumentationPath("/openapi.json")));
                    config.registerPlugin(new ReDocPlugin(redocConfig ->
                            redocConfig.setDocumentationPath("/openapi.json")));
                    }
                )
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

    private static void initControllers(Javalin app, ApiServiceConfig appConfig) {
        logger.info("Initializing controllers");
        HealthController healthController = new HealthController();
        app.get("/", healthController::getHealthy);

        // services
        IrodsService irodsService = new IrodsService(appConfig.getIrodsServiceConfig());
        LdapService ldapService = new LdapService(appConfig.getLdapServiceConfig());
        ldapService.init();

        // authentication only if configured
        if (appConfig.getAuthConfig() != null) {
            AuthService authService = new AuthService(
                    appConfig.getAuthConfig().getTokenIssuer(),
                    appConfig.getAuthConfig().getUsers());
            authService.init();

            AuthController authController = new AuthController(
                    authService, appConfig.getAuthConfig().getApiKey());
            app.post("/api/login", authController::login);

            Handler authMiddleware = ctx -> {
                String header = ctx.header(AUTHORIZATION);
                if (header == null) {
                    throw new UnauthorizedAccessException("Missing Authorization token");
                }
                authService.verifyToken(header
                        .replace("Bearer", "")
                        .strip());
            };

            // auth middleware
            app.before("/api/users/*", authMiddleware);
            app.before("/api/groups/*", authMiddleware);
        }

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

    private static OpenApiPlugin getNewOpenApiPlugin() {
        SecurityComponentConfiguration securityComponentApiKey = new SecurityComponentConfiguration()
                .withSecurityScheme("ApiKeyAuth", new ApiKeyAuth());
        SecurityComponentConfiguration securityComponentBearer = new SecurityComponentConfiguration()
                .withSecurityScheme("Bearer", new BearerAuth());

        String description = "API for creating LDAP and iRODS user accounts, including account registration and management";

        return new OpenApiPlugin(openApiConfig ->
                openApiConfig
                        .withDocumentationPath("/openapi.json")
                        .withDefinitionConfiguration((version,
                                 openApiDefinition) ->
                                        openApiDefinition
                                                .withInfo(openApiInfo ->
                                                        openApiInfo
                                                                .description(description))
                                                .withSecurity(securityComponentApiKey)
                                                .withSecurity(securityComponentBearer)));
    }
}
