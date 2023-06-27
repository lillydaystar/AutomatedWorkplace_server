package com.naukma.clientserver.service.GoodCriterions;

import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class SortingCriterion {
    private final String fieldName;
    private final boolean ascending;

    public SortingCriterion() {
        this.fieldName = "name";
        this.ascending = true;
    }

    public SortingCriterion(String fieldName, boolean ascending) {
        this.fieldName = fieldName;
        this.ascending = ascending;
    }
}