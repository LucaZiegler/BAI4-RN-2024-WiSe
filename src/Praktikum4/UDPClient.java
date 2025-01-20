package Praktikum4;

/*
 * UDPClient.java
 *
 * Version 2.1
 * Vorlesung Rechnernetze HAW Hamburg
 * Autor: M. Huebner (nach Kurose/Ross)
 * Zweck: UDP-Client Beispielcode:
 *        UDP-Socket erzeugen, einen vom Benutzer eingegebenen
 *        String in ein UDP-Paket einpacken und an den UDP-Server senden,
 *        den String in Grossbuchstaben empfangen und ausgeben
 *        Nach QUIT beenden, bei SHUTDOWN den Serverthread beenden
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;


public class UDPClient {
    private int PORT = 0;
    public final String HOSTNAME = "localhost";
    public final int BUFFER_SIZE = 1024;
    public final String CHARSET = "IBM-850"; // "UTF-8"

    // UDP-Socketklasse
    private DatagramSocket clientSocket;

    private boolean serviceRequested = true;
    public InetAddress SERVER_IP_ADDRESS;

    public UDPClient(int port) {
        PORT = port;
    }

    /* Client starten. Ende, wenn quit eingegeben wurde */
    public void run() {
        Scanner inFromUser;
        String sentence;
        String modifiedSentence;

        try {
            /* IP-Adresse des Servers ermitteln --> DNS-Client-Aufruf! */
            SERVER_IP_ADDRESS = InetAddress.getByName(HOSTNAME);

            /* UDP-Socket erzeugen (kein Verbindungsaufbau!)
             * Socket wird an irgendeinen freien (Quell-)Port gebunden, da kein Port angegeben */
            clientSocket = new DatagramSocket();


        } catch (IOException e) {
            System.err.println("Connection aborted by server!");
        }

    }

    public void writeToServer(byte[] data) throws IOException {

        /* Paket erzeugen mit Server-IP und Server-Zielport */
        DatagramPacket sendPacket = new DatagramPacket(data, data.length,
                SERVER_IP_ADDRESS, PORT);
        /* Senden des Pakets */
        clientSocket.send(sendPacket);

        System.err.println("UDP Client has sent the message");
    }

    public String readFromServer() throws IOException {
        /* Liefere den naechsten String vom Server */
        String receiveString = "";

        /* Paket fuer den Empfang erzeugen */
        byte[] receiveData = new byte[BUFFER_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, BUFFER_SIZE);

        /* Warte auf Empfang des Antwort-Pakets auf dem eigenen (Quell-)Port,
         * den der Server aus dem Nachrichten-Paket ermittelt hat */
        clientSocket.receive(receivePacket);

        /* Paket wurde empfangen --> auspacken und Inhalt anzeigen */
        receiveString = new String(receivePacket.getData(), 0,
                receivePacket.getLength(), CHARSET);

        System.err.println("UDP Client got from Server: " + receiveString);

        return receiveString;
    }
}