package com.naukma.clientserver.request;

import lombok.Data;

@Data
public class GoodUpdateRequestData {
    private String name;
    private String description;
    private String producer;
    private int quantity;
    private double price;
    private int groupId;
}