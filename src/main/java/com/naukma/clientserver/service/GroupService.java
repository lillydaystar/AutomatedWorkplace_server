package com.naukma.clientserver.service;

import com.naukma.clientserver.exception.group.GroupAlreadyExistsException;
import com.naukma.clientserver.exception.group.GroupNotFoundException;
import com.naukma.clientserver.model.Group;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupService {
    private final Connection connection;

    public GroupService(Connection connection) {
        this.connection = connection;
    }

    public void createGroup(Group group) throws GroupAlreadyExistsException {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO good_group " +
                            "(name, description) VALUES (?, ?)");
            statement.setString(1, group.getName());
            statement.setString(2, group.getDescription());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            throw new GroupAlreadyExistsException("This group already exists!");
        }
    }

    public Group getGroupById(int id) throws GroupNotFoundException {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM good_group WHERE id = ?");
            statement.setInt(1, id);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int groupId = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");

                return new Group(groupId, name, description);
            }
        } catch (SQLException e) {
            System.out.println("Invalid SQL SELECT query");
            e.printStackTrace();
        }
        throw new GroupNotFoundException("Group not found!");
    }

    public Group getGroupByName(String groupName) throws GroupNotFoundException {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM good_group WHERE name = ?");
            statement.setString(1, groupName);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");

                return new Group(id, name, description);
            }
        } catch (SQLException e) {
            System.out.println("Invalid SQL SELECT query");
            e.printStackTrace();
        }
        throw new GroupNotFoundException("Incorrect group name!");
    }

    public List<Group> getAllGroups() {
        List<Group> groups = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM good_group");
            parseGroup(groups, statement, resultSet);
        } catch (SQLException e) {
            System.out.println("Invalid SQL SELECT query");
            e.printStackTrace();
        }
        return groups;
    }

    public void updateGroup(Group group) throws GroupNotFoundException {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE good_group " +
                            "SET name = ?, description = ? " +
                            "WHERE id = ?");
            statement.setString(1, group.getName());
            statement.setString(2, group.getDescription());
            statement.setInt(3, group.getId());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0)
                throw new GroupNotFoundException("Group not found!");

            statement.close();
        } catch (SQLException e) {
            System.out.println("Invalid SQL UPDATE query");
            e.printStackTrace();
        }
    }

    public void deleteGroup(int id) throws GroupNotFoundException {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM good_group " +
                            "WHERE id = ?");
            statement.setInt(1, id);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0)
                throw new GroupNotFoundException("Group not found!");

            statement.close();
        } catch (SQLException e) {
            System.out.println("Invalid SQL DELETE query");
            e.printStackTrace();
        }
    }

    public void deleteAllGroups() {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM good_group ");
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.out.println("Invalid SQL DELETE query");
            e.printStackTrace();
        }
    }

    public List<Group> getGroupsSortedByName(String order) {
        List<Group> groups = new ArrayList<>();
        try {
            String query = "SELECT * FROM good_group " +
                    "ORDER BY name ";
            if (order.equalsIgnoreCase("asc"))
                query += "ASC";
            else if (order.equalsIgnoreCase("desc"))
                query += "DESC";
            else
                throw new IllegalArgumentException("Invalid order. Must be 'asc' or 'desc'.");

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            parseGroup(groups, statement, resultSet);
        } catch (SQLException e) {
            System.out.println("Invalid SQL SELECT query");
            e.printStackTrace();
        }
        return groups;
    }

    private void parseGroup(List<Group> groups, Statement statement, ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            String description = resultSet.getString("description");

            Group group = new Group(id, name, description);
            groups.add(group);
        }

        resultSet.close();
        statement.close();
    }
}