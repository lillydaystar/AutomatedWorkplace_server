package com.naukma.clientserver.database.goods;

import com.naukma.clientserver.exception.good.GoodAlreadyExistsException;
import com.naukma.clientserver.exception.good.GoodNotFoundException;
import com.naukma.clientserver.exception.good.GoodPriceConstraintFailedException;
import com.naukma.clientserver.exception.good.GoodQuantityConstraintFailedException;
import com.naukma.clientserver.exception.group.GroupAlreadyExistsException;
import com.naukma.clientserver.exception.group.GroupNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.naukma.clientserver.model.Good;
import com.naukma.clientserver.model.Group;
import com.naukma.clientserver.service.DatabaseInitializationService;
import com.naukma.clientserver.service.GoodService;
import com.naukma.clientserver.service.GroupService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GoodCrudTest {
    private GroupService groupService;
    private GoodService goodService;
    private Connection connection;

    @BeforeEach
    public void setUp() {
        String dbName = "test_db";
        DatabaseInitializationService databaseInitializationService = new DatabaseInitializationService();
        connection = databaseInitializationService.createConnection(dbName);
        groupService = new GroupService(connection);
        goodService = new GoodService(connection);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        groupService.deleteAllGroups();
        connection.close();
    }

    @Test
    void testCreateGood_Basic() throws GoodAlreadyExistsException, GoodNotFoundException, GroupNotFoundException,
            GroupAlreadyExistsException, GoodPriceConstraintFailedException
    {
        groupService.createGroup(new Group("Fruits", "Group for fruits"));
        int groupId = groupService.getGroupByName("Fruits").getId();

         goodService.createGood(new Good("Apple", "Fresh Apple", "FruitVendor", 0.5, groupId));

        // Now retrieve the good and verify its data
        Good good = goodService.getGoodByName("Apple");
        assertEquals("Apple", good.getName());
        assertEquals("Fresh Apple", good.getDescription());
        assertEquals("FruitVendor", good.getProducer());
        assertEquals(0.5, good.getPrice());
        assertEquals(groupId, good.getGroupId());
    }

    @Test
    void testCreateGood_DuplicateGood() throws GroupAlreadyExistsException, GroupNotFoundException,
            GoodAlreadyExistsException, GoodPriceConstraintFailedException
    {
        groupService.createGroup(new Group("Fruits", "Group for fruits"));
        int groupId = groupService.getGroupByName("Fruits").getId();

        goodService.createGood(new Good("Apple", "Fresh Apple", "FruitVendor", 10, groupId));

        assertThrows(GoodAlreadyExistsException.class, () ->
                goodService.createGood(new Good("Apple", "Not fresh Apple", "FruitVendor2", 20, groupId))
        );
    }

    @Test
    void testCreateGood_InvalidPrice() throws GroupAlreadyExistsException, GroupNotFoundException {
        groupService.createGroup(new Group("Fruits", "Group for fruits"));
        int groupId = groupService.getGroupByName("Fruits").getId();

        int negativePrice = -1;

        assertThrows(GoodPriceConstraintFailedException.class, () ->
                goodService.createGood(new Good("Apple", "Not fresh Apple", "FruitVendor2", negativePrice, groupId))
        );
    }

    @Test
    public void testGetGoodById_ExistingGood() throws GroupAlreadyExistsException, GroupNotFoundException,
            GoodAlreadyExistsException, GoodPriceConstraintFailedException, GoodNotFoundException
    {
        groupService.createGroup(new Group("Fruits", "Group for fruits"));
        int groupId = groupService.getGroupByName("Fruits").getId();
        Good good = new Good("Apple", "Fresh Apple", "FruitVendor", 10, groupId);
        goodService.createGood(good);

        Good apple = goodService.getGoodByName("Apple");

        Good retrievedApple = goodService.getGoodById(apple.getId());

        assertEquals("Apple", retrievedApple.getName());
        assertEquals("Fresh Apple", retrievedApple.getDescription());
        assertEquals("FruitVendor", retrievedApple.getProducer());
        assertEquals(10, retrievedApple.getPrice());
        assertEquals(groupId, retrievedApple.getGroupId());
    }

    @Test
    public void testGetGoodById_NonExistentGood() {
        int nonExistentId = 9999;
        assertThrows(GoodNotFoundException.class, () -> goodService.getGoodById(nonExistentId));
    }


    @Test
    public void testGetAllGoods_EmptyDatabase() {
        List<Good> goods = goodService.getAllGoods();
        assertEquals(0, goods.size(), "List should be empty");
    }

    @Test
    public void testGetAllGoods_WithData() throws GoodAlreadyExistsException, GroupNotFoundException,
            GroupAlreadyExistsException, GoodPriceConstraintFailedException
    {
        groupService.createGroup(new Group("Fruits", "Group for fruits"));
        int groupId = groupService.getGroupByName("Fruits").getId();

        goodService.createGood(new Good("Apple", "Fresh Apple", "FruitVendor", 10, groupId));
        goodService.createGood(new Good("Banana", "Fresh Banana", "FruitVendor", 5, groupId));

        List<Good> goods = goodService.getAllGoods();
        assertEquals(2, goods.size(), "List should have two goods");

        Good apple = goods.get(0);
        assertEquals("Apple", apple.getName());
        assertEquals("Fresh Apple", apple.getDescription());

        Good banana = goods.get(1);
        assertEquals("Banana", banana.getName());
        assertEquals("Fresh Banana", banana.getDescription());
    }

    @Test
    public void testUpdateGood_ExistingGood() throws Exception {
        groupService.createGroup(new Group("Fruits", "Group for fruits"));
        int groupId = groupService.getGroupByName("Fruits").getId();
        Good good = new Good("Apple", "Fresh Apple", "FruitVendor", 10, groupId);
        goodService.createGood(good);
        Good apple = goodService.getGoodByName("Apple");

        Good newGood = new Good(apple.getId(), "Green Apple", "Very Fresh Apple",
                "GreenVendor", 20, 15.0, groupId);
        goodService.updateGood(newGood);

        Good updatedApple = goodService.getGoodByName("Green Apple");
        Good expectedApple = new Good(apple.getId(), "Green Apple", "Very Fresh Apple",
                "GreenVendor", 20, 15.0, groupId);

        assertEquals(expectedApple, updatedApple);
    }

    @Test
    public void testUpdateGood_NonExistingGood() {
        assertThrows(GoodNotFoundException.class, () ->
                goodService.updateGood(new Good(9999, "NonExisting", "Does not exist",
                        "NoVendor", 1, 1.0, 1)));
    }

    @Test
    public void testUpdateGood_NegativeQuantity() throws Exception {
        groupService.createGroup(new Group("Fruits", "Group for fruits"));
        int groupId = groupService.getGroupByName("Fruits").getId();
        goodService.createGood(new Good("Apple", "Fresh Apple", "FruitVendor", 10, groupId));
        Good apple = goodService.getGoodByName("Apple");

        assertThrows(GoodQuantityConstraintFailedException.class,
                () -> goodService.updateGood(new Good(apple.getId(), "Apple", "Fresh Apple",
                        "FruitVendor", -1, 10.0, groupId)));
    }

    @Test
    public void testUpdateGood_NegativePrice() throws Exception {
        groupService.createGroup(new Group("Fruits", "Group for fruits"));
        int groupId = groupService.getGroupByName("Fruits").getId();
        goodService.createGood(new Good("Apple", "Fresh Apple", "FruitVendor", 10, groupId));
        Good apple = goodService.getGoodByName("Apple");

        assertThrows(GoodPriceConstraintFailedException.class, () ->
                goodService.updateGood(new Good(apple.getId(), "Apple", "Fresh Apple",
                        "FruitVendor", 10, -1.0, groupId)));
    }

    @Test
    public void testDeleteGood_ExistingGood() throws Exception {
        groupService.createGroup(new Group("Fruits", "Group for fruits"));
        int groupId = groupService.getGroupByName("Fruits").getId();

        goodService.createGood(new Good("Apple", "Fresh Apple", "FruitVendor", 10, groupId));
        Good apple = goodService.getGoodByName("Apple");

        goodService.deleteGood(apple.getId());

        assertThrows(GoodNotFoundException.class, () -> goodService.getGoodByName("Apple"));
    }

    @Test
    public void testDeleteGood_NonExistingGood() {
       assertThrows(GoodNotFoundException.class, () -> goodService.deleteGood(9999));
    }

}
