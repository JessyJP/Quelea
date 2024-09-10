package org.quelea.server;

import org.quelea.services.utils.QueleaProperties;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.quelea.windows.options.customprefs.MobileServerPreference.getIP;

/**
 * mDNS service to advertise Quelea services (e.g., mobile lyrics, remote control) via Bonjour/mDNS.
 */
public class mDNS_Service {

    private static final Logger LOGGER = Logger.getLogger(mDNS_Service.class.getName());
    private JmDNS jmdns;
    private final Map<String, ServiceInfo> registeredServices = new HashMap<>();

    /**
     * Start the mDNS service and register a service with a specific service name.
     *
     * @param serviceName The name of the service to advertise.
     * @param port        The port that the service runs on.
     * @param concurrent  If true, starts the service in a separate thread and performs self-test after 30 seconds.
     */
    public void startService(String serviceName, int port, boolean concurrent) {
        LOGGER.log(Level.INFO, "Attempting to start mDNS service: {0} on port {1} (concurrent: {2})",
                new Object[]{serviceName, port, concurrent});

        if (concurrent) {
            Thread serviceThread = new Thread(() -> {
                try {
                    startServiceInternal(serviceName, port);
                    Thread.sleep(30000);  // Wait for 30 seconds before running the test
                    selfTest(serviceName, port);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error starting mDNS service: " + serviceName, e);
                }
            });
            serviceThread.start();
        } else {
            startServiceInternal(serviceName, port);
            selfTest(serviceName, port);
        }
    }

    /**
     * Internal method to start the service.
     */
    private void startServiceInternal(String serviceName, int port) {
        try {
            LOGGER.log(Level.INFO, "Starting internal service for {0} on port {1}", new Object[]{serviceName, port});

            if (jmdns == null) {
                String ipAddress = getIP();
                LOGGER.log(Level.INFO, "Obtained IP Address: {0}", ipAddress);

                if (ipAddress == null) {
                    LOGGER.log(Level.SEVERE, "No valid IP address found for mDNS service.");
                    return;
                }
                jmdns = JmDNS.create(ipAddress);  // Ensure it uses the correct IPv4 address
                LOGGER.log(Level.INFO, "JmDNS instance created using IP: {0}", ipAddress);
            }

            // Register the mDNS service with the correct service type (_http._tcp) and domain (.local)
            String fullServiceName = serviceName + "._http._tcp.local.";

            // Adding the actual IP and port in the properties map
            Map<String, String> properties = new HashMap<>();
            properties.put("address", getIP());  // IP from getIP method
            properties.put("port", String.valueOf(port));  // Dynamically passed port

            // Register service with the serviceName and properties
            ServiceInfo serviceInfo = ServiceInfo.create(fullServiceName, serviceName, port, 0, 0, properties);
            jmdns.registerService(serviceInfo);
            registeredServices.put(serviceName, serviceInfo);

            LOGGER.log(Level.INFO, "Successfully registered mDNS service: {0} on port {1}", new Object[]{serviceName, port});
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to start mDNS service for " + serviceName, e);
        }
    }

    /**
     * Start multiple mDNS services (for mobile lyrics and remote control), either concurrently or sequentially.
     *
     * @param concurrent If true, starts services in separate threads and performs self-tests.
     */
    public void startAll(boolean concurrent) {
        LOGGER.log(Level.INFO, "Starting all mDNS services (concurrent: {0})", concurrent);

        startService("mobile_lyrics", QueleaProperties.get().getMobLyricsPort(), concurrent);
        startService("remote_control", QueleaProperties.get().getRemoteControlPort(), concurrent);
    }

    /**
     * Overloaded version of startAll that defaults to concurrent = false.
     */
    public void startAll() {
        startAll(true);  // Default to concurrent = true with a 30 seconds wait before testing
    }

    /**
     * Stop all running mDNS services.
     */
    public void stopAll() {
        LOGGER.log(Level.INFO, "Stopping all mDNS services");

        if (jmdns != null) {
            registeredServices.forEach((serviceName, serviceInfo) -> {
                try {
                    jmdns.unregisterService(serviceInfo);
                    LOGGER.log(Level.INFO, "Unregistered mDNS service: {0}", serviceName);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to unregister mDNS service: " + serviceName, e);
                }
            });
            registeredServices.clear();
            try {
                jmdns.close();
                LOGGER.log(Level.INFO, "JmDNS instance closed successfully.");
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to close JmDNS", e);
            }
        }
    }

