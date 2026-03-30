package net.neoforged.neoforge.network;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Utility for detecting IPv6 dual-stack support.
 */
public class DualStackUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Boolean ipv6Cached = null;

    private DualStackUtils() {}

    /**
     * Check if the system has IPv6 support by scanning network interfaces.
     * Result is cached after first call.
     */
    public static boolean checkIPv6() {
        if (ipv6Cached != null) return ipv6Cached;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    if (addresses.nextElement() instanceof Inet6Address) {
                        ipv6Cached = true;
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("[ReForged] IPv6 check failed: {}", e.getMessage());
        }
        ipv6Cached = false;
        return false;
    }
}
