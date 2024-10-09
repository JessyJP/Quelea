package org.quelea.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleMDNSTest {

    public static void main(String[] args) {
        // Run the server and mDNS registration in a new thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(SimpleMDNSTest::startServer);
    }

    public static void startServer() {
        try {
            int port = 23454;  // Localhost HTTP server port
            int redirectPort = 80;  // mDNS service port

            // Create the main HTTP server on port 23454
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String response = "This is an mDNS test";
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            });
            server.setExecutor(null); // creates a default executor
            server.start();
            System.out.println("Server started at http://localhost:" + port);

            // Create another HTTP server on port 80 that redirects to port 23454
            HttpServer redirectServer = HttpServer.create(new InetSocketAddress(redirectPort), 0);
            redirectServer.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String redirectUrl = "http://localhost:" + port;
                    String response = "<html><body>Redirecting to <a href='" + redirectUrl + "'>" + redirectUrl + "</a></body></html>";
                    exchange.getResponseHeaders().set("Location", redirectUrl);
                    exchange.sendResponseHeaders(302, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            });
            redirectServer.setExecutor(null);
            redirectServer.start();
            System.out.println("Redirect server started at http://mdns-test.local:" + redirectPort);

            // Register the mDNS service
            InetAddress localHost = InetAddress.getLocalHost();
            JmDNS jmdns = JmDNS.create(localHost);
            ServiceInfo serviceInfo = ServiceInfo.create("_http._tcp.local.", "mdns-test", redirectPort, "test mDNS service");
            jmdns.registerService(serviceInfo);

            // Diagnostic messages for URLs
            String mdnsURL = "http://mdns-test.local:" + redirectPort;
            System.out.println("mDNS service registered with name 'mdns-test'");
            System.out.println("Access the server at:");
            System.out.println("Localhost: http://localhost:" + port);
            System.out.println("mDNS: " + mdnsURL);

            // Keep the server running
            System.out.println("Press Ctrl+C to stop the server");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
