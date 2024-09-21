import socket
from dataclasses import dataclass
from zeroconf import ServiceInfo, Zeroconf


# =====================================================
#                  Service Configuration
# =====================================================

@dataclass
class ServiceConfig:
    name: str
    port: int
    ip: str
    url: str

    def __post_init__(self):
        # Ensure the URL is correctly formatted with a single .local suffix
        self.url = f"http://{self.name}:{self.port}"


# =====================================================
#                  Utility Functions
# =====================================================

# Function to get the local IP address
def get_local_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(('10.254.254.254', 1))  # Use a dummy IP and port to get the local IP
        ip = s.getsockname()[0]
    except Exception:
        ip = '127.0.0.1'
    finally:
        s.close()
    return ip


# =====================================================
#                  mDNS Service Registration
# =====================================================

# Function to register an mDNS service
def register_service(name, port, service_type="_http._tcp.local.", description=None):
    local_ip = get_local_ip()
    desc = description or {'path': '/'}
    service_info = ServiceInfo(
        service_type,
        f"{name}.{service_type}",  # Keeps the service type correct with .local
        addresses=[socket.inet_aton(local_ip)],
        port=port,
        properties=desc,
        server=f"{name}.local."  # Corrected to avoid redundant .local and keep it as a standard mDNS name
    )
    zeroconf = Zeroconf()
    zeroconf.register_service(service_info)
    print(f"Registered service: {name} on port {port} with type {service_type}")
    return zeroconf, service_info


# =====================================================
#                  Service Initialization
# =====================================================

# Function to initialize and register mDNS services
def initialize_services(configurations):
    # Initialize a list to hold Zeroconf service objects
    zeroconf_services = []

    # Register each service found in the configurations
    for config in configurations:
        zeroconf, service_info = register_service(name=config.name, port=config.port)
        zeroconf_services.append((zeroconf, service_info))
        # Display the initialized service with the correct URL
        print(f"Initialized service: {config.url}")

    return zeroconf_services
