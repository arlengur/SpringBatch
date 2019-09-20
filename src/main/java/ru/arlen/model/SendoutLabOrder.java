package ru.arlen.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class SendoutLabOrder {
    private Date orderSent = new Date();
    private long orderId;
    private String orderType;
}
