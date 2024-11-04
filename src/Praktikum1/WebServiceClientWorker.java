package Praktikum1;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

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
                        requestData.Headers.put(values[0].toLowerCase(), values[1]);
                        System.out.println(values[0] + ": " + values[1]);
                    }
                } else {
                    if (requestData.Content == null)
                        requestData.Content = "";
                    requestData.Content += line + "\n";
                }
            }
            HttpResponseData responseData = onHttpRequestReceived(requestData, out);

            writeStatusCode(out, responseData.StatusCode, responseData.StatusMessage);
            for (String key : responseData.ResponseHeaders.keySet()) {
                writeHeader(out, key, responseData.ResponseHeaders.get(key));
            }
            if (responseData.ResponseContent != null) {
                int contentLength = responseData.ResponseContent.getBytes(StandardCharsets.UTF_8).length;
                writeHeader(out, "Content-Length", contentLength);
            }
            out.writeBytes("\n");

            if (responseData.ResponseContent != null) {
                out.writeBytes(responseData.ResponseContent);
            }
            out.flush();
            out.close();
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

    private HttpResponseData onRawHttpRequestReceived(String httpRequestRaw, DataOutputStream out) throws IOException {
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

        return onHttpRequestReceived(requestData, out);
    }

    private HttpResponseData onHttpRequestReceived(HttpRequestData requestData, DataOutputStream out) throws IOException {
        HttpResponseData responseData = new HttpResponseData();
        responseData.StatusCode = 400;
        responseData.StatusMessage = "Bad request";

        String userAgent = requestData.Headers.get("user-agent");
        if (userAgent != null && !userAgent.toLowerCase().contains("firefox")) {
            return NotAcceptable();
        }

        Path path = Paths.get(".htusers");
        if (Files.exists(path)) {
            if (!requestData.Headers.containsKey("authorization")) {
                return Unauthorized();
            }
            String[] authData = requestData.Headers.get("authorization").split(" ");
            if (!authData[0].equals("Basic")) {
                return Unauthorized();
            }
            String credentials = new String(Base64.getDecoder().decode(authData[1]));

            try {
                BufferedReader reader = new BufferedReader(new FileReader(".htusers"));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equals(credentials)) {
                        return Ok();
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Unauthorized();
        }

        return responseData;
    }

    private HttpResponseData Ok() {
        HttpResponseData responseData = new HttpResponseData();
        responseData.StatusCode = 200;
        responseData.StatusMessage = "Ok";
        responseData.ResponseContent = "OK";
        responseData.ResponseHeaders.put("Content-Type", "text/plain");
        return responseData;
    }

    private HttpResponseData NotAcceptable() {
        HttpResponseData responseData = new HttpResponseData();
        responseData.StatusCode = 406;
        responseData.StatusMessage = "Not Acceptable";
        return responseData;
    }

    private HttpResponseData Unauthorized() {
        HttpResponseData responseData = new HttpResponseData();
        responseData.StatusCode = 401;
        responseData.StatusMessage = "Unauthorized";
        responseData.ResponseHeaders.put("WWW-Authenticate", "Basic realm=\"HAW\", charset=\"UTF-8\"");
        return responseData;
    }

    private void writeStatusCode(DataOutputStream out, int statusCode, String statusMessage) throws IOException {
        out.writeBytes("HTTP/1.0 " + statusCode + " " + statusMessage + "\n");
    }

    private void writeHeader(DataOutputStream out, String key, Object value) throws IOException {
        out.writeBytes(key + ": " + value + "\n");
    }

    private String readFromClient(BufferedReader reader) throws IOException {
        return reader.readLine();
    }
}
