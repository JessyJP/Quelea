param (
    [string]$Action = "register" # Default action is "register"
)

# Define services and their respective IPs and ports
$services = @(
    @{ Name = "mobile_lyrics"; Port = "1111"; IPAddress = "192.168.0.12" },
    @{ Name = "remote_control"; Port = "1112"; IPAddress = "192.168.0.12" }
)

# Function to register the DNS rules for all services
function Register-Services {
    foreach ($service in $services) {
        $domain = "$($service.Name).local"
        $port = $service.Port
        $ipAddress = $service.IPAddress
        
        Write-Host "Registering $($service.Name) on $domain to ${ipAddress}:${port}"
        
        # Add a DNS rule to map the service's .local domain to its IP address
        Add-DnsClientNrptRule -Namespace "$domain" -NameServers $ipAddress
        
        # Set up a firewall rule to allow traffic on the port (optional, remove if unnecessary)
        New-NetFirewallRule -DisplayName "$($service.Name) Port $port" -Direction Inbound -LocalPort $port -Protocol TCP -Action Allow
        
        Write-Host "$($service.Name) registered and port $port opened."
    }
}

# Function to deregister the DNS rules for all services
function Deregister-Services {
    foreach ($service in $services) {
        $domain = "$($service.Name).local"
        $port = $service.Port
        
        Write-Host "Deregistering $($service.Name) from $domain"
        
        # Remove the DNS rule
        Remove-DnsClientNrptRule -Namespace "$domain"
        
        # Remove the firewall rule (optional, remove if unnecessary)
        Remove-NetFirewallRule -DisplayName "$($service.Name) Port $port"
        
        Write-Host "$($service.Name) deregistered and port $port closed."
    }
}

# Perform the action for both services
if ($Action -eq "register") {
    Register-Services
} elseif ($Action -eq "deregister") {
    Deregister-Services
} else {
    Write-Host "Invalid action. Use 'register' or 'deregister'."
}
