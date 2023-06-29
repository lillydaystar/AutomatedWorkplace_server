package com.naukma.clientserver.http;

import com.naukma.clientserver.exception.user.UserAlreadyExistsException;
import com.naukma.clientserver.model.User;
import com.naukma.clientserver.service.UserService;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.naukma.clientserver.exception.group.GroupAlreadyExistsException;
import com.naukma.clientserver.model.Group;
import com.naukma.clientserver.service.DatabaseInitializationService;
import com.naukma.clientserver.service.GoodService;
import com.naukma.clientserver.service.GroupService;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.sql.Connection;
import java.util.Date;

public class Server {
    private static final int SERVER_PORT = 8080;
    static Key SECRET_KEY = Keys.hmacShaKeyFor(System.getenv("SECRETKEY").getBytes(StandardCharsets.UTF_8));
    private static final String DATABASE = "automated_workplace";
    private GroupService groupService;
    private GoodService goodService;
    private static UserService userService;

    public Server() {
        HttpsServer server;
        try {
            server = createServer();
        } catch (Exception e) {
            throw new RuntimeException("Server error!");
        }
        initializeServices();
        bindContexts(server);

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + SERVER_PORT);
    }

    private HttpsServer createServer() throws IOException, KeyStoreException, CertificateException,
            NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        // Create an instance of an HTTPS server.
        HttpsServer server = HttpsServer.create();

        // Bind the server to a specific port (defined in SERVER_PORT) and allow an unlimited backlog of incoming connections.
        server.bind(new InetSocketAddress(SERVER_PORT), 0);

        // Convert the password string into a character array.
        char[] passphrase = "password".toCharArray();

        // Create an instance of KeyStore with Java KeyStore (JKS) as the type.
        KeyStore ks = KeyStore.getInstance("JKS");

        // Load the keystore file (your-keystore.jks) into the KeyStore instance.
        ks.load(Files.newInputStream(Paths.get("your-keystore.jks")), passphrase);

        // Get a KeyManagerFactory instance using the SunX509 algorithm.
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

        // Initialize the KeyManagerFactory with the KeyStore and passphrase.
        kmf.init(ks, passphrase);

        // Get a TrustManagerFactory instance using the SunX509 algorithm.
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

        // Initialize the TrustManagerFactory with the KeyStore.
        tmf.init(ks);

        // Get an instance of SSLContext for TLS (Transport Layer Security) protocol.
        SSLContext ssl = SSLContext.getInstance("TLS");

        // Initialize the SSLContext with the key managers and trust managers.
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        // Set the HttpsConfigurator to the server to determine the encryption settings for secure connections.
        server.setHttpsConfigurator(new HttpsConfigurator(ssl) {
            public void configure(HttpsParameters params) {
                try {
                    // Get the default SSLContext.
                    SSLContext c = SSLContext.getDefault();

                    // Create an SSLEngine instance from the SSLContext.
                    SSLEngine engine = c.createSSLEngine();

                    // Set whether client authentication is needed.
                    params.setNeedClientAuth(false);

                    // Set the cipher suites that will be enabled for use.
                    params.setCipherSuites(engine.getEnabledCipherSuites());

                    // Set the versions of the SSL/TLS protocol that will be enabled for use.
                    params.setProtocols(engine.getEnabledProtocols());

                    // Get the default SSL parameters.
                    SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();

                    // Set the SSL parameters to be used for the SSL connections.
                    params.setSSLParameters(defaultSSLParameters);

                } catch (Exception ex) {
                    // Print the stack trace of the exception to the console.
                    ex.printStackTrace();

                    // Log that there was a failure in creating the HTTPS server.
                    System.out.println("Failed to create HTTPS server");
                }
            }
        });

        // Return the configured HTTPS server instance.
        return server;
    }

    private void bindContexts(com.sun.net.httpserver.HttpServer server) {
        server.createContext("/login", new LoginHandler(userService));
        server.createContext("/api/good/total-cost", new TotalSumHandler(goodService));
        server.createContext("/api/good", new GoodHandler(goodService));
        server.createContext("/api/group", new GroupHandler(groupService));
    }

    private void initializeServices()  {
        Connection dbConnection = establishDbConnection();
        groupService = new GroupService(dbConnection);
        try {
            groupService.createGroup(new Group("group", "description"));
        } catch (GroupAlreadyExistsException ignored) {}

        goodService = new GoodService(dbConnection);

        userService = new UserService(dbConnection);
        try {
            userService.createUser(new User("lillydaystar@gmail.com", "ThomasTheBest"));
        } catch (UserAlreadyExistsException ignored) {}
    }

    private Connection establishDbConnection() {
        DatabaseInitializationService databaseInitializationService = new DatabaseInitializationService();
        return databaseInitializationService.createConnection(DATABASE);
    }

    public static boolean isTokenValid(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
            return false;

        String token = authorizationHeader.substring(7); // Extract token without "Bearer " prefix

        try {
            Claims claims = Jwts.parser().setSigningKey(Server.SECRET_KEY)
                    .parseClaimsJws(token).getBody();
            String login = claims.getSubject();
            Date expirationDate = claims.getExpiration();
            if (expirationDate == null)
                return false;
            Date currentDate = new Date();
            return login != null && userService.getUser(login) != null
                    && currentDate.before(expirationDate);
        } catch (JwtException e) {
            System.out.println("Error validating token: " + e.getMessage());
            return false;
        }
    }
}
