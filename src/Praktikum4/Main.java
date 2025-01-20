package Praktikum4;

import java.io.IOException;
import java.net.NetworkInterface;

public class Main {
    public static void main(String[] args) throws IOException {
        // look for your IPv6-Address in the results and use the corresponding Scope ID (Interface ID) as args[0]
        // and Hardware (LAN) Address as args[1] in DHCPv6Explorer.main()
        NetworkExplorer networkExplorer = new NetworkExplorer();
        NetworkInterface defInterface = networkExplorer.getDefaultNetworkInterface();

        String interfaceHardwareId = networkExplorer.byteArraytoHexString(defInterface.getHardwareAddress());
        DHCPv6Explorer explorer = new DHCPv6Explorer(defInterface.getIndex(), interfaceHardwareId);
        explorer.request();
    }
}
