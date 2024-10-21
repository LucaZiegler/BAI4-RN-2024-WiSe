package Praktikum1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class WebService implements IWebService {
    private final ServerSocket _serverSocket;

    public WebService(int port) throws IOException {
        _serverSocket = new ServerSocket(port);
    }

    @java.lang.Override
    public void Run() {
        while (true) {
            try {
                Socket clientSocket = _serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}
