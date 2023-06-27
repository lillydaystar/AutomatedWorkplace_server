package com.naukma.clientserver.http;

import com.naukma.clientserver.exception.user.UserAlreadyExistsException;
import com.naukma.clientserver.model.User;
import com.naukma.clientserver.service.UserService;
import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.security.Keys;
import com.naukma.clientserver.exception.group.GroupAlreadyExistsException;
import com.naukma.clientserver.model.Group;
import com.naukma.clientserver.service.DatabaseInitializationService;
import com.naukma.clientserver.service.GoodService;
import com.naukma.clientserver.service.GroupService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.sql.Connection;

public class Server {
    private static final int SERVER_PORT = 8765;
    static Key SECRET_KEY = Keys.hmacShaKeyFor(System.getenv("SECRETKEY").getBytes(StandardCharsets.UTF_8));
    private static final String DATABASE = "automated_workplace";
    private GroupService groupService;
    private GoodService goodService;
    private UserService userService;

    public Server() throws IOException {
        HttpServer server = createServer();
        initializeServices();
        bindContexts(server);

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + SERVER_PORT);
    }

    private com.sun.net.httpserver.HttpServer createServer() throws IOException {
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create();
        server.bind(new InetSocketAddress(SERVER_PORT), 0);
        return server;
    }

    private void bindContexts(com.sun.net.httpserver.HttpServer server) {
        server.createContext("/login", new LoginHandler());
        server.createContext("/api/good", new GoodHandler(goodService));
    }

    private void initializeServices()  {
        Connection dbConnection = establishDbConnection();
        groupService = new GroupService(dbConnection);
        try {
            groupService.createGroup(new Group("group", "description"));
        } catch (GroupAlreadyExistsException ignored) {}

        goodService = new GoodService(dbConnection);

        userService = new UserService(dbConnection);
        try {
            userService.createUser(new User("lillydaystar@gmail.com", "ThomasTheBest"));
        } catch (UserAlreadyExistsException ignored) {}
    }

    private Connection establishDbConnection() {
        DatabaseInitializationService databaseInitializationService = new DatabaseInitializationService();
        return databaseInitializationService.createConnection(DATABASE);
    }
}
