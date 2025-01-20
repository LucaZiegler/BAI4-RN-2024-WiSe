package Praktikum4;

import java.io.IOException;
import java.net.*;

public class DHCPv6Explorer { // Port 546
    // Folie 38; ff02::1:2 https://datatracker.ietf.org/doc/html/rfc8415#section-7
    private final static String All_DHCP_RELAY_AGENTS_AND_SERVERS = "ff020000000000000000000000010002";
    private InetAddress _inetAddress;
    private final String _hardwareAddress;

    public DHCPv6Explorer(int interfaceId, String hardwareAddress) throws UnknownHostException {
        _inetAddress = Inet6Address.getByAddress("", hexStringtoByteArray(All_DHCP_RELAY_AGENTS_AND_SERVERS), interfaceId);

        this._hardwareAddress = hardwareAddress;
    }

    public void request() throws IOException {
        StringBuilder message = new StringBuilder();
        message.append("01"); // msg-type: SOLICIT (1): A client sends a Solicit message to locate servers
        message.append("000000"); // transaction-id: The transaction ID for this message exchange. A 3-octet field
        // Client Identifier Option https://datatracker.ietf.org/doc/html/rfc8415#section-21.2
        message.append("0001"); // option-code: OPTION_CLIENTID (1)
        // division by 2 to find out the bytes, + 2 for type code, + 2 for hardware type
        message.append(String.format("%04x", _hardwareAddress.length() / 2 + 2 + 2)); // option-len: Length of DUID in octets.
        // DHCP Unique Identifier (DUID)
        message.append("0003"); // DUID type code: consists of a 2-octet type code in network byte order (big-endian)
        message.append("0006"); // hardware type (16 bits): WLAN: 6
        message.append(_hardwareAddress);

        String solicitHex = message.toString();
        byte[] solicitData = hexStringtoByteArray(solicitHex);

        //TODO: send the request
        try (DatagramSocket clientSocket = new DatagramSocket(null)) {
            // Wir binden explizit an IPv6-unspecified "::" mit Port 546
            SocketAddress bindAddr = new InetSocketAddress("::", 546);
            clientSocket.bind(bindAddr);

            // Optional: Timeout, damit receive() nicht ewig blockiert
            clientSocket.setSoTimeout(5000);

            // 4) Zieladresse = ff02::1:2 mit passender Scope-ID.
            //    Passen Sie ggf. "%eth0" oder "%4" etc. an Ihr System an!
            InetAddress multicastAddress = _inetAddress;
            int serverPort = 547;  // DHCPv6-Server-Port

            // 5) UDP-Paket erstellen und senden
            DatagramPacket sendPacket = new DatagramPacket(
                    solicitData,
                    solicitData.length,
                    multicastAddress,
                    serverPort
            );

            System.out.println("[Sende Solicit] -> " + multicastAddress + ":" + serverPort);
            System.out.println("Hex-Dump Solicit:\n " + solicitHex);

            clientSocket.send(sendPacket);

            // 6) Antwort (Advertise) empfangen
            byte[] buf = new byte[1024];  // Puffer für eintreffendes Paket
            DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

            System.out.println("Warte auf Advertise-Antwort...");
            clientSocket.receive(receivePacket);

            // 7) Ausgabe der Antwort
            byte[] respData = new byte[receivePacket.getLength()];
            System.arraycopy(buf, 0, respData, 0, receivePacket.getLength());

            String respHex = /*ServiceCode.byteArraytoHexString(respData)*/"-";

            System.out.println("[Empfangenes Advertise] von " +
                    receivePacket.getAddress() + ":" +
                    receivePacket.getPort());
            System.out.println("Hex-Dump Advertise:\n " + respHex);

        } catch (BindException be) {
            System.err.println("Bind-Fehler: Port 546 wird evtl. bereits benutzt oder erfordert Administrator-Rechte.");
            be.printStackTrace();
        } catch (SocketTimeoutException ste) {
            System.err.println("Zeitüberschreitung beim Warten auf Antwort (Timeout).");
        } catch (IOException e) {
            e.printStackTrace();
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

}
