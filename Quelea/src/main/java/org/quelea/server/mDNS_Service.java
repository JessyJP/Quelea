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
     * @param concurrent  If true, starts the service in a separate thread and performs self-test.
     */
    public void startService(String serviceName, int port, boolean concurrent) {
        LOGGER.log(Level.INFO, "Attempting to start mDNS service: {0} on port {1} (concurrent: {2})",
                new Object[]{serviceName, port, concurrent});

        if (concurrent) {
            Thread serviceThread = new Thread(() -> {
                try {
                    startServiceInternal(serviceName, port);
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
        startAll(false);  // Default to sequential (concurrent = false)
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


}
