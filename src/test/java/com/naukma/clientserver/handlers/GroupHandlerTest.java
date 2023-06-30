package com.naukma.clientserver.handlers;

import com.naukma.clientserver.exception.group.GroupAlreadyExistsException;
import com.naukma.clientserver.exception.group.GroupNotFoundException;
import com.naukma.clientserver.exception.user.UserAlreadyExistsException;
import com.naukma.clientserver.https.GroupHandler;
import com.naukma.clientserver.https.Server;
import com.naukma.clientserver.mock.MockGroupService;
import com.naukma.clientserver.mock.MockHttpExchange;
import com.naukma.clientserver.mock.MockUserService;
import com.naukma.clientserver.model.Group;
import com.naukma.clientserver.model.User;
import com.naukma.clientserver.service.GroupService;
import com.naukma.clientserver.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

public class GroupHandlerTest {
    private static GroupService mockGroupService;
    private static MockHttpExchange mockHttpExchange;
    private static GroupHandler groupHandler;

    @BeforeAll
    static void setUp() throws UserAlreadyExistsException, IOException, GroupAlreadyExistsException {
        mockGroupService = new MockGroupService();
        mockHttpExchange = new MockHttpExchange();
        groupHandler = new GroupHandler(mockGroupService);

        UserService mockUserService = new MockUserService();
        mockUserService.createUser(new User("lillydaystar@gmail.com", "ThomasTheBest"));
        Server server = new Server();
        server.setUserService(mockUserService);

        Group existingGroup = new Group("Group3", "Description1");
        mockGroupService.createGroup(existingGroup);
    }

    @Test
    void handlePostRequest_Returns201_and_Returns409() throws IOException, GroupNotFoundException {

        String requestBody = "{\"name\":\"Group1\",\"description\":\"Description1\"}";
        mockHttpExchange.setRequestMethod("POST");
        mockHttpExchange.setRequestURI(URI.create("/api/group"));
        mockHttpExchange.getRequestHeaders().add("Authorization", "Bearer " + MockHttpExchange.VALID_TOKEN);
        mockHttpExchange.setRequestBody(requestBody);
        mockHttpExchange.setResponseBody(new ByteArrayOutputStream());

        groupHandler.handle(mockHttpExchange);

        assertEquals(201, mockHttpExchange.getResponseCode());
        String responseBody = mockHttpExchange.getResponseBody().toString();
        assertNotNull(responseBody);
        assertEquals("Group with ID = 2 was created successfully!", responseBody);

        Group createdGroup = mockGroupService.getGroupById(2);
        assertNotNull(createdGroup);
        assertEquals("Group1", createdGroup.getName());
        assertEquals("Description1", createdGroup.getDescription());


        requestBody = "{\"name\":\"Group1\",\"description\":\"Description1\"}";
        mockHttpExchange.setRequestMethod("POST");
        mockHttpExchange.setRequestURI(URI.create("/api/group"));
        mockHttpExchange.getRequestHeaders().add("Authorization", "Bearer " + MockHttpExchange.VALID_TOKEN);
        mockHttpExchange.setRequestBody(requestBody);
        mockHttpExchange.setResponseBody(new ByteArrayOutputStream());

        groupHandler.handle(mockHttpExchange);

        assertEquals(409, mockHttpExchange.getResponseCode());
        responseBody = mockHttpExchange.getResponseBody().toString();
        assertNotNull(responseBody);
        assertEquals("Group with this name already exists", responseBody);
    }


    @Test
    void handlePutRequest_Returns204() throws IOException, GroupNotFoundException, GroupAlreadyExistsException {

        String requestBody = "{\"name\":\"UpdatedGroup\",\"description\":\"UpdatedDescription\"}";
        mockHttpExchange.setRequestMethod("PUT");
        mockHttpExchange.setRequestURI(URI.create("/api/group/1"));
        mockHttpExchange.getRequestHeaders().add("Authorization", "Bearer " + MockHttpExchange.VALID_TOKEN);
        mockHttpExchange.setRequestBody(requestBody);
        mockHttpExchange.setResponseBody(new ByteArrayOutputStream());

        Group existingGroup = new Group("Group2", "Description1");
        mockGroupService.createGroup(existingGroup);
        groupHandler.handle(mockHttpExchange);

        assertEquals(204, mockHttpExchange.getResponseCode());
        String responseBody = mockHttpExchange.getResponseBody().toString();
        assertNotNull(responseBody);
        assertEquals("", responseBody);

        Group updatedGroup = mockGroupService.getGroupById(1);
        assertNotNull(updatedGroup);
        assertEquals("UpdatedGroup", updatedGroup.getName());
        assertEquals("UpdatedDescription", updatedGroup.getDescription());
    }

    @Test
    void handlePutRequest_GroupNotFound() throws IOException {
        String requestBody = "{\"name\":\"UpdatedGroup\",\"description\":\"UpdatedDescription\"}";
        mockHttpExchange.setRequestMethod("PUT");
        mockHttpExchange.setRequestURI(URI.create("/api/group/10"));
        mockHttpExchange.getRequestHeaders().add("Authorization", "Bearer " + MockHttpExchange.VALID_TOKEN);
        mockHttpExchange.setRequestBody(requestBody);
        mockHttpExchange.setResponseBody(new ByteArrayOutputStream());

        groupHandler.handle(mockHttpExchange);

        assertEquals(404, mockHttpExchange.getResponseCode());
        String responseBody = mockHttpExchange.getResponseBody().toString();
        assertNotNull(responseBody);
        assertEquals("Not found", responseBody);
    }

    @Test
    void handleDeleteRequest_Returns204() throws IOException, GroupNotFoundException {
        mockHttpExchange.setRequestMethod("DELETE");
        mockHttpExchange.setRequestURI(URI.create("/api/group/0"));
        mockHttpExchange.getRequestHeaders().add("Authorization", "Bearer " + MockHttpExchange.VALID_TOKEN);
        mockHttpExchange.setResponseBody(new ByteArrayOutputStream());



        groupHandler.handle(mockHttpExchange);

        assertEquals(204, mockHttpExchange.getResponseCode());
        String responseBody = mockHttpExchange.getResponseBody().toString();
        assertNotNull(responseBody);
        assertEquals("", responseBody);

        Group deletedGroup = mockGroupService.getGroupById(0);
        assertNull(deletedGroup);
    }

    @Test
    void handleDeleteRequest_GroupNotFound() throws IOException {
        mockHttpExchange.setRequestMethod("DELETE");
        mockHttpExchange.setRequestURI(URI.create("/api/group/10"));
        mockHttpExchange.getRequestHeaders().add("Authorization", "Bearer " + MockHttpExchange.VALID_TOKEN);
        mockHttpExchange.setResponseBody(new ByteArrayOutputStream());

        groupHandler.handle(mockHttpExchange);

        assertEquals(404, mockHttpExchange.getResponseCode());
        String responseBody = mockHttpExchange.getResponseBody().toString();
        assertNotNull(responseBody);
        assertEquals("Not found", responseBody);
    }
}
