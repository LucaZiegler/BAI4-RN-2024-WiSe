package Praktikum4;

import java.net.SocketException;

public class Main {
    public static void main(String[] args) throws SocketException {
        // look for your IPv6-Address in the results and use the corresponding Scope ID (Interface ID) as args[0]
        // and Hardware (LAN) Address as args[1] in DHCPv6Explorer.main()
        NetworkExplorer networkExplorer = new NetworkExplorer();
        networkExplorer.showNetwork();
    }
}
