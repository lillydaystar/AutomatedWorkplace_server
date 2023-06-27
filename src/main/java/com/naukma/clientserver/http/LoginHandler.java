package com.naukma.clientserver.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Jwts;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class LoginHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String login = exchange.getRequestURI().getQuery().split("&")[0].split("=")[1];
            String password = exchange.getRequestURI().getQuery().split("&")[1].split("=")[1];

            boolean isValid = checkLogin(login, password);

            if (isValid) {
                try {
                    String jwtToken = Jwts.builder()
                            .setSubject(login)
                            .setIssuedAt(new Date())
                            .signWith(HttpServer.SECRET_KEY)
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
        final String hardcodedLogin = "login";
        final String hashedHardcodedPassword = "5f4dcc3b5aa765d61d8327deb882cf99"; // MD5 hash of "password"

        return hardcodedLogin.equals(login) && hashedHardcodedPassword.equals(password);
    }
}