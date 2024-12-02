package com.azeauj.keycloak.solaceeventlistener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.keycloak.events.admin.AdminEvent;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class KeycloakAdminEventSolaceMessage extends AdminEvent implements Serializable {
    private static final long serialVersionUID = -5863581400455001787L;

    public static KeycloakAdminEventSolaceMessage create(AdminEvent adminEvent) {
        KeycloakAdminEventSolaceMessage message = new KeycloakAdminEventSolaceMessage();
        message.setAuthDetails(adminEvent.getAuthDetails());
        message.setError(adminEvent.getError());
        message.setOperationType(adminEvent.getOperationType());
        message.setRealmId(adminEvent.getRealmId());
        message.setRepresentation(adminEvent.getRepresentation());
        message.setResourcePath(adminEvent.getResourcePath());
        message.setResourceType(adminEvent.getResourceType());
        message.setResourceTypeAsString(adminEvent.getResourceTypeAsString());
        message.setTime(adminEvent.getTime());

        return message;
    }

}
