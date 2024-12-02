package com.azeauj.keycloak.solaceeventlistener;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.util.JsonSerialization;

public class SolaceConfig {
    private static final Logger logger = Logger.getLogger(SolaceConfig.class);

    private String host;
    private String vpn;
    private String queue;
    private String username;
    private String password;
    private Boolean directTransport;

    public static String writeAsJson(Object object, boolean isPretty) {
        try {
            if(isPretty) {
                return JsonSerialization.writeValueAsPrettyString(object);
            }
            return JsonSerialization.writeValueAsString(object);

        } catch (Exception e) {
            logger.error("[keycloak-to-solace] ERROR - Could not serialize to JSON", e);
        }
        return "unparseable";
    }

    public static SolaceConfig createFromScope(Config.Scope scope) {
        SolaceConfig config = new SolaceConfig();

        config.host = resolveConfigVar(scope, "HOST", "solace:55555");
        config.vpn = resolveConfigVar(scope, "VPN", "keycloak");
        config.queue = resolveConfigVar(scope, "QUEUE", "keycloak");
        config.username = resolveConfigVar(scope, "USERNAME", "manager");
        config.password = resolveConfigVar(scope, "PASSWORD", "1234");
        config.directTransport = true;

        return config;
    }

    private static String resolveConfigVar(Config.Scope scope, String variableName, String defaultValue) {
        String value = defaultValue;
        if(scope != null && scope.get(variableName) != null) {
            value = scope.get(variableName);
        } else {
            String envVariableName = "KEYCLOAK_SOLACE_" + variableName;
            String env = System.getenv(envVariableName);
            if(env != null) {
                value = env;
            }
        }
        if (!variableName.equals("PASSWORD")) {
            logger.infof("[keycloak-to-solace] Configuration: %s=%s%n", variableName, value);
        }
        return value;

    }

    public String getHost() {
        return host;
    }

    public String getVpn() {
        return vpn;
    }

    public String getQueue() {
        return queue;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getDirectTransport() {
        return directTransport;
    }
}
