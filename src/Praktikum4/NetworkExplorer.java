package Praktikum4;

import java.net.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class NetworkExplorer {
    public void showNetwork() throws SocketException, UnknownHostException {

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

    public NetworkInterface getDefaultNetworkInterface() throws UnknownHostException, SocketException {
        InetAddress myAddr = InetAddress.getLocalHost();
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface networkInterface = en.nextElement();
            Enumeration<InetAddress> inAddrs = networkInterface.getInetAddresses();
            while (inAddrs.hasMoreElements()) {
                InetAddress inAddr = inAddrs.nextElement();
                if (inAddr.equals(myAddr)) {
                    return networkInterface;
                }
            }
        }
        return null;
    }

    public String byteArraytoHexString(byte[] byteArray) {
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
