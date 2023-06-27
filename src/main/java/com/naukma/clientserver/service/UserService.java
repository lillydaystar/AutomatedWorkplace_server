package com.naukma.clientserver.service;

import com.naukma.clientserver.exception.user.UserAlreadyExistsException;
import com.naukma.clientserver.model.User;
import com.naukma.clientserver.utils.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {
    private final Connection connection;

    public UserService(Connection connection) {
        this.connection = connection;
    }

    public void createUser(User user) throws UserAlreadyExistsException {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO user (login, password) VALUES (?, ?)");
            statement.setString(1, user.getLogin());

            String hashedPassword = PasswordHasher.hashPassword(user.getPassword());
            statement.setString(2, hashedPassword);

            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            if(e.getMessage().contains("UNIQUE constraint failed: user.login"))
                throw new UserAlreadyExistsException("There is another user with this login!");
            else
                e.printStackTrace();
        }
    }

    public User getUser(String login) {
        User user = null;
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM user WHERE login = ?");
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next())
                user = new User(resultSet.getString("login"), resultSet.getString("password"));

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            System.out.println("SQL exception: get User by login");
            e.printStackTrace();
        }
        return user;
    }
}
