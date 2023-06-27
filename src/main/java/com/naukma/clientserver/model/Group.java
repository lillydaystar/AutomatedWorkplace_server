package com.naukma.clientserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class Group {
    private int id;
    private String name;
    private String description;

    public Group(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
