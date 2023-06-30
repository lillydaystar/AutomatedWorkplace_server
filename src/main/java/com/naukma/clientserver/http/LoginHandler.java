package com.naukma.clientserver.http;

import com.naukma.clientserver.model.User;
import com.naukma.clientserver.service.UserService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class LoginHandler implements HttpHandler {
    private final UserService userService;

    public LoginHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String login = exchange.getRequestURI().getQuery().split("&")[0].split("=")[1];
            String password = exchange.getRequestURI().getQuery().split("&")[1].split("=")[1];

            boolean isValid = checkLogin(login, password);

            if (isValid) {
                try {
                    long expirationTimeMillis = System.currentTimeMillis() + 24 * 60 * 60 * 1000; // Поточний час + 24 години
                    Date expirationDate = new Date(expirationTimeMillis);
                    Claims claims = Jwts.claims();
                    claims.setSubject(login);
                    claims.setExpiration(expirationDate);

                    String jwtToken = Jwts.builder()
                            .setClaims(claims)
                            .setIssuedAt(new Date())
                            .signWith(Server.SECRET_KEY)
                            .compact();

                    // Convert token to JSON format
                    String tokenInJson = "{\"token\":\"" + jwtToken + "\"}";

                    byte[] responseBytes = tokenInJson.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(responseBytes);
                    os.close();
                } catch (Exception e) {
                    System.out.println("Error generating token:");
                    e.printStackTrace();
                }
            } else {
                byte[] responseBytes = "Unauthorized".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(401, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();
            }
        } else {
            String response = "405 Method Not Allowed";
            ServerUtils.sendResponse(exchange, 405, response);
        }
    }

    private boolean checkLogin(String login, String password) {
        User user = userService.getUser(login);
        return user != null && user.getPassword().equals(password);
    }
}