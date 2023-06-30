package com.naukma.clientserver.database.goods;

import com.naukma.clientserver.exception.good.GoodAlreadyExistsException;
import com.naukma.clientserver.exception.good.GoodPriceConstraintFailedException;
import com.naukma.clientserver.exception.group.GroupAlreadyExistsException;
import com.naukma.clientserver.exception.group.GroupNotFoundException;
import com.naukma.clientserver.service.GoodCriterions.FilteringCriterion;
import com.naukma.clientserver.service.GoodCriterions.SortingCriterion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.naukma.clientserver.model.*;
import com.naukma.clientserver.service.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GoodListByCriteriaTest {
    private GroupService groupService;
    private GoodService goodService;
    private Connection connection;

    @BeforeEach
    public void setUp() throws GroupAlreadyExistsException, GroupNotFoundException, GoodAlreadyExistsException, GoodPriceConstraintFailedException {
        String dbName = "test_db";
        DatabaseInitializationService databaseInitializationService = new DatabaseInitializationService();
        connection = databaseInitializationService.createConnection(dbName);
        groupService = new GroupService(connection);
        goodService = new GoodService(connection);

        // Fruits group
        groupService.createGroup(new Group("Fruits", "Group for fruits"));
        int fruitsGroupId = groupService.getGroupByName("Fruits").getId();

        goodService.createGood(new Good("Apple", "Fresh Apple", "VendorA", 12.5, fruitsGroupId));
        goodService.createGood(new Good("Banana", "Yellow Banana", "VendorB", 8.0, fruitsGroupId));
        goodService.createGood(new Good("Orange", "Sour Orange", "VendorC", 15.0, fruitsGroupId));
        goodService.createGood(new Good("Grapes", "Sweet Grapes", "VendorD", 6.0, fruitsGroupId));
        goodService.createGood(new Good("Pineapple", "Tropical Fruit", "VendorE", 20.0, fruitsGroupId));

        // Vegetables group
        groupService.createGroup(new Group("Vegetables", "Group for vegetables"));
        int vegetablesGroupId = groupService.getGroupByName("Vegetables").getId();

        goodService.createGood(new Good("Tomato", "Red Tomato", "VendorA", 3.0, vegetablesGroupId));
        goodService.createGood(new Good("Cucumber", "Green Cucumber", "VendorB", 4.0, vegetablesGroupId));
        goodService.createGood(new Good("Carrot", "Sweet Carrot", "VendorC", 2.5, vegetablesGroupId));
        goodService.createGood(new Good("Potato", "Starchy Potato", "VendorD", 1.5, vegetablesGroupId));
        goodService.createGood(new Good("Broccoli", "Healthy Broccoli", "VendorE", 5.0, vegetablesGroupId));

        // Beverages group
        groupService.createGroup(new Group("Beverages", "Group for beverages"));
        int beveragesGroupId = groupService.getGroupByName("Beverages").getId();

        goodService.createGood(new Good("Coca-Cola", "Soft Drink", "VendorF", 1.0, beveragesGroupId));
        goodService.createGood(new Good("Orange Juice", "Fruit Juice", "VendorG", 2.0, beveragesGroupId));
        goodService.createGood(new Good("Green Tea", "Healthy Beverage", "VendorH", 3.5, beveragesGroupId));
        goodService.createGood(new Good("Coffee", "Hot Drink", "VendorI", 4.0, beveragesGroupId));
        goodService.createGood(new Good("Water", "Mineral Water", "VendorJ", 0.5, beveragesGroupId));
    }

    @AfterEach
    public void tearDown() throws SQLException {
        groupService.deleteAllGroups();
        connection.close();
    }

    @Test
    void testFilteringSingleCriterion() {
        List<FilteringCriterion> filteringCriteria = new ArrayList<>();
        filteringCriteria.add(new FilteringCriterion("price", ">", 10));

        List<SortingCriterion> sortingCriteria = new ArrayList<>();

        List<Good> goods = goodService.listGoodsByCriteria(filteringCriteria, sortingCriteria);

        for (Good good : goods) {
            assertTrue(good.getPrice() > 10);
        }
    }

    @Test
    void testSortingSingleCriterion() {
        List<FilteringCriterion> filteringCriteria = new ArrayList<>();
        List<SortingCriterion> sortingCriteria = new ArrayList<>();

        // Sorting by price in ascending order
        sortingCriteria.add(new SortingCriterion("price", true));

        List<Good> goods = goodService.listGoodsByCriteria(filteringCriteria, sortingCriteria);
        double previousPrice = 0;
        for (Good good : goods) {
            assertTrue(good.getPrice() >= previousPrice);
            previousPrice = good.getPrice();
        }
    }

    @Test
    void testFilteringAndSortingMultipleCriteria() {
        List<FilteringCriterion> filteringCriteria = new ArrayList<>();
        List<SortingCriterion> sortingCriteria = new ArrayList<>();

        // Filtering by price greater than 2
        filteringCriteria.add(new FilteringCriterion("price", ">", 2));

        // Sorting by producer in ascending order
        sortingCriteria.add(new SortingCriterion("producer", true));

        List<Good> goods = goodService.listGoodsByCriteria(filteringCriteria, sortingCriteria);

        double previousPrice = 2;
        String previousProducer = "";
        for (Good good : goods) {
            assertTrue(good.getPrice() > previousPrice || good.getProducer().compareTo(previousProducer) >= 0);
            previousProducer = good.getProducer();
        }
    }

    @Test
    void testFilteringMultipleCriteria() throws GroupNotFoundException {
        List<FilteringCriterion> filteringCriteria = new ArrayList<>();
        List<SortingCriterion> sortingCriteria = new ArrayList<>();

        // Filtering by goods in Vegetables group and price less than 4
        int groupId = groupService.getGroupByName("Vegetables").getId();
        filteringCriteria.add(new FilteringCriterion("groupId", "=", groupId));
        filteringCriteria.add(new FilteringCriterion("price", "<", 4));

        List<Good> goods = goodService.listGoodsByCriteria(filteringCriteria, sortingCriteria);

        for (Good good : goods) {
            assertTrue(good.getPrice() < 4);
            assertEquals(groupService.getGroupByName("Vegetables").getId(), good.getGroupId());
        }
    }

    @Test
    void testSortingMultipleCriteria() {
        List<FilteringCriterion> filteringCriteria = new ArrayList<>();
        List<SortingCriterion> sortingCriteria = new ArrayList<>();

        // Sorting by group ID in ascending order and then by price in descending order
        sortingCriteria.add(new SortingCriterion("groupId", true));
        sortingCriteria.add(new SortingCriterion("price", false));

        List<Good> goods = goodService.listGoodsByCriteria(filteringCriteria, sortingCriteria);

        int previousGroupId = -1;
        double previousPrice = Double.MAX_VALUE;
        for (Good good : goods) {
            if (good.getGroupId() == previousGroupId) {
                assertTrue(good.getPrice() <= previousPrice);
            }
            previousGroupId = good.getGroupId();
            previousPrice = good.getPrice();
        }
    }

    @Test
    void testFilteringUsingLikeAndSortingByNameAsc() {
        List<FilteringCriterion> filteringCriteria = new ArrayList<>();
        List<SortingCriterion> sortingCriteria = new ArrayList<>();

        // Filtering names that start with 'A'
        filteringCriteria.add(new FilteringCriterion("name", "LIKE", "C"));

        // Sorting by name in ascending order
        sortingCriteria.add(new SortingCriterion("name", true));

        List<Good> goods = goodService.listGoodsByCriteria(filteringCriteria, sortingCriteria);

        String previousName = "";
        for (Good good : goods) {
            assertTrue(good.getName().startsWith("C"));
            assertTrue(good.getName().compareTo(previousName) >= 0);
            previousName = good.getName();
        }
    }

    @Test
    void testFilteringUsingLikeAndSortingByNameDesc() {
        List<FilteringCriterion> filteringCriteria = new ArrayList<>();
        List<SortingCriterion> sortingCriteria = new ArrayList<>();

        // Filtering names that end with 'e'
        filteringCriteria.add(new FilteringCriterion("producer", "LIKE", "Vend"));

        // Sorting by name in descending order
        sortingCriteria.add(new SortingCriterion("producer", false));

        List<Good> goods = goodService.listGoodsByCriteria(filteringCriteria, sortingCriteria);

        String previousProducer = "zzzzzzzz"; // Placeholder for high value
        for (Good good : goods) {
            assertTrue(good.getProducer().startsWith("Vend"));
            assertTrue(good.getProducer().compareTo(previousProducer) <= 0);
            previousProducer = good.getProducer();
        }
    }
}
