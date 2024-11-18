package Praktikum1;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

public class WebServiceClientWorker extends Thread {
    private Socket _clientSocket;

    public WebServiceClientWorker(Socket clientSocket) {
        _clientSocket = clientSocket;
    }

    public void run() {
        try {
            System.out.println();

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
            System.out.println();
            HttpResponseData responseData = onHttpRequestReceived(requestData, out);

            writeStatusCode(out, responseData.StatusCode, responseData.StatusMessage);
            for (String key : responseData.ResponseHeaders.keySet()) {
                writeHeader(out, key, responseData.ResponseHeaders.get(key));
            }
            if (responseData.ResponseContent != null) {
                int contentLength = responseData.ResponseContent.length;
                writeHeader(out, "Content-Length", contentLength);
            }
            out.writeBytes("\n");

            if (responseData.ResponseContent != null) {
                out.write(responseData.ResponseContent);
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

    private HttpResponseData onHttpRequestReceived(HttpRequestData requestData, DataOutputStream out) throws IOException {
        /* INIT DEFAULT CASE */
        HttpResponseData responseData = new HttpResponseData();
        responseData.StatusCode = 400;
        responseData.StatusMessage = "Bad request";
        /* INIT DEFAULT CASE */

        /* USER AGENT */
        String userAgent = requestData.Headers.get("user-agent");
        if (userAgent != null && !userAgent.toLowerCase().contains("firefox")) {
            return NotAcceptable();
        }
        /* USER AGENT */

        /* AUTHENTIFICATION */
        boolean authSuccess = false;
        Path path = Paths.get(".htuser");
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
                BufferedReader reader = new BufferedReader(new FileReader(".htuser"));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.length() > 0 && line.equals(credentials)) {
                        authSuccess = true;
                        break;
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!authSuccess)
                return Unauthorized();
        }
        /* AUTHENTIFICATION */

        String resource = requestData.Path.replaceFirst("/", "");
        if (resource.equals(""))
            resource = "index.html";
        path = Paths.get(resource);
        if (Files.exists(path)) {
            InputStream fileIn = new FileInputStream(path.toAbsolutePath().toFile());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = fileIn.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            responseData.ResponseContent = outputStream.toByteArray();
            String fileExt = fileExtension(resource);
            if (fileExt.equals(".gif")) {
                responseData.ResponseHeaders.put("Content-Type", "image/gif");
            } else if (fileExt.equals(".html")) {
                responseData.ResponseHeaders.put("Content-Type", "text/html; charset=utf-8");
            } else if (fileExt.equals(".jpg") || fileExt.equals(".jpeg")) {
                responseData.ResponseHeaders.put("Content-Type", "image/jpg");
            } else if (fileExt.equals(".pdf")) {
                responseData.ResponseHeaders.put("Content-Type", "application/pdf");
            }
            responseData.StatusCode = 200;
        } else {
            return NotFound();
        }
        return responseData;
    }

    public static String fileExtension(String fileName) {
        return Optional.of(fileName.lastIndexOf(".")).filter(i -> i >= 0)
                .filter(i -> i > fileName.lastIndexOf(File.separator))
                .map(fileName::substring).orElse("");
    }


    private HttpResponseData NotAcceptable() {
        HttpResponseData responseData = new HttpResponseData();
        responseData.StatusCode = 406;
        responseData.StatusMessage = "Not Acceptable";
        return responseData;
    }

    private HttpResponseData NotFound() {
        HttpResponseData responseData = new HttpResponseData();
        responseData.StatusCode = 404;
        responseData.StatusMessage = "Not Found";
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
        writeLine(out,"HTTP/1.0 " + statusCode + " " + statusMessage);
    }

    private void writeHeader(DataOutputStream out, String key, Object value) throws IOException {
        writeLine(out, key + ": " + value);
    }

    private void writeLine(DataOutputStream out, String val) throws IOException {
        System.out.println(val);
        out.writeBytes(val + "\r\n");
    }

    private String readFromClient(BufferedReader reader) throws IOException {
        return reader.readLine();
    }
}