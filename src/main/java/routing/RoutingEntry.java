package routing;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.Socket;
import java.util.Objects;


@Data
@AllArgsConstructor
public class RoutingEntry {

    private String ip;
    private int port;
    private String name;
    private int hopCount;
    private transient Socket socket;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoutingEntry)) return false;
        RoutingEntry entry = (RoutingEntry) o;
        return port == entry.port &&
                ip.equals(entry.ip) &&
                name.equals(entry.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port, name);
    }
}
