package com.naukma.clientserver.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;

public class ServerUtils {
    public static String getRequestData(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));

        StringBuilder requestData = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            requestData.append(line);

        return requestData.toString();
    }

    public static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        if (statusCode == 204) {
            exchange.sendResponseHeaders(statusCode, -1);
        } else {
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
