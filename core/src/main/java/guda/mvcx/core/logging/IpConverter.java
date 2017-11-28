package guda.mvcx.core.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IpConverter extends ClassicConverter {

    private static final String CLIENT_IP_KEY = "vertx.logger-client-ip";
    private final String clientIp;

    public IpConverter() {
        if (System.getProperty(CLIENT_IP_KEY) != null) {
            this.clientIp = System.getProperty(CLIENT_IP_KEY);
        } else {
            this.clientIp = getClientIp();
        }
    }

    @Override
    public String convert(ILoggingEvent event) {
        return clientIp;
    }

    private String getClientIp() {
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = netInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.isSiteLocalAddress() && !address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (SocketException ignored) {

        }

        return null;
    }
}
