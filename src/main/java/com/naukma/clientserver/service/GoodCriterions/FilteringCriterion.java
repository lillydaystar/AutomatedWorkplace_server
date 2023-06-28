package com.naukma.clientserver.service.GoodCriterions;

import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class FilteringCriterion {
    private final String fieldName;
    private final String condition;
    private final Object value;

    public FilteringCriterion() {
        fieldName = "price";
        condition = ">";
        value = 0;
    }

    public FilteringCriterion(String fieldName, String condition, Object value) {
        this.fieldName = fieldName;
        this.condition = condition;
        if (condition.equalsIgnoreCase("LIKE"))
            this.value = value + "%";
        else
            this.value = value;
    }
}
