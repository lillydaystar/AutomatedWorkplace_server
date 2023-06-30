package com.naukma.clientserver.mock;

import com.naukma.clientserver.exception.user.UserAlreadyExistsException;
import com.naukma.clientserver.model.User;
import com.naukma.clientserver.service.UserService;
import com.naukma.clientserver.utils.PasswordHasher;

import java.util.HashMap;
import java.util.Map;

public class MockUserService extends UserService {
    private final Map<String, User> users;

    public MockUserService() {
        super(null);
        users = new HashMap<>();
    }

    @Override
    public void createUser(User user) throws UserAlreadyExistsException {
        if (users.containsKey(user.getLogin())) {
            throw new UserAlreadyExistsException("There is another user with this login!");
        }
        user.setPassword(PasswordHasher.hashPassword(user.getPassword()));
        users.put(user.getLogin(), user);
    }

    @Override
    public User getUser(String login) {
        return users.get(login);
    }
}
