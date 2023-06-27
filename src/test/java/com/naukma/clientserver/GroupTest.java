package com.naukma.clientserver;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.naukma.clientserver.exception.group.GroupAlreadyExistsException;
import com.naukma.clientserver.exception.group.GroupNotFoundException;
import com.naukma.clientserver.model.Group;
import com.naukma.clientserver.service.DatabaseInitializationService;
import com.naukma.clientserver.service.GroupService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GroupTest {
    private GroupService groupService;
    private Connection connection;

    @BeforeEach
    public void setUp() {
        String dbName = "test_db";
        DatabaseInitializationService databaseInitializationService = new DatabaseInitializationService();
        connection = databaseInitializationService.createConnection(dbName);
        groupService = new GroupService(connection);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        groupService.deleteAllGroups();
        connection.close();
    }

    @Test
    void testCreateGroup() throws GroupNotFoundException, GroupAlreadyExistsException {
        groupService.createGroup(new Group("Test Group", "Test Description"));

        Group createdGroup = groupService.getGroupByName("Test Group");

        assertNotNull(createdGroup);
        assertEquals("Test Group", createdGroup.getName());
        assertEquals("Test Description", createdGroup.getDescription());
    }

    @Test
    void testCreateExistingGroup() throws GroupAlreadyExistsException {
        groupService.createGroup(new Group("Test Group", "Test Description"));

        assertThrows(GroupAlreadyExistsException.class, () ->
                groupService.createGroup(new Group("Test Group", "Test Description")));
    }

    @Test
    void testGetAllGroups_EmptyList() {
        List<Group> groups = groupService.getAllGroups();
        assertTrue(groups.isEmpty());
    }

    @Test
    void testGetAllGroups_SingleGroup() throws GroupAlreadyExistsException {
        groupService.createGroup(new Group("Group 1", "Description 1"));

        List<Group> groups = groupService.getAllGroups();
        assertEquals(1, groups.size());

        Group group = groups.get(0);
        assertEquals("Group 1", group.getName());
        assertEquals("Description 1", group.getDescription());
    }

    @Test
    void testGetAllGroups_MultipleGroups() throws GroupAlreadyExistsException {
        groupService.createGroup(new Group("Group 1", "Description 1"));
        groupService.createGroup(new Group("Group 2", "Description 2"));
        groupService.createGroup(new Group("Group 3", "Description 3"));

        List<Group> groups = groupService.getAllGroups();
        assertEquals(3, groups.size());

        Group group1 = groups.get(0);
        assertEquals("Group 1", group1.getName());
        assertEquals("Description 1", group1.getDescription());

        Group group2 = groups.get(1);
        assertEquals("Group 2", group2.getName());
        assertEquals("Description 2", group2.getDescription());

        Group group3 = groups.get(2);
        assertEquals("Group 3", group3.getName());
        assertEquals("Description 3", group3.getDescription());
    }

    @Test
    void testGetGroupById_ValidId() throws GroupNotFoundException, GroupAlreadyExistsException {
        groupService.createGroup(new Group("Test Group", "Test Description"));
        Group createdGroup = groupService.getGroupByName("Test Group");

        Group retrievedGroup = groupService.getGroupById(createdGroup.getId());

        assertNotNull(retrievedGroup);
        assertEquals(createdGroup.getId(), retrievedGroup.getId());
        assertEquals("Test Group", retrievedGroup.getName());
        assertEquals("Test Description", retrievedGroup.getDescription());
    }

    @Test
    void testGetGroupById_InvalidId() {
        assertThrows(GroupNotFoundException.class, () -> groupService.getGroupById(9999));
    }

    @Test
    void testGetGroupByName_ExistingGroup() throws GroupAlreadyExistsException, GroupNotFoundException {
        groupService.createGroup(new Group("Group 1", "Description 1"));
        groupService.createGroup(new Group("Group 2", "Description 2"));

        Group group = groupService.getGroupByName("Group 2");
        assertNotNull(group);
        assertEquals("Group 2", group.getName());
        assertEquals("Description 2", group.getDescription());
    }

    @Test
    void testGetGroupByName_NonexistentGroup() {
        assertThrows(GroupNotFoundException.class, () -> groupService.getGroupByName("Non-existent Group"));
    }

    @Test
    void testUpdateGroup() throws GroupNotFoundException, GroupAlreadyExistsException {
        groupService.createGroup(new Group("Test Group", "Initial Description"));

        Group createdGroup = groupService.getGroupByName("Test Group");

        groupService.updateGroup(new Group(createdGroup.getId(), "Updated Name", "Updated Description"));

        Group updatedGroup = groupService.getGroupById(createdGroup.getId());

        assertNotNull(updatedGroup);
        assertEquals("Updated Name", updatedGroup.getName());
        assertEquals("Updated Description", updatedGroup.getDescription());
    }

    @Test
    void testUpdateGroup_NotFound() {
        assertThrows(GroupNotFoundException.class, () ->
                groupService.updateGroup(new Group(9999, "Non-existent Name", "Updated Description")));
    }

    @Test
    void testUpdateGroup_MultipleGroups() throws GroupNotFoundException, GroupAlreadyExistsException {
        groupService.createGroup(new Group("Group 1", "Description 1"));
        groupService.createGroup(new Group("Group 2", "Description 2"));
        groupService.createGroup(new Group("Group 3", "Description 3"));

        Group group2 = groupService.getGroupByName("Group 2");

        groupService.updateGroup(new Group(group2.getId(), "Group 2", "Updated Description"));

        Group updatedGroup = groupService.getGroupById(group2.getId());
        assertEquals("Updated Description", updatedGroup.getDescription());

        List<Group> groups = groupService.getAllGroups();
        for (Group group : groups)
            if (group.getId() != group2.getId())
                assertEquals("Description " + group.getName().charAt(6), group.getDescription());
    }

    @Test
    void testDeleteGroup_NotFound() {
        assertThrows(GroupNotFoundException.class, () -> groupService.deleteGroup(9999));
    }

    @Test
    void testDeleteGroup_MultipleGroups() throws GroupNotFoundException, GroupAlreadyExistsException {
        groupService.createGroup(new Group("Group 1", "Description 1"));
        groupService.createGroup(new Group("Group 2", "Description 2"));
        groupService.createGroup(new Group("Group 3", "Description 3"));

        Group group2 = groupService.getGroupByName("Group 2");

        groupService.deleteGroup(group2.getId());
        assertThrows(GroupNotFoundException.class, () -> groupService.getGroupById(group2.getId()));

        // Verify the remaining groups are still here
        List<Group> groups = groupService.getAllGroups();
        assertEquals(2, groups.size());

        for (Group group : groups)
            assertNotEquals(group2.getId(), group.getId());
    }

    @Test
    void testGetGroupsSortedByName_DescendingOrder() throws GroupAlreadyExistsException {
        groupService.createGroup(new Group("Group A", "Description 1"));
        groupService.createGroup(new Group("Group C", "Description 3"));
        groupService.createGroup(new Group("Group B", "Description 2"));

        List<Group> groups = groupService.getGroupsSortedByName("desc");
        assertEquals(3, groups.size());

        Group group1 = groups.get(0);
        assertEquals("Group C", group1.getName());
        assertEquals("Description 3", group1.getDescription());

        Group group2 = groups.get(1);
        assertEquals("Group B", group2.getName());
        assertEquals("Description 2", group2.getDescription());

        Group group3 = groups.get(2);
        assertEquals("Group A", group3.getName());
        assertEquals("Description 1", group3.getDescription());
    }

    @Test
    void testGetGroupsSortedByName_InvalidOrder() {
        assertThrows(IllegalArgumentException.class, () ->
                groupService.getGroupsSortedByName("invalid"));
    }

    @Test
    void testDeleteGroup() throws GroupNotFoundException, GroupAlreadyExistsException {
        groupService.createGroup(new Group("Test Group", "Test Description"));
        Group createdGroup = groupService.getGroupByName("Test Group");

        groupService.deleteGroup(createdGroup.getId());

        assertThrows(GroupNotFoundException.class, () -> groupService.getGroupById(createdGroup.getId()));
    }

    @Test
    void testDeleteAllGroups() throws GroupAlreadyExistsException {
        groupService.createGroup(new Group("Test Group 1", "Test Description 1"));
        groupService.createGroup(new Group("Test Group 2", "Test Description 2"));
        groupService.createGroup(new Group("Test Group 3", "Test Description 3"));

        groupService.deleteAllGroups();

        List<Group> groups = groupService.getAllGroups();
        assertTrue(groups.isEmpty());
    }
}
