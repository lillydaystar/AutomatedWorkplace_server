package com.naukma.clientserver.request;

import lombok.Data;

@Data
public class GoodCreateRequestData {
    private String name;
    private String description;
    private String producer;
    private double price;
    private int groupId;
}