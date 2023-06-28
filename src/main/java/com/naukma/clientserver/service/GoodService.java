package com.naukma.clientserver.service;


import com.naukma.clientserver.exception.good.GoodAlreadyExistsException;
import com.naukma.clientserver.exception.good.GoodNotFoundException;
import com.naukma.clientserver.exception.good.GoodPriceConstraintFailedException;
import com.naukma.clientserver.exception.good.GoodQuantityConstraintFailedException;
import com.naukma.clientserver.exception.group.GroupNotFoundException;
import com.naukma.clientserver.model.Good;
import com.naukma.clientserver.service.GoodCriterions.FilteringCriterion;
import com.naukma.clientserver.service.GoodCriterions.SortingCriterion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoodService {
    private final Connection connection;

    public GoodService(Connection connection) {
        this.connection = connection;
    }

    public void createGood(Good good)
            throws GoodAlreadyExistsException, GroupNotFoundException, GoodPriceConstraintFailedException {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO good " +
                            "(name, description, producer, price, groupId) VALUES (?, ?, ?, ?, ?)");
            statement.setString(1, good.getName());
            statement.setString(2, good.getDescription());
            statement.setString(3, good.getProducer());
            statement.setDouble(4, good.getPrice());
            statement.setInt(5, good.getGroupId());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            if (e.getMessage().contains("FOREIGN KEY constraint failed"))
                throw new GroupNotFoundException("First create this group!");
            else if (e.getMessage().contains("UNIQUE constraint failed: good.name"))
                throw new GoodAlreadyExistsException("Good with this name already exists!");
            else if (e.getMessage().contains("CHECK constraint failed: price >= 0"))
                throw new GoodPriceConstraintFailedException("Price can't be negative!");
            else
                e.printStackTrace();
        }
    }

    public Good getGoodById(int id) throws GoodNotFoundException {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM good WHERE id = ?");
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Good> goods = parseGoods(resultSet);
                if (!goods.isEmpty())
                    return goods.get(0);
            }
        } catch (SQLException e) {
            System.out.println("Invalid SELECT query");
            e.printStackTrace();
        }
        throw new GoodNotFoundException("Good not found!");
    }

    public Good getGoodByName(String name) throws GoodNotFoundException {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM good WHERE name = ?");
            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Good> goods = parseGoods(resultSet);
                if (!goods.isEmpty())
                    return goods.get(0);
            }
        } catch (SQLException e) {
            System.out.println("Invalid SELECT query");
            e.printStackTrace();
        }
        throw new GoodNotFoundException("Good not found!");
    }

    public List<Good> getAllGoods() {
        List<Good> goods = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM good");
            goods = parseGoods(resultSet);
            statement.close();
        } catch(SQLException e) {
            System.out.println("Invalid SELECT query");
            e.printStackTrace();
        }
        return goods;
    }

    public void updateGood(Good good) throws GoodNotFoundException, GroupNotFoundException, GoodAlreadyExistsException,
            GoodPriceConstraintFailedException, GoodQuantityConstraintFailedException {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE good " +
                        "SET name = ?, description = ?, producer = ?, quantity = ?, price = ?, groupId = ? " +
                        "WHERE id = ?");
            statement.setString(1, good.getName());
            statement.setString(2, good.getDescription());
            statement.setString(3, good.getProducer());
            statement.setInt(4, good.getQuantity());
            statement.setDouble(5, good.getPrice());
            statement.setInt(6, good.getGroupId());
            statement.setInt(7, good.getId());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0)
                throw new GoodNotFoundException("Good not found!");

            statement.close();
        } catch (SQLException e) {
            if (e.getMessage().contains("FOREIGN KEY constraint failed"))
                throw new GroupNotFoundException("This group does not exist!");
            else if (e.getMessage().contains("UNIQUE constraint failed: good.name"))
                throw new GoodAlreadyExistsException("Good with this name already exists!");
            else if (e.getMessage().contains("CHECK constraint failed: price >= 0"))
                throw new GoodPriceConstraintFailedException("Price can't be negative!");
            else if(e.getMessage().contains("CHECK constraint failed: quantity >= 0"))
                throw new GoodQuantityConstraintFailedException("Quantity can't be negative!");
            else
                e.printStackTrace();
        }
    }

    public void deleteGood(int id) throws GoodNotFoundException {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM good " +
                            "WHERE id = ?");
            statement.setInt(1, id);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0)
                throw new GoodNotFoundException("Good not found!");

            statement.close();
        } catch (SQLException e) {
            System.out.println("Invalid DELETE query");
            e.printStackTrace();
        }
    }

    public List<Good> listGoodsByCriteria(List<FilteringCriterion> filteringCriteria,
                                          List<SortingCriterion> sortingCriteria) {
        String query = buildQuery(filteringCriteria, sortingCriteria);
        return executeListByCriteriaQuery(query, filteringCriteria);
    }

    private String buildQuery(List<FilteringCriterion> filteringCriteria, List<SortingCriterion> sortingCriteria) {
        StringBuilder query = new StringBuilder("SELECT * FROM good");

        // Adding filtering criteria
        if (!filteringCriteria.isEmpty()) {
            query.append(" WHERE ");
            for (int i = 0; i < filteringCriteria.size(); i++) {
                FilteringCriterion criterion = filteringCriteria.get(i);
                query
                        .append(criterion.getFieldName())
                        .append(" ")
                        .append(criterion.getCondition())
                        .append(" ?");

                if (i < filteringCriteria.size() - 1)
                    query.append(" AND ");
            }
        }

        // Adding sorting criteria
        if (!sortingCriteria.isEmpty()) {
            query.append(" ORDER BY ");
            for (int i = 0; i < sortingCriteria.size(); i++) {
                SortingCriterion criterion = sortingCriteria.get(i);
                query.append(criterion.getFieldName());
                query.append(criterion.isAscending() ? " ASC" : " DESC");

                if (i < sortingCriteria.size() - 1)
                    query.append(", ");
            }
        }
        return query.toString();
    }

    private List<Good> executeListByCriteriaQuery(String query, List<FilteringCriterion> filteringCriteria) {
        List<Good> goods = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the values for the placeholders in the PreparedStatement
            for (int i = 0; i < filteringCriteria.size(); i++) {
                FilteringCriterion criterion = filteringCriteria.get(i);
                preparedStatement.setObject(i + 1, criterion.getValue());
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                goods = parseGoods(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return goods;
    }

    private List<Good> parseGoods(ResultSet resultSet) throws SQLException {
        List<Good> goods = new ArrayList<>();
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            String description = resultSet.getString("description");
            String producer = resultSet.getString("producer");
            int quantity = resultSet.getInt("quantity");
            double price = resultSet.getDouble("price");
            int groupId = resultSet.getInt("groupId");

            Good good = new Good(id, name, description, producer, quantity, price, groupId);
            goods.add(good);
        }
        return goods;
    }
}
