package com.naukma.clientserver.mock;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

public class MockHttpExchange extends HttpExchange {
    public static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsaWxseWRheXN0YXJAZ21haWwuY29tIiwiZXhwIjoxNjg4MTUyNDM0LCJpYXQiOjE2ODgwNjYwMzR9.Mx4ahmyWvJIT66r3A0EiiZULOfc4EDqrMBMH-JmI0Ac";
    private String requestMethod;
    private URI requestURI;
    private Headers requestHeaders;
    private int responseCode;
    private ByteArrayOutputStream responseBody;

    public MockHttpExchange() {
        responseBody = new ByteArrayOutputStream();
        requestHeaders = new Headers();
    }

    @Override
    public Headers getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public Headers getResponseHeaders() {
        return new Headers();
    }

    @Override
    public URI getRequestURI() {
        return requestURI;
    }

    @Override
    public String getRequestMethod() {
        return requestMethod;
    }

    @Override
    public HttpContext getHttpContext() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public InputStream getRequestBody() {
        return null;
    }

    @Override
    public OutputStream getResponseBody() {
        return responseBody;
    }

    @Override
    public void sendResponseHeaders(int responseCode, long responseLength) {
        this.responseCode = responseCode;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return null;
    }

    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return null;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public void setAttribute(String name, Object value) {

    }

    @Override
    public void setStreams(InputStream i, OutputStream o) {

    }

    @Override
    public HttpPrincipal getPrincipal() {
        return null;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setRequestURI(URI requestURI) {
        this.requestURI = requestURI;
    }

    public void setRequestHeaders(Headers requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setResponseBody(ByteArrayOutputStream responseBody) {
        this.responseBody = responseBody;
    }
}
