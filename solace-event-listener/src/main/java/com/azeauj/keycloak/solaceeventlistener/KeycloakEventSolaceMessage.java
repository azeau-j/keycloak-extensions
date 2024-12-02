package com.azeauj.keycloak.solaceeventlistener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.keycloak.events.Event;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class KeycloakEventSolaceMessage extends Event implements Serializable {
    private static final long serialVersionUID = 5729831749503089047L;

    public static KeycloakEventSolaceMessage create(Event event) {
        KeycloakEventSolaceMessage message = new KeycloakEventSolaceMessage();
        message.setClientId(event.getClientId());
        message.setDetails(event.getDetails());
        message.setError(event.getError());
        message.setIpAddress(event.getIpAddress());
        message.setRealmId(event.getRealmId());
        message.setSessionId(event.getSessionId());
        message.setTime(event.getTime());
        message.setType(event.getType());
        message.setUserId(event.getUserId());

        return message;
    }

}
