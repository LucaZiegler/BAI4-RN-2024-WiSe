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

            //String httpRequestRaw = "";
            String line;
            HttpRequestData requestData = new HttpRequestData();
            boolean parseFirstLine = true;
            boolean parseHeaders = false;
            while (!(line = readFromClient(in)).isEmpty()) {
                if (parseFirstLine) {
                    String[] values = line.split(" ");
                    requestData.Method = values[0];
                    requestData.Path = values[1];
                    requestData.Version = values[2];

                    parseFirstLine = false;
                    parseHeaders = true;
                } else if (parseHeaders) {
                    if (line == "") {
                        parseHeaders = false;
                    } else {
                        String[] values = line.split(": ");
                        requestData.Headers.put(values[0], values[1]);
                    }
                } else {
                    if (requestData.Content == null)
                        requestData.Content = "";
                    requestData.Content += line + "\n";
                }
            }
            onHttpRequestReceived(requestData, out);
            _clientSocket.close();
        } catch (Exception ex) {

        } finally {
            try {
                _clientSocket.close();
            } catch (IOException e) {
                // Ignored
            }
        }
    }

    private void onRawHttpRequestReceived(String httpRequestRaw, DataOutputStream out) throws IOException {


        String method = httpRequestRaw.substring(0, httpRequestRaw.indexOf(" "));
        method = method.toUpperCase();
        String path = httpRequestRaw.substring(httpRequestRaw.indexOf("/"));
        path = path.substring(0, path.indexOf(" "));

        HttpRequestData requestData = new HttpRequestData();
        requestData.Method = method;
        requestData.Path = path;
        requestData.Content = null;

        if (method == "POST") {

        }

        onHttpRequestReceived(requestData, out);
    }

    private void onHttpRequestReceived(HttpRequestData requestData, DataOutputStream out) throws IOException {
        writeStatusCode(out, 200, "OK");
        writeHeader(out, "Content-Type", "text/plain");
        out.writeBytes("\n");

        out.writeBytes("Test!");
        out.flush();
        out.close();
    }

    private void writeStatusCode(DataOutputStream out, int statusCode, String statusMessage) throws IOException {
        out.writeBytes("HTTP/1.0 " + statusCode + " " + statusMessage + "\n");
    }

    private void writeHeader(DataOutputStream out, String key, String value) throws IOException {
        out.writeBytes(key + ": " + value + "\n");
    }

    private String readFromClient(BufferedReader reader) throws IOException {
        return reader.readLine();
    }
}
