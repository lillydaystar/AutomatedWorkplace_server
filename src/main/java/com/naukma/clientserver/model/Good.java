package com.naukma.clientserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class Good {
    private int id;
    private String name;
    private String description;
    private String producer;
    private int quantity;
    private double price;
    private int groupId;


    public Good(int id, String name, String description, String producer, double price, int groupId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.producer = producer;
        this.price = price;
        this.groupId = groupId;
    }

    public Good(String name, String description, String producer, int quantity, double price, int groupId) {
        this.name = name;
        this.description = description;
        this.producer = producer;
        this.quantity = quantity;
        this.price = price;
        this.groupId = groupId;
    }

    public Good(String name, String description, String producer, double price, int groupId) {
        this.name = name;
        this.description = description;
        this.producer = producer;
        this.price = price;
        this.groupId = groupId;
    }
}
