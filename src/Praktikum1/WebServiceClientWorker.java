package Praktikum1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class WebServiceClientWorker extends Thread {
    private Socket _clientSocket;

    public WebServiceClientWorker(Socket clientSocket) {
        _clientSocket = clientSocket;
    }

    public void run() {
        try {
            DataOutputStream out = new DataOutputStream(_clientSocket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(_clientSocket.getInputStream()));

            String httpRequestRaw = "";
            String line;
            while (!(line = readFromClient(in)).isEmpty()) {
                httpRequestRaw += line + "\n";
            }
            System.out.println(httpRequestRaw);
        } catch (Exception ex) {

        }finally {
            try {
                _clientSocket.close();
            } catch (IOException e) {
                // Ignored
            }
        }
    }

    private String readFromClient(BufferedReader reader) throws IOException {
        return reader.readLine();
    }
}
