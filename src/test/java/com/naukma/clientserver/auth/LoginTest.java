package com.naukma.clientserver.auth;

import com.naukma.clientserver.exception.user.UserAlreadyExistsException;
import com.naukma.clientserver.https.LoginHandler;
import com.naukma.clientserver.https.Server;
import com.naukma.clientserver.mock.MockHttpExchange;
import com.naukma.clientserver.mock.MockUserService;
import com.naukma.clientserver.model.User;
import com.naukma.clientserver.service.UserService;
import com.naukma.clientserver.utils.PasswordHasher;
import com.sun.net.httpserver.Headers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {
    private static final String INVALID_TOKEN = "invalid_token";
    private static final String LOGIN = "testUser";
    private static final String PASSWORD = "testPassword";

    private static UserService mockUserService;

    @BeforeAll
    static void setUp() throws UserAlreadyExistsException, IOException {
        mockUserService = new MockUserService();
        mockUserService.createUser(new User("lillydaystar@gmail.com", "ThomasTheBest"));
        mockUserService.createUser(new User("testUser", "testPassword"));
        Server server = new Server();
        server.setUserService(mockUserService);
    }

    @Test
    void testValidToken() {
        String authorizationHeader = "Bearer " + MockHttpExchange.VALID_TOKEN;
        assertTrue(Server.isTokenValid(authorizationHeader));
    }

    @Test
    void testInvalidToken() {
        String authorizationHeader = "Bearer " + INVALID_TOKEN;
        assertFalse(Server.isTokenValid(authorizationHeader));
    }

    @Test
    void testLoginHandlerWithValidCredentials() {
        MockHttpExchange mockExchange = new MockHttpExchange();
        mockExchange.setRequestMethod("POST");
        String password = PasswordHasher.hashPassword(PASSWORD);
        mockExchange.setRequestURI(URI.create("/login?login=" + LOGIN + "&password=" + password));
        mockExchange.setRequestHeaders(new Headers());

        ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream();
        mockExchange.setResponseBody(responseOutputStream);

        assertDoesNotThrow(() -> {
            LoginHandler loginHandler = new LoginHandler(mockUserService);
            loginHandler.handle(mockExchange);
        });

        assertEquals(200, mockExchange.getResponseCode());
        assertNotNull(responseOutputStream.toString());
    }

    @Test
    void testLoginHandlerWithInvalidCredentials() {
        MockHttpExchange mockExchange = new MockHttpExchange();
        mockExchange.setRequestMethod("POST");
        mockExchange.setRequestURI(URI.create("/login?login=invalid&password=invalid"));
        mockExchange.setRequestHeaders(new Headers());

        ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream();
        mockExchange.setResponseBody(responseOutputStream);

        assertDoesNotThrow(() -> {
            LoginHandler loginHandler = new LoginHandler(mockUserService);
            loginHandler.handle(mockExchange);
        });

        assertEquals(401, mockExchange.getResponseCode());
        assertEquals("Unauthorized", responseOutputStream.toString());
    }

}
