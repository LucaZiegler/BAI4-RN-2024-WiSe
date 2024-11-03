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
            String userAgent = null;
            while (!(line = readFromClient(in)).isEmpty()) {
                httpRequestRaw += line + "\n";
                if (line.startsWith("User-Agent")) {
                    userAgent = line;
                }
            }
            if (httpRequestRaw.equals("")) {
                System.out.println("HTTP/1.0 400 Bad Request");
            }

            //else if (!hasAuthorizationHeader(in)) {
            //      System.out.println("HTTP/1.0 401 Unauthorized - Authorization required.");
            //}
            else if (httpRequestRaw.contains("/404.html")) {
                System.out.println("HTTP/1.0 404 Not Found");
            }

            else if (userAgent != null && userAgent.contains("Firefox")) {
                System.out.println("HTTP/1.0 200 OK\r\n");
                System.out.println("Content-Type: text/plain\r\n");
                System.out.println(("Content-Length: " + httpRequestRaw.length() + "\r\n"));
                System.out.println("\r\n");
            } else {
                System.out.println("HTTP/1.0 406 Not Acceptable\r\n");
                System.out.println("Content-Type: text/plain\r\n");
                System.out.println("Content-Length: 0\r\n");
                System.out.println("\r\n");
            }

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
