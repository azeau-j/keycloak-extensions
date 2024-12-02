package com.azeauj.keycloak.solaceeventlistener;

import com.solacesystems.jcsmp.*;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class SolaceEventListenerProviderFactory implements EventListenerProviderFactory {

    private static final Logger logger = Logger.getLogger(SolaceEventListenerProviderFactory.class);
    private SolaceConfig solaceConfig;
    private JCSMPProperties properties;
    private JCSMPSession session;
    private XMLMessageProducer messageProducer;

    @Override
    public SolaceEventListenerProvider create(KeycloakSession keycloakSession) {
        checkConnectionAndProducer();
        return new SolaceEventListenerProvider(messageProducer, keycloakSession, solaceConfig);
    }

    private synchronized void checkConnectionAndProducer() {
        try {
            if (session == null || session.isClosed()) {
                this.session = JCSMPFactory.onlyInstance().createSession(properties);
            }
            if (messageProducer == null || messageProducer.isClosed()) {
                this.messageProducer = session.getMessageProducer(
                        new JCSMPStreamingPublishCorrelatingEventHandler() {
                            @Override
                            public void responseReceivedEx(Object key) {
                                logger.info("[keycloak-to-solace] Producer received response for msg: " + key.toString());
                            }

                            @Override
                            public void handleErrorEx(Object key, JCSMPException cause, long timestamp) {
                                logger.infof("[keycloak-to-solace] ERROR - Producer received error for msg: %s@%s - %s%n", key.toString(), timestamp, cause);
                            }
                        }
                );
            }
        } catch (JCSMPException exception) {
            logger.error("[keycloak-to-solace] ERROR - Connection to solace failed", exception);
        }
    }

    @Override
    public void init(Config.Scope scope) {
        solaceConfig = SolaceConfig.createFromScope(scope);
        this.properties = new JCSMPProperties();

        properties.setProperty(JCSMPProperties.HOST, solaceConfig.getHost());     // host:port
        properties.setProperty(JCSMPProperties.USERNAME, solaceConfig.getUsername()); // client-username
        properties.setProperty(JCSMPProperties.PASSWORD, solaceConfig.getPassword()); // client-password
        properties.setProperty(JCSMPProperties.VPN_NAME, solaceConfig.getVpn()); // message-vpn
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        //
    }

    @Override
    public void close() {
        messageProducer.close();
        session.closeSession();
    }

    @Override
    public String getId() {
        return "keycloak-to-solace";
    }

}
