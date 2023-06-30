package com.naukma.clientserver.handlers;

import com.naukma.clientserver.exception.user.UserAlreadyExistsException;
import com.naukma.clientserver.http.Server;
import com.naukma.clientserver.http.TotalSumHandler;
import com.naukma.clientserver.mock.MockGoodService;
import com.naukma.clientserver.mock.MockHttpExchange;
import com.naukma.clientserver.mock.MockUserService;
import com.naukma.clientserver.model.User;
import com.naukma.clientserver.service.GoodService;
import com.naukma.clientserver.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class TotalSumHandlerTest {

    private static GoodService mockGoodService;
    private static MockHttpExchange mockHttpExchange;
    private static TotalSumHandler totalSumHandler;

    @BeforeAll
    static void setUp() throws UserAlreadyExistsException, IOException {
        mockGoodService = new MockGoodService();
        mockHttpExchange = new MockHttpExchange();
        totalSumHandler = new TotalSumHandler(mockGoodService);

        UserService mockUserService = new MockUserService();
        mockUserService.createUser(new User("lillydaystar@gmail.com", "ThomasTheBest"));
        Server server = new Server();
        server.setUserService(mockUserService);
    }

    @Test
    void handleGetRequest_NoFilteringCriteria() throws IOException {
        URI mockRequestURI = URI.create("/goods");
        mockHttpExchange.setRequestURI(mockRequestURI);
        mockHttpExchange.setRequestMethod("GET");
        ((MockGoodService) mockGoodService).setTotalSumOfAllGoods(1000.0);
        ((MockGoodService) mockGoodService).setTotalSumOfGoodsByCriteria(0.0);
        mockHttpExchange.getRequestHeaders().add("Authorization", "Bearer " + MockHttpExchange.VALID_TOKEN);

        ByteArrayOutputStream mockResponseBody = new ByteArrayOutputStream();
        mockHttpExchange.setResponseBody(mockResponseBody);

        totalSumHandler.handle(mockHttpExchange);

        assertEquals(200, mockHttpExchange.getResponseCode());
        assertEquals("1000.0", mockResponseBody.toString());
    }

    @Test
    void handleGetRequest_ReturnsTotalSumOfGoodsByCriteria() throws IOException {
        URI mockRequestURI = URI.create("/goods?groupId=1&name=Shirt");
        mockHttpExchange.setRequestURI(mockRequestURI);
        mockHttpExchange.setRequestMethod("GET");
        ((MockGoodService) mockGoodService).setTotalSumOfAllGoods(0.0);
        ((MockGoodService) mockGoodService).setTotalSumOfGoodsByCriteria(500.0);
        mockHttpExchange.getRequestHeaders().add("Authorization", "Bearer " + MockHttpExchange.VALID_TOKEN);

        ByteArrayOutputStream mockResponseBody = new ByteArrayOutputStream();
        mockHttpExchange.setResponseBody(mockResponseBody);

        totalSumHandler.handle(mockHttpExchange);

        assertEquals(200, mockHttpExchange.getResponseCode());
        assertEquals("500.0", mockResponseBody.toString());
    }

    @Test
    void handleGetRequest_InvalidMethod() throws IOException {
        mockHttpExchange.setRequestMethod("POST");

        ByteArrayOutputStream mockResponseBody = new ByteArrayOutputStream();
        mockHttpExchange.setResponseBody(mockResponseBody);

        totalSumHandler.handle(mockHttpExchange);

        assertEquals(405, mockHttpExchange.getResponseCode());
        assertEquals("Method Not Allowed", mockResponseBody.toString());
    }
}
