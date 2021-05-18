import java.util.List;

public class Client {
    private String ip;
    private String username;
    private long port;

    public Client(String ip, String username, long port) {
        this.ip = ip;
        this.port = port;
        this.username = username;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getPort() {
        return port;
    }

    public void setPort(long port) {
        this.port = port;
    }

    public boolean equal(Client client) {
        return this.username.equals(client.username) && this.port == client.port;
    }

    public static int clientExist(List<Client> list, Client client) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equal(client)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return username + " - " + ip + " - " + port;
    }

}
