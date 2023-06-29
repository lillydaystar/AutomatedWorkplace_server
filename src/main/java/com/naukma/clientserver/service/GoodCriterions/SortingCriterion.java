package com.naukma.clientserver.service.GoodCriterions;

import lombok.Getter;

@Getter
public class SortingCriterion {
    private final String fieldName;
    private final boolean ascending;

    public SortingCriterion(String fieldName, boolean ascending) {
        this.fieldName = fieldName;
        this.ascending = ascending;
    }
}