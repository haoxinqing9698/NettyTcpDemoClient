package net.itfeng.nettytcpdemoclient.util;

import lombok.extern.slf4j.Slf4j;

import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * 基于MAC地址生成唯一client_id
 *
 * @author fengxubo
 * @since 2024/1/9 11:36
 * 
 */
@Slf4j
public class ClientIdUtil {

    public static String getClientId() {
        String mac = getMacAddress(); // 你的MAC地址。
        if (mac.isEmpty()) {
            log.error("获取MAC地址失败");
            return "";
        }
        return mac;
    }

    private static String getMacAddress() {
        String mac = "";
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                if (networkInterface.isUp() && !networkInterface.isVirtual() && !networkInterface.isLoopback() &&
                        (networkInterface.getName().equals("eth0") || networkInterface.getName().equals("eth1") || networkInterface.getName().equals("en0"))) {
                    byte[] macBytes = networkInterface.getHardwareAddress();

                    if (macBytes != null) {
                        StringBuilder macAddress = new StringBuilder();
                        for (byte b : macBytes) {
                            macAddress.append(String.format("%02X:", b));
                        }

                        if (!macAddress.isEmpty()) {
                            macAddress.deleteCharAt(macAddress.length() - 1); // 移除末尾的冒号
                        }
                        return macAddress.toString();
                    }
                }
            }

        } catch (Exception e) {
            log.error("获取MAC地址失败", e);
        }

        return mac;
    }
}
