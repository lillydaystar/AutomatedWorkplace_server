package com.naukma.clientserver.https;

import com.naukma.clientserver.service.GoodCriterions.FilteringCriterion;
import com.naukma.clientserver.service.GoodService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TotalSumHandler implements HttpHandler {
    private final GoodService goodService;

    public TotalSumHandler(GoodService goodService) {
        this.goodService = goodService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String authorizationHeader = exchange.getRequestHeaders().getFirst("Authorization");

        if (!Server.isTokenValid(authorizationHeader)) {
            ServerUtils.sendResponse(exchange, 403, "Forbidden");
            return;
        }

        String method = exchange.getRequestMethod();

        if (method.equalsIgnoreCase("GET")) {
            handleGetRequest(exchange);
        } else {
            ServerUtils.sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        Map<String, String> queryParams = ServerUtils.parseQueryParams(exchange.getRequestURI().getQuery());

        // Construct filtering criteria
        List<FilteringCriterion> filteringCriteria = new ArrayList<>();

        if (queryParams.containsKey("groupId"))
            filteringCriteria.add(new FilteringCriterion("groupId", "=", queryParams.get("groupId")));
        if (queryParams.containsKey("name"))
            filteringCriteria.add(new FilteringCriterion("name", "LIKE", queryParams.get("name")));

        double totalSum;

        if (filteringCriteria.isEmpty())
            totalSum = goodService.getTotalSumOfAllGoods();
        else
            totalSum = goodService.getTotalSumOfGoodsByCriteria(filteringCriteria);

        ServerUtils.sendResponse(exchange, 200, Double.toString(totalSum));
    }

}

