package com.kong.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kong
 */
@Slf4j
public class IpUtil {

    private IpUtil() {}

    public static String currHostIp() {
        InetAddress address = null;
        try{
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.error("error when getting host ip...", e);
        }
        if (address != null) {
            return address.getHostAddress();
        }
        return null;
    }
}
