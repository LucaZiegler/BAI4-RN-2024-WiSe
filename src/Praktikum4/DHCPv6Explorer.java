package Praktikum4;

import java.net.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class DHCPv6Explorer {//546
    private final static String All_DHCP_RA_AND_SERVERS = "ff020000000000000000000000010002"; // Folie 38; ff02::1:2 https://datatracker.ietf.org/doc/html/rfc8415#section-7
    private final static int LOCAL_NETWORK_INTERFACE_ID = 22; // Scope-ID

    public static void main(String[] args) throws SocketException, UnknownHostException {
        showNetwork();
        InetAddress inetAddress = Inet6Address.getByAddress("", hexStringtoByteArray(All_DHCP_RA_AND_SERVERS), LOCAL_NETWORK_INTERFACE_ID);
    }

    private static void showNetwork() throws SocketException {
        /* Netzwerk-Infos fuer alle Interfaces ausgeben */
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface ni = en.nextElement();
            System.out.println("\nDisplay Name = " + ni.getDisplayName());
            System.out.println(" Name = " + ni.getName());
            System.out.println(" Scope ID (Interface ID) = " + ni.getIndex());
            System.out.println(" Hardware (LAN) Address = " + byteArraytoHexString(ni.getHardwareAddress()));

            List<InterfaceAddress> list = ni.getInterfaceAddresses();
            Iterator<InterfaceAddress> it = list.iterator();

            while (it.hasNext()) {
                InterfaceAddress ia = it.next();
                System.out
                        .println(" Adress = " + ia.getAddress() + " with Prefix-Length " + ia.getNetworkPrefixLength());
            }
        }
    }

    private static byte[] hexStringtoByteArray(String hex) {
        /* Konvertiere den String mit Hex-Ziffern in ein Byte-Array */
        byte[] val = new byte[hex.length() / 2];
        for (int i = 0; i < val.length; i++) {
            int index = i * 2;
            int num = Integer.parseInt(hex.substring(index, index + 2), 16);
            val[i] = (byte) num;
        }
        return val;
    }

    private static String byteArraytoHexString(byte[] byteArray) {
        /* Konvertiere das Byte-Array in einen String mit Hex-Ziffern */
        StringBuilder hex = new StringBuilder();
        if (byteArray != null) {
            for (int i = 0; i < byteArray.length; ++i) {
                hex.append(String.format("%02X", byteArray[i]));
            }
        }
        return hex.toString();
    }
}
