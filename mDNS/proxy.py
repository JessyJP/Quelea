from http.server import BaseHTTPRequestHandler, HTTPServer
from datetime import datetime

from mdns_core import get_local_ip

# Define a dictionary that maps service names to their backend ports
service_ports = {
    "lyricsq2.local": 1111,  # Map friendly service name to port 1111
    "remoteq2.local": 1112  # Map friendly service name to port 1112
}


# A simple HTTP request handler that forwards requests to the correct backend port
class ProxyHTTPRequestHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        # Extract the host from the request headers
        host = self.headers.get('Host').split(':')[0]  # Get hostname without port
        target_port = service_ports.get(host)
        local_ip = get_local_ip()

        # Log request details
        self.log_request_details(host, self.path)

        if target_port:
            # Forward the request to the appropriate backend service
            redirect_url = f'http://{local_ip}:{target_port}{self.path}'
            self.send_response(302)  # HTTP 302 Found, used for redirection
            self.send_header('Location', redirect_url)
            self.end_headers()
            self.log_redirection(host, redirect_url)
        else:
            # If the service name isn't recognized, return a 404 error
            self.send_error(404, "Service not found")
            self.log_error(f"Service not found for {host}")

    def log_request_details(self, host, path):
        """Logs detailed request information."""
        print(f"[{datetime.now()}] Incoming request:")
        print(f"  Host: {host}")
        print(f"  Path: {path}")
        print(f"  Full URL: http://{host}{path}")

    def log_redirection(self, host, redirect_url):
        """Logs the redirection details."""
        print(f"[{datetime.now()}] Redirecting {host} to {redirect_url}")

    def log_error(self, message):
        """Logs error messages."""
        print(f"[{datetime.now()}] ERROR: {message}")


# Function to log initial mappings of URLs to backend services
def log_initial_mappings():
    local_ip = get_local_ip()
    print("Initializing Proxy Server with the following mappings:")
    for service, port in service_ports.items():
        print(f"  http://{service} -> http://{local_ip}:{port}")


# Function to start the proxy server
def run_proxy_server():
    # Log the initial mappings
    log_initial_mappings()

    # Start the proxy server on port 80
    server_address = ('', 80)  # Listen on all available interfaces on port 80
    httpd = HTTPServer(server_address, ProxyHTTPRequestHandler)
    print("Proxy server running on port 80. Press Ctrl+C to stop.")
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("Stopping proxy server...")
        httpd.server_close()


if __name__ == "__main__":
    run_proxy_server()
