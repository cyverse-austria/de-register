package com.cyverse.irods.config;

import lombok.Data;

@Data
public class IrodsClientConfig {
    private String irodsPassword;
    private Boolean ipcServices;
}
