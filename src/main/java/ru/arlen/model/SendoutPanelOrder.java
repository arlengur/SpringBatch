package ru.arlen.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendoutPanelOrder {
    private long id;
    private String provider;
    private String state;
    private long orderId;
    private long panelId;

    @Override
    public String toString() {
        return "SendoutPanelOrder{" + "provider='" + provider + '\'' + ", state='" + state + '\'' + ", orderId=" + orderId + ", panelId=" + panelId + '}';
    }
}
