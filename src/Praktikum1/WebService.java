package Praktikum1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class WebService implements IWebService {
    public Semaphore _requestSemaphore;
    private final ServerSocket _serverSocket;
    private boolean _serviceRunning = true;

    public WebService(int port, int maxThreads) throws IOException {
        _serverSocket = new ServerSocket(port);
        _requestSemaphore = new Semaphore(maxThreads);
    }

    @java.lang.Override
    public void Run() {
        while (_serviceRunning) {
            try {
                _requestSemaphore.acquire();
                Socket clientSocket = _serverSocket.accept();

                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String httpRequestRaw = "";
                String line;
                while (true) {
                    line = readFromClient(in);
                    httpRequestRaw += line;
                }
                //System.out.println(httpRequestRaw);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    private String readFromClient(BufferedReader reader) throws IOException {
        return reader.readLine();
    }
}
