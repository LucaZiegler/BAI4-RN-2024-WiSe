package Praktikum4;

import java.net.Inet6Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) throws SocketException, UnknownHostException {
        // look for your IPv6-Address in the results and use the corresponding Scope ID (Interface ID) as args[0]
        // and Hardware (LAN) Address as args[1] in DHCPv6Explorer.main()
        NetworkExplorer networkExplorer = new NetworkExplorer();
        NetworkInterface defInterface = networkExplorer.getDefaultNetworkInterface();

        String interfaceHardwareId = networkExplorer.byteArraytoHexString(defInterface.getHardwareAddress());
        DHCPv6Explorer explorer = new DHCPv6Explorer(defInterface.getIndex(), interfaceHardwareId);
    }
}
