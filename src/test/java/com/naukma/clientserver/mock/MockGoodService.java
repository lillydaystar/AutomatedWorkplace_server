package com.naukma.clientserver.mock;

import com.naukma.clientserver.exception.good.GoodNotFoundException;
import com.naukma.clientserver.model.Good;
import com.naukma.clientserver.service.GoodCriterions.FilteringCriterion;
import com.naukma.clientserver.service.GoodService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockGoodService extends GoodService {
    private Map<Integer, Good> mockGoods;
    private int counter = 0;

    private double totalSumOfAllGoods;
    private double totalSumOfGoodsByCriteria;

    public MockGoodService() {
        super(null);
        mockGoods = new HashMap<>();
    }

    public void setTotalSumOfAllGoods(double totalSumOfAllGoods) {
        this.totalSumOfAllGoods = totalSumOfAllGoods;
    }

    public void setTotalSumOfGoodsByCriteria(double totalSumOfGoodsByCriteria) {
        this.totalSumOfGoodsByCriteria = totalSumOfGoodsByCriteria;
    }

    @Override
    public double getTotalSumOfAllGoods() {
        return totalSumOfAllGoods;
    }

    @Override
    public double getTotalSumOfGoodsByCriteria(List<FilteringCriterion> criteria) {
        return totalSumOfGoodsByCriteria;
    }

    @Override
    public List<Good> getAllGoods() {
        return new ArrayList<>(mockGoods.values());
    }

    @Override
    public Good getGoodById(int id) {
        return mockGoods.get(id);
    }

    @Override
    public Good getGoodByName(String name) {
        for (int i = 0; i < counter; i++) {
            Good good = mockGoods.get(i);
            if (name.equals(good.getName()))
                return good;
        }
        return null;
    }

    @Override
    public void createGood(Good good) {
        good.setId(counter);
        mockGoods.put(counter++, good);
    }

    @Override
    public void updateGood(Good good) {
        int key = good.getId();
        Good good1 = mockGoods.get(key);
        good1.setName(good.getName());
        good1.setDescription(good.getDescription());
        good1.setProducer(good.getProducer());
        good1.setGroupId(good.getGroupId());
        good1.setPrice(good.getPrice());
        good1.setQuantity(good.getQuantity());
    }

    @Override
    public void deleteGood(int id) {
        mockGoods.remove(id);
    }

    public void setMockGoods(Map<Integer, Good> mockGoods) {
        this.mockGoods = mockGoods;
        while (mockGoods.containsKey(counter)) {
            mockGoods.get(counter).setId(counter);
            counter++;
        }
    }

    public void setMockGood(Good mockGood) {
        mockGood.setId(counter);
        mockGoods.put(counter++, mockGood);
    }
}
