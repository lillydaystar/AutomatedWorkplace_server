package com.naukma.clientserver.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoodRequestData {
    private String name;
    private String description;
    private String producer;
    private int quantity;
    private double price;
    private int groupId;
}