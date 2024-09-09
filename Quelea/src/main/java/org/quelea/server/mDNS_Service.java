package org.quelea.server;

import org.quelea.services.utils.QueleaProperties;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
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
     * @param concurrent  If true, starts the service in a separate thread and performs self-test.
     */
    public void startService(String serviceName, int port, boolean concurrent) {
        if (concurrent) {
            Thread serviceThread = new Thread(() -> {
                startServiceInternal(serviceName, port);
                selfTest(serviceName);
            });
            serviceThread.start();
        } else {
            startServiceInternal(serviceName, port);
        }
    }

    /**
     * Internal method to start the service.
     */
    private void startServiceInternal(String serviceName, int port) {
        try {
            if (jmdns == null) {
                String ipAddress = getIP();
                if (ipAddress == null){ return;}
                jmdns = JmDNS.create(ipAddress);  // Ensure it uses the correct IPv4 address
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

            LOGGER.log(Level.INFO, "Registered mDNS service: {0} on port {1}", new Object[]{serviceName, port});
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to start mDNS service for " + serviceName, e);
        }
    }


    /**
     * Perform a self-test to verify that the mDNS service has been properly registered and is discoverable.
     *
     * @param serviceName The name of the service.
     */
    private void selfTest(String serviceName) {
        try {
            Thread.sleep(1000); // Sleep for 1 second to allow registration
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Self-test delay interrupted", e);
        }

        ServiceInfo serviceInfo = registeredServices.get(serviceName);
        if (serviceInfo != null) {
            String testUrl = getServiceURL(serviceName);
            boolean serviceAvailable = resolveService(testUrl);

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
     * Simulate resolving the service by checking if the URL responds.
     *
     * @param url The URL to resolve.
     * @return True if the service is discoverable, false otherwise.
     */
    private boolean resolveService(String url) {
        for (ServiceInfo info : registeredServices.values()) {
            String resolvedUrl = getServiceURL(info.getName());
            if (resolvedUrl != null && resolvedUrl.equals(url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Start multiple mDNS services (for mobile lyrics and remote control), either concurrently or sequentially.
     *
     * @param concurrent If true, starts services in separate threads and performs self-tests.
     */
    public void startAll(boolean concurrent) {
        startService("mobile_lyrics", QueleaProperties.get().getMobLyricsPort(), concurrent);
        startService("remote_control", QueleaProperties.get().getRemoteControlPort(), concurrent);
    }

    /**
     * Overloaded version of startAll that defaults to concurrent = false.
     */
    public void startAll() {
        startAll(false);  // Default to sequential (concurrent = false)
    }

    /**
     * Stop all running mDNS services.
     */
    public void stopAll() {
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
            return "http://" + serviceInfo.getName() + ".local:" + serviceInfo.getPort();
        }
        LOGGER.log(Level.WARNING, "Service not found: {0}", serviceName);
        return null;
    }


}
