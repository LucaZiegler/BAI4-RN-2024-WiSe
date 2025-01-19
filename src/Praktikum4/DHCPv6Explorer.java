package Praktikum4;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DHCPv6Explorer { // Port 546
    // Folie 38; ff02::1:2 https://datatracker.ietf.org/doc/html/rfc8415#section-7
    private final static String All_DHCP_RELAY_AGENTS_AND_SERVERS = "ff020000000000000000000000010002";

    public static void main(String[] args) throws UnknownHostException {
        final int LOCAL_NETWORK_INTERFACE_ID = Integer.parseInt(args[0]); // Scope ID (Interface ID)
        final String HARDWARE_ADDRESS = args[1]; // Hardware (LAN) Address
        InetAddress inetAddress = Inet6Address.getByAddress("", hexStringtoByteArray(All_DHCP_RELAY_AGENTS_AND_SERVERS), LOCAL_NETWORK_INTERFACE_ID);
        StringBuilder message = new StringBuilder();
        message.append("01"); // msg-type: SOLICIT (1): A client sends a Solicit message to locate servers
        message.append("000000"); // transaction-id: The transaction ID for this message exchange. A 3-octet field
        // Client Identifier Option https://datatracker.ietf.org/doc/html/rfc8415#section-21.2
        message.append("0001"); // option-code: OPTION_CLIENTID (1)
        // division by 2 to find out the bytes, + 2 for type code, + 2 for hardware type
        message.append(String.format("%04x", HARDWARE_ADDRESS.length() / 2 + 2 + 2)); // option-len: Length of DUID in octets.
        // DHCP Unique Identifier (DUID)
        message.append("0003"); // DUID type code: consists of a 2-octet type code in network byte order (big-endian)
        message.append("0006"); // hardware type (16 bits): WLAN: 6
        message.append(HARDWARE_ADDRESS);
        byte[] solicitRequest = hexStringtoByteArray(message.toString());

        //TODO: send the request
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
