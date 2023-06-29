package com.naukma.clientserver.service.GoodCriterions;

import lombok.Getter;

@Getter
public class FilteringCriterion {
    private final String fieldName;
    private final String condition;
    private final Object value;

    public FilteringCriterion(String fieldName, String condition, Object value) {
        this.fieldName = fieldName;
        this.condition = condition;
        if (condition.equalsIgnoreCase("LIKE"))
            this.value = value + "%";
        else
            this.value = value;
    }
}
