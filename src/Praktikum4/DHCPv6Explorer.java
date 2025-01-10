package Praktikum4;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class DHCPv6Explorer { //Port 546
    private final static String All_DHCP_RA_AND_SERVERS = "ff020000000000000000000000010002"; // Folie 38; ff02::1:2 https://datatracker.ietf.org/doc/html/rfc8415#section-7

    public static void main(String[] args) throws UnknownHostException {
        final int LOCAL_NETWORK_INTERFACE_ID = Integer.parseInt(args[0]); // Scope-ID
        InetAddress inetAddress = Inet6Address.getByAddress("", hexStringtoByteArray(All_DHCP_RA_AND_SERVERS), LOCAL_NETWORK_INTERFACE_ID);
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

}
