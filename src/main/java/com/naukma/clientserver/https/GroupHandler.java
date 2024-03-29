package com.naukma.clientserver.https;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naukma.clientserver.exception.group.GroupAlreadyExistsException;
import com.naukma.clientserver.exception.group.GroupNotFoundException;
import com.naukma.clientserver.model.Group;
import com.naukma.clientserver.request.GroupRequestData;
import com.naukma.clientserver.service.GroupService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


import java.io.IOException;
import java.util.List;

public class GroupHandler implements HttpHandler {
    private final GroupService groupService;

    public GroupHandler(GroupService groupService) {
        this.groupService = groupService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String authorizationHeader = exchange.getRequestHeaders().getFirst("Authorization");

        if (!Server.isTokenValid(authorizationHeader)) {
            ServerUtils.sendResponse(exchange, 403, "Forbidden");
            return;
        }

        String method = exchange.getRequestMethod();

        if (method.equalsIgnoreCase("POST"))
            handlePostRequest(exchange);
        else if (method.equalsIgnoreCase("PUT"))
            handlePutRequest(exchange);
        else if (method.equalsIgnoreCase("DELETE"))
            handleDeleteRequest(exchange);
        else if (method.equalsIgnoreCase("GET"))
            handleGetRequest(exchange);
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        String requestBody = ServerUtils.getRequestData(exchange);
        try {
            int createGroupId = createGroup(requestBody);
            String response = "Group with ID = " + createGroupId + " was created successfully!";
            ServerUtils.sendResponse(exchange, 201, response);
        } catch (GroupAlreadyExistsException e) {
            ServerUtils.sendResponse(exchange, 409, e.getMessage());
        }
    }

    private int createGroup(String requestBody) throws GroupAlreadyExistsException {
        GroupRequestData groupRequestData = parseGroupRequestData(requestBody);

        String name = groupRequestData.getName();
        String description = groupRequestData.getDescription();
        Group newGroup = new Group(name, description);

        groupService.createGroup(newGroup);

        try {
            return groupService.getGroupByName(name).getId();
        } catch (GroupNotFoundException e) {
            System.out.println("Can't get ID of created group.");
        }
        return -1;
    }

    private void handlePutRequest(HttpExchange exchange) throws IOException {
        String requestBody = ServerUtils.getRequestData(exchange);
        int id = ServerUtils.getIdFromRequestURI(exchange.getRequestURI().getPath());

        try {
            updateGroup(id, requestBody);
            ServerUtils.sendResponse(exchange, 204, "");
        } catch (GroupNotFoundException e) {
            ServerUtils.sendResponse(exchange, 404, e.getMessage());
        } catch (GroupAlreadyExistsException e) {
            ServerUtils.sendResponse(exchange, 409, e.getMessage());
        }
    }

    private void updateGroup(int id, String requestBody) throws GroupNotFoundException, GroupAlreadyExistsException {
        GroupRequestData groupRequestData = parseGroupRequestData(requestBody);

        String name = groupRequestData.getName();
        String description = groupRequestData.getDescription();
        Group updatedGroup = new Group(id, name, description);

        groupService.updateGroup(updatedGroup);
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        int id = ServerUtils.getIdFromRequestURI(exchange.getRequestURI().getPath());

        if (id == -1)
            handleGetAllRequest(exchange);
        else {
            handleGetOneRequest(exchange, id);
        }
    }

    private void handleGetOneRequest(HttpExchange exchange, int id) throws IOException {
        try {
            String responseInfo = retrieveGroup(id);
            ServerUtils.sendResponse(exchange, 200, responseInfo);
        } catch (GroupNotFoundException e) {
            e.printStackTrace();
            ServerUtils.sendResponse(exchange, 404, e.getMessage());
        }
    }

    private String retrieveGroup(int id) throws GroupNotFoundException {
        Group group = groupService.getGroupById(id);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(group);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error serializing Group object to JSON", e);
        }
    }

    private void handleGetAllRequest(HttpExchange exchange) throws IOException {
        List<Group> groups;
        if (exchange.getRequestURI().getQuery() == null) {
            groups = groupService.getAllGroups();
        } else {
            String order = exchange.getRequestURI().getQuery().split("=")[1];
            groups = groupService.getGroupsSortedByName(order);
        }
        String responseInfo = retrieveGroups(groups);
        ServerUtils.sendResponse(exchange, 200, responseInfo);
    }


    private String retrieveGroups(List<Group> groups) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(groups);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error serializing Group list to JSON", e);
        }
    }

    private void handleDeleteRequest(HttpExchange exchange) throws IOException {
        int id = ServerUtils.getIdFromRequestURI(exchange.getRequestURI().getPath());

        try {
            groupService.deleteGroup(id);
            ServerUtils.sendResponse(exchange, 204, "");
        } catch (GroupNotFoundException e) {
            ServerUtils.sendResponse(exchange, 404, e.getMessage());
        }
    }

    private GroupRequestData parseGroupRequestData(String requestBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(requestBody, GroupRequestData.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error deserializing request body", e);
        }
    }

}
