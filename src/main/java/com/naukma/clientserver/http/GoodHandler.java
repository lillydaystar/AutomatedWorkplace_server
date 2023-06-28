package com.naukma.clientserver.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naukma.clientserver.request.GoodCriteriaRequestData;
import com.naukma.clientserver.request.GoodRequestData;
import com.naukma.clientserver.service.GoodCriterions.FilteringCriterion;
import com.naukma.clientserver.service.GoodCriterions.SortingCriterion;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.naukma.clientserver.exception.good.*;
import com.naukma.clientserver.exception.group.*;
import com.naukma.clientserver.model.Good;
import com.naukma.clientserver.service.GoodService;

import java.io.IOException;
import java.util.List;

public class GoodHandler implements HttpHandler {
    private final GoodService goodService;

    public GoodHandler(GoodService goodService) {
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

        if (method.equalsIgnoreCase("GET"))
            handleGetRequest(exchange);
        else if (method.equalsIgnoreCase("POST"))
            handlePostRequest(exchange);
        else if(method.equalsIgnoreCase("PUT"))
            handlePutRequest(exchange);
        else if(method.equalsIgnoreCase("DELETE"))
            handleDeleteRequest(exchange);
        else
            ServerUtils.sendResponse(exchange, 405, "Method Not Allowed!");
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        int id = ServerUtils.getIdFromRequestURI(exchange.getRequestURI().getPath());

        if (id == -1)
            handleGetAllRequest(exchange);
        else
            handleGetOneRequest(exchange, id);
    }

    private void handleGetAllRequest(HttpExchange exchange) throws IOException {
        List<Good> goods;
        String requestBody = ServerUtils.getRequestData(exchange);
        if (requestBody.equals(""))
            goods = goodService.getAllGoods();
        else
            goods = getGoodsWithCriteria(requestBody);
        String responseInfo = retrieveGoods(goods);
        ServerUtils.sendResponse(exchange, 200, responseInfo);
    }

    private String retrieveGoods(List<Good> goods) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(goods);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error serializing Good object to JSON", e);
        }
    }

    private List<Good> getGoodsWithCriteria(String requestBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            GoodCriteriaRequestData criteriaObject = objectMapper.readValue(requestBody, GoodCriteriaRequestData.class);
            List<FilteringCriterion> filteringCriteria = criteriaObject.getFilteringCriteria();
            List<SortingCriterion> sortingCriteria = criteriaObject.getSortingCriteria();

            return goodService.listGoodsByCriteria(filteringCriteria, sortingCriteria);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error serializing Good object to JSON", e);
        }
    }

    private void handleGetOneRequest(HttpExchange exchange, int id) throws IOException {
        try {
            String responseInfo = retrieveGood(id);
            ServerUtils.sendResponse(exchange, 200, responseInfo);
        } catch (GoodNotFoundException e) {
            e.printStackTrace();
            ServerUtils.sendResponse(exchange, 404, e.getMessage());
        }
    }

    private String retrieveGood(int id) throws GoodNotFoundException {
        Good good = goodService.getGoodById(id);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(good);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error serializing Good object to JSON", e);
        }
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        String requestBody = ServerUtils.getRequestData(exchange);
        try {
            int createGoodId = createGood(requestBody);
            String response = "Good with ID = " + createGoodId + " was created successfully!";
            ServerUtils.sendResponse(exchange, 201, response);
        } catch (GroupNotFoundException | GoodAlreadyExistsException | GoodPriceConstraintFailedException e) {
            e.printStackTrace();
            ServerUtils.sendResponse(exchange, 409, e.getMessage());
        }
    }

    private int createGood(String requestBody) throws GroupNotFoundException, GoodAlreadyExistsException,
            GoodPriceConstraintFailedException {
        GoodRequestData requestData = parseGoodRequestData(requestBody);

        String name = requestData.getName();
        String description = requestData.getDescription();
        String producer = requestData.getProducer();
        double price = requestData.getPrice();
        int groupId = requestData.getGroupId();

        Good newGood = new Good(name, description, producer, price, groupId);
        goodService.createGood(newGood);
        try {
            return goodService.getGoodByName(name).getId();
        } catch (GoodNotFoundException e) {
            System.out.println("Can't get ID of created good.");
        }
        return -1;
    }

    private void handlePutRequest(HttpExchange exchange) throws IOException {
        String requestBody = ServerUtils.getRequestData(exchange);
        int id = ServerUtils.getIdFromRequestURI(exchange.getRequestURI().getPath());

        try {
            updateGood(id, requestBody);
            ServerUtils.sendResponse(exchange, 204, "");
        } catch (GoodNotFoundException e) {
            ServerUtils.sendResponse(exchange, 404, e.getMessage());
        } catch (GroupNotFoundException | GoodQuantityConstraintFailedException | GoodAlreadyExistsException
                 | GoodPriceConstraintFailedException e) {
            ServerUtils.sendResponse(exchange, 409, e.getMessage());
        }
    }

    private void updateGood(int id, String requestBody) throws GroupNotFoundException, GoodAlreadyExistsException,
            GoodPriceConstraintFailedException, GoodQuantityConstraintFailedException, GoodNotFoundException {
        GoodRequestData requestData = parseGoodRequestData(requestBody);

        String name = requestData.getName();
        String description = requestData.getDescription();
        String producer = requestData.getProducer();
        int quantity = requestData.getQuantity();
        double price = requestData.getPrice();
        int groupId = requestData.getGroupId();

        Good updatedGood = new Good(id, name, description, producer, quantity, price, groupId);

        goodService.updateGood(updatedGood);
    }

    private void handleDeleteRequest(HttpExchange exchange) throws IOException {
        int id = ServerUtils.getIdFromRequestURI(exchange.getRequestURI().getPath());

        try {
            deleteGood(id);
            ServerUtils.sendResponse(exchange, 204, "");
        } catch (GoodNotFoundException e) {
            ServerUtils.sendResponse(exchange, 404, e.getMessage());
        }
    }

    private void deleteGood(int id) throws GoodNotFoundException {
        goodService.deleteGood(id);
    }

    private GoodRequestData parseGoodRequestData(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, GoodRequestData.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error deserializing request body", e);
        }
    }

}