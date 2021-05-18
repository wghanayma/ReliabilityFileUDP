import java.util.InvalidPropertiesFormatException;

public class ClientInfo {
     private String ipAddress;
    private long port;
    private String ipRemote;
    private final long portRemote;
    private boolean isIP = false;
    private boolean IPServer = false;

    public ClientInfo( String localIP, long localPort, String ipRemote, long portRemote) throws InvalidPropertiesFormatException {
        Pair<Boolean, String> isIpLocalValid = validIPOrHostname(localIP);
        Pair<Boolean, String> isIpRemoteValid = validIPOrHostname(ipRemote);
        if (isIpLocalValid.getKey()) {
            if (isIpRemoteValid.getValue().equals("IP")) {
                this.isIP = true;
            }
            this.ipAddress = localIP;
        } else {
            throw new InvalidPropertiesFormatException(ipAddress + " is Invalid");
        }
        if (isIpRemoteValid.getKey()) {
            if (isIpRemoteValid.getValue().equals("IP")) {
                this.IPServer = true;
            }
            this.ipRemote = ipRemote;
        } else {
            throw new InvalidPropertiesFormatException(ipRemote + " is Invalid");
        }
        if (validPort(localPort)) {
            this.port = localPort;
        } else {
            throw new InvalidPropertiesFormatException(port + " is Invalid");
        }
        if (validPort(portRemote)) {
            this.portRemote = portRemote;
        } else {
            throw new InvalidPropertiesFormatException(portRemote + " is Invalid");
        }
     }

    public ClientInfo(String ipServer, long portServer) throws InvalidPropertiesFormatException {
        Pair<Boolean, String> isIpServerValid = validIPOrHostname(ipServer);
        if (isIpServerValid.getKey()) {
            if (isIpServerValid.getValue().equals("IP")) {
                this.IPServer = true;
            }
            this.ipRemote = ipServer;
        } else {
            throw new InvalidPropertiesFormatException(ipServer + " is Invalid");
        }
        if (validPort(portServer)) {
            this.portRemote = portServer;
        } else {
            throw new InvalidPropertiesFormatException(port + " is Invalid");
        }
    }

    public Pair<Boolean, String> validIPOrHostname(String ip) {
        String validIPAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}" +
                "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
        if (ip.matches(validIPAddressRegex)) {
            return new Pair<>(true, "IP");
        }
        return new Pair<>(false, "");
    }

    public boolean validPort(long port) {
        return (port > 1024 && port <= Math.pow(2, 16));
    }

    public long getPort() {
        return port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public long getPortRemote() {
        return portRemote;
    }

    public String getIpAddressRemote() {
        return ipRemote;
    }

 }
