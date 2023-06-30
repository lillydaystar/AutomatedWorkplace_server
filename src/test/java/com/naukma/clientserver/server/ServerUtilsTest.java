package com.naukma.clientserver.server;

import com.naukma.clientserver.https.ServerUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServerUtilsTest {

    @Test
    void getIdFromRequestURI_ValidURI_ReturnsId() {
        String uri = "/api/items/123";

        int result = ServerUtils.getIdFromRequestURI(uri);

        assertEquals(123, result);
    }

    @Test
    void getIdFromRequestURI_InvalidURI_ReturnsMinusOne() {
        String uri = "/api/items";

        int result = ServerUtils.getIdFromRequestURI(uri);

        assertEquals(-1, result);
    }

    @Test
    void parseQueryParams_ValidQuery_ReturnsQueryParams() {
        String query = "param1=value1&param2=value2";

        Map<String, String> queryParams = ServerUtils.parseQueryParams(query);

        assertEquals(2, queryParams.size());
        assertEquals("value1", queryParams.get("param1"));
        assertEquals("value2", queryParams.get("param2"));
    }

    @Test
    void parseQueryParams_InvalidQuery_ReturnsEmptyMap() {
        String query = "param1";

        Map<String, String> queryParams = ServerUtils.parseQueryParams(query);

        assertEquals(0, queryParams.size());
    }
}
