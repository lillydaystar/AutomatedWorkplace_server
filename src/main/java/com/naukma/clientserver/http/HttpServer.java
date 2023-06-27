package com.naukma.clientserver.http;

import io.jsonwebtoken.security.Keys;
import com.naukma.clientserver.exception.GroupAlreadyExistsException;
import com.naukma.clientserver.model.Group;
import com.naukma.clientserver.service.DatabaseInitializationService;
import com.naukma.clientserver.service.GoodService;
import com.naukma.clientserver.service.GroupService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.sql.Connection;

public class HttpServer {
    private static final int SERVER_PORT = 8765;
    static Key SECRET_KEY = Keys.hmacShaKeyFor(System.getenv("SECRETKEY").getBytes(StandardCharsets.UTF_8));
    private static final String DATABASE = "automated_workplace";
    private static GroupService groupService;
    private static GoodService goodService;

    public HttpServer() throws IOException {
        com.sun.net.httpserver.HttpServer server = createServer();
        initializeServices();
        bindContexts(server);

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + SERVER_PORT);
    }

    private static com.sun.net.httpserver.HttpServer createServer() throws IOException {
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create();
        server.bind(new InetSocketAddress(SERVER_PORT), 0);
        return server;
    }

    private static void bindContexts(com.sun.net.httpserver.HttpServer server) {
        server.createContext("/login", new LoginHandler());
        server.createContext("/api/good", new GoodHandler(goodService));
    }

    private static void initializeServices()  {
        Connection dbConnection = establishDbConnection();
        groupService = new GroupService(dbConnection);
        try {
            groupService.createGroup(new Group("group", "description"));
        } catch (GroupAlreadyExistsException ignored) {}
        goodService = new GoodService(dbConnection);
    }

    private static Connection establishDbConnection() {
        DatabaseInitializationService databaseInitializationService = new DatabaseInitializationService();
        return databaseInitializationService.createConnection(DATABASE);
    }
}
