package com.naukma.clientserver.handlers;

import com.naukma.clientserver.exception.user.UserAlreadyExistsException;
import com.naukma.clientserver.https.GoodHandler;
import com.naukma.clientserver.https.Server;
import com.naukma.clientserver.mock.MockGoodService;
import com.naukma.clientserver.mock.MockHttpExchange;
import com.naukma.clientserver.mock.MockUserService;
import com.naukma.clientserver.model.Good;
import com.naukma.clientserver.model.User;
import com.naukma.clientserver.service.GoodService;
import com.naukma.clientserver.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GoodHandlerTest {
    private static GoodService mockGoodService;
    private static MockHttpExchange mockHttpExchange;
    private static GoodHandler goodHandler;

    @BeforeAll
    static void setUp() throws UserAlreadyExistsException, IOException {
        mockGoodService = new MockGoodService();
        mockHttpExchange = new MockHttpExchange();
        goodHandler = new GoodHandler(mockGoodService);

        UserService mockUserService = new MockUserService();
        mockUserService.createUser(new User("lillydaystar@gmail.com", "ThomasTheBest"));
        Server server = new Server();
        server.setUserService(mockUserService);
    }

    @Test
    public void handleGetAllRequest_Returns200() throws IOException {
        mockHttpExchange.setRequestMethod("GET");
        mockHttpExchange.setRequestURI(URI.create("/goods"));
        mockHttpExchange.getRequestHeaders().add("Authorization", "Bearer your_token");
        mockHttpExchange.setResponseBody(new ByteArrayOutputStream());

        Map<Integer, Good> mockGoods = new HashMap<>();
        mockGoods.put(0, new Good("Good1", "Description1", "Producer1", 10.0, 1));
        mockGoods.put(1, new Good("Good2", "Description2", "Producer2", 20.0, 1));
        ((MockGoodService) mockGoodService).setMockGoods(mockGoods);

        goodHandler.handle(mockHttpExchange);

        assertEquals(200, mockHttpExchange.getResponseCode());
        String responseBody = mockHttpExchange.getResponseBody().toString();
        assertNotNull(responseBody);
    }

    @Test
    public void handleGetOneRequest_Returns200() throws IOException {
        mockHttpExchange.setRequestMethod("GET");
        mockHttpExchange.setRequestURI(URI.create("/goods/1"));
        mockHttpExchange.getRequestHeaders().add("Authorization", "Bearer " + MockHttpExchange.VALID_TOKEN);
        mockHttpExchange.setResponseBody(new ByteArrayOutputStream());

        Good mockGood = new Good("Good1", "Description1", "Producer1", 10.0, 1);
        ((MockGoodService) mockGoodService).setMockGood(mockGood);

        goodHandler.handle(mockHttpExchange);

        assertEquals(200, mockHttpExchange.getResponseCode());

        String responseBody = mockHttpExchange.getResponseBody().toString();
        assertNotNull(responseBody);
    }

    @Test
    public void handlePostRequest_Returns201() throws IOException {
        mockHttpExchange.setRequestMethod("POST");
        mockHttpExchange.setRequestURI(URI.create("/goods"));
        mockHttpExchange.getRequestHeaders().add("Authorization", "Bearer " + MockHttpExchange.VALID_TOKEN);
        mockHttpExchange.setRequestBody("{\n" +
                "  \"name\": \"New Good\",\n" +
                "  \"description\": \"New Description\",\n" +
                "  \"producer\": \"New Producer\",\n" +
                "  \"price\": 30.0,\n" +
                "  \"groupId\": 1\n" +
                "}");
        mockHttpExchange.setResponseBody(new ByteArrayOutputStream());

        goodHandler.handle(mockHttpExchange);

        assertEquals(201, mockHttpExchange.getResponseCode());

        String responseBody = mockHttpExchange.getResponseBody().toString();
        assertNotNull(responseBody);
        assertEquals("Good with ID = 0 was created successfully!", responseBody);
    }

    @Test
    public void handlePutRequest_Returns204() throws IOException {
        mockHttpExchange.setRequestMethod("PUT");
        mockHttpExchange.setRequestURI(URI.create("/api/good/1"));
        mockHttpExchange.getRequestHeaders().add("Authorization", "Bearer " + MockHttpExchange.VALID_TOKEN);
        mockHttpExchange.setRequestBody("{\n" +
                "  \"name\": \"Updated Good\",\n" +
                "  \"description\": \"Updated Description\",\n" +
                "  \"producer\": \"Updated Producer\",\n" +
                "  \"price\": 40.0,\n" +
                "  \"groupId\": 2\n" +
                "}");
        mockHttpExchange.setResponseBody(new ByteArrayOutputStream());

        Good mockGood = new Good("Good1", "Description1", "Producer1", 10.0, 1);
        ((MockGoodService) mockGoodService).setMockGood(mockGood);

        goodHandler.handle(mockHttpExchange);

        assertEquals(204, mockHttpExchange.getResponseCode());

        String responseBody = mockHttpExchange.getResponseBody().toString();
        assertNotNull(responseBody);
        assertEquals("", responseBody);
    }

    @Test
    public void handleDeleteRequest_Returns204() throws IOException {
        Good mockGood = new Good("Good1", "Description1", "Producer1", 10.0, 1);
        ((MockGoodService) mockGoodService).setMockGood(mockGood);
        mockHttpExchange.setRequestMethod("DELETE");
        mockHttpExchange.setRequestURI(URI.create("/api/good/1"));
        mockHttpExchange.getRequestHeaders().add("Authorization", "Bearer " + MockHttpExchange.VALID_TOKEN);
        mockHttpExchange.setResponseBody(new ByteArrayOutputStream());

        goodHandler.handle(mockHttpExchange);

        assertEquals(204, mockHttpExchange.getResponseCode());

        String responseBody = mockHttpExchange.getResponseBody().toString();
        assertNotNull(responseBody);
        assertEquals("", responseBody);
    }
}