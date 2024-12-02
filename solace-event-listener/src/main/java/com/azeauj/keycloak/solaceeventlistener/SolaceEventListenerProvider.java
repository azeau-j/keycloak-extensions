package com.azeauj.keycloak.solaceeventlistener;

import com.solacesystems.jcsmp.*;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerTransaction;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

public class SolaceEventListenerProvider implements EventListenerProvider {

    private static final Logger logger = Logger.getLogger(SolaceEventListenerProvider.class);

    private final SolaceConfig solaceConfig;

    private final XMLMessageProducer messageProducer;

    private final EventListenerTransaction transaction = new EventListenerTransaction(this::publishAdminEvent, this::publishEvent);

    public SolaceEventListenerProvider(XMLMessageProducer messageProducer, KeycloakSession keycloakSession, SolaceConfig solaceConfig) {
        this.messageProducer = messageProducer;
        this.solaceConfig = solaceConfig;
        keycloakSession.getTransactionManager().enlistAfterCompletion(transaction);
    }

    @Override
    public void onEvent(Event event) {
        logger.infof("[keycloak-to-solace] ## NEW %s EVENT", event.getType());
        transaction.addEvent(event);
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        logger.info("[keycloak-to-solace] ## NEW ADMIN EVENT");
        transaction.addAdminEvent(adminEvent, includeRepresentation);
    }

    @Override
    public void close() {
        // Nothing to close
    }

    private void publishEvent(Event event) {
        KeycloakEventSolaceMessage message = KeycloakEventSolaceMessage.create(event);
        String messageString = SolaceConfig.writeAsJson(message, true);

        this.publishNotification(messageString);
    }

    private void publishAdminEvent(AdminEvent adminEvent, Boolean includeRepresentation) {
        KeycloakAdminEventSolaceMessage message = KeycloakAdminEventSolaceMessage.create(adminEvent);
        String messageString = SolaceConfig.writeAsJson(message, true);

        this.publishNotification(messageString);
    }

    private void publishNotification(String messageString) {
        try {
            Queue queue = JCSMPFactory.onlyInstance().createQueue(solaceConfig.getQueue());
            TextMessage message = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
            message.setDeliveryMode(DeliveryMode.PERSISTENT);
            message.setText(messageString);
            message.setCorrelationKey(messageString);

            messageProducer.send(message, queue);

            logger.tracef("[keycloak-to-solace] SUCCESS - sending message: %s%n", solaceConfig.getQueue());
        } catch (Exception exception) {
            logger.errorf(exception, "[keycloak-to-solace] ERROR - sending message: %s%n", solaceConfig.getQueue());
        }
    }
}