    /**
     * Get the mDNS alias (service name) URL for a specific service.
     *
     * @param serviceName The name of the service.
     * @return The formatted URL (e.g., "http://mobile_lyrics.local:port").
     */
    public String getServiceURL(String serviceName) {
        ServiceInfo serviceInfo = registeredServices.get(serviceName);
        if (serviceInfo != null) {
            // Return the alias (service name) in the URL
            String url = "http://" + serviceInfo.getName() + ".local:" + serviceInfo.getPort();
            LOGGER.log(Level.INFO, "Generated service URL for {0}: {1}", new Object[]{serviceName, url});
            return url;
        }
        LOGGER.log(Level.WARNING, "Service not found: {0}", serviceName);
        return null;
    }

    // ----- Test methods ------

    /**
     * Perform a self-test to verify that the mDNS service has been properly registered and is discoverable.
     *
     * @param serviceName The name of the service.
     * @param port        The port the service runs on.
     */
    private void selfTest(String serviceName, int port) {
        LOGGER.log(Level.INFO, "Starting self-test for service: {0}", serviceName);

        try {
            Thread.sleep(1000); // Sleep for 1 second to allow registration
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Self-test delay interrupted", e);
        }

        ServiceInfo serviceInfo = registeredServices.get(serviceName);
        if (serviceInfo != null) {
            String testUrl = getServiceURL(serviceName);
            LOGGER.log(Level.INFO, "Self-test for service {0}, testing URL: {1}", new Object[]{serviceName, testUrl});

            boolean serviceAvailable = resolveService(serviceName, port);

            if (serviceAvailable) {
                LOGGER.log(Level.INFO, "Self-test passed: mDNS service {0} is available at {1}",
                        new Object[]{serviceName, testUrl});
            } else {
                LOGGER.log(Level.SEVERE, "Self-test failed: mDNS service {0} is NOT available at {1}",
                        new Object[]{serviceName, testUrl});
            }
        } else {
            LOGGER.log(Level.SEVERE, "Self-test failed: mDNS service not found for {0}", serviceName);
        }
    }

    /**
     * Simulate resolving the service by checking if both the URL and IP respond.
     *
     * @param serviceName The service name to resolve.
     * @param port        The port on which the service runs.
     * @return True if the service is discoverable and accessible, false otherwise.
     */
    private boolean resolveService(String serviceName, int port) {
        boolean ipSuccess = false;
        boolean localDomainSuccess = false;

        String ipAddress = getIP();
        if (ipAddress == null || ipAddress.isEmpty()) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve a valid IP address for the service.");
            return false;
        }

        // Test IP-based URL
        String ipBasedUrl = "http://" + ipAddress + ":" + port;
        LOGGER.log(Level.INFO, "Testing connection to service at IP-based URL: {0}", ipBasedUrl);
        ipSuccess = testConnection(ipBasedUrl);

        if (ipSuccess) {
            LOGGER.log(Level.INFO, "Successfully connected to IP-based URL: {0}", ipBasedUrl);
        } else {
            LOGGER.log(Level.WARNING, "Failed to connect to IP-based URL: {0}", ipBasedUrl);
        }

        // Test .local domain URL
        String localDomainUrl = "http://" + serviceName + ".local:" + port;
        LOGGER.log(Level.INFO, "Testing connection to service at .local URL: {0}", localDomainUrl);
        localDomainSuccess = testConnection(localDomainUrl);

        if (localDomainSuccess) {
            LOGGER.log(Level.INFO, "Successfully connected to .local URL: {0}", localDomainUrl);
        } else {
            LOGGER.log(Level.WARNING, "Failed to connect to .local URL: {0}", localDomainUrl);
        }

        // Test port directly
        boolean portAccessible = testPort(ipAddress, port);
        if (portAccessible) {
            LOGGER.log(Level.INFO, "Port {0} is accessible on IP {1}", new Object[]{port, ipAddress});
        } else {
            LOGGER.log(Level.WARNING, "Port {0} is NOT accessible on IP {1}", new Object[]{port, ipAddress});
        }

        // Return true if either URL test or port test is successful
        return (ipSuccess || localDomainSuccess) && portAccessible;
    }

    /**
     * Test the connection to a given URL.
     *
     * @param urlString The URL to test.
     * @return True if the connection is successful, false otherwise.
     */
    private boolean testConnection(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);  // 5 seconds timeout

            int responseCode = connection.getResponseCode();
            return (responseCode >= 200 && responseCode < 400);  // Consider success for 2xx and 3xx response codes
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to URL: " + urlString, e);
            return false;
        }
    }

    /**
     * Test if the specified port is accessible on the given IP address.
     *
     * @param ipAddress The IP address to test.
     * @param port      The port to test.
     * @return True if the port is accessible, false otherwise.
     */
    private boolean testPort(String ipAddress, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ipAddress, port), 5000);  // 5 seconds timeout
            return true;  // Port is accessible
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to connect to port {0} on IP {1}", new Object[]{port, ipAddress});
            return false;
        }
    }

}
