package com.naukma.clientserver.request;

import com.naukma.clientserver.service.GoodCriterions.FilteringCriterion;
import com.naukma.clientserver.service.GoodCriterions.SortingCriterion;
import lombok.Data;

import java.util.List;

@Data
public class GoodCriteriaRequestData {
    private List<FilteringCriterion> filteringCriteria;
    private List<SortingCriterion> sortingCriteria;
}
