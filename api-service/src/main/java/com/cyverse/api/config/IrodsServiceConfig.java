package com.cyverse.api.config;

import lombok.Data;

@Data
public class IrodsServiceConfig {
    private String password;
    private String zone;
    private Boolean ipcServices;
}
