package Praktikum2;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.util.Base64;
import java.util.Properties;

public class MailFile {
    private static final String CRLF = "\r\n"; // Carriage Return + Line Feed

    public static void main(String[] args) throws IOException {

        String recipient = args[0]; // uncomment only when running the program with this args, otherwise error
        String filePath = args[1];

        Config config = LoadConfig("MailFile.ini");

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) factory.createSocket(config.hostname, config.port);

        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());

        String domainLine = receiveAndPrintInConsole(inFromServer);
        //System.out.println(domainLine);
        String[] domainLineParsed = domainLine.split(" "); // {code, domain, rest}
        if (Integer.parseInt(domainLineParsed[0]) != 220 || !domainLineParsed[1].equals(config.hostname)) {
            System.out.println("Service not ready or wrong server!");
            return;
        }

        sendAndPrintInConsole(outToServer, "EHLO localhost"); // send status line

        // The format for multiline replies requires that every line, except the last, begin with the reply code,
        // followed immediately by a hyphen, "-" (also known as minus), followed by text. The last line will begin
        // with the reply code, followed immediately by <SP>, optionally some text, and <CRLF>.
        // https://datatracker.ietf.org/doc/html/rfc5321#section-4.2.1
        String replyLine = receiveAndPrintInConsole(inFromServer);
        while (replyLine.charAt(3) == '-') {
            replyLine = receiveAndPrintInConsole(inFromServer);
        }

        //inFromServer.ready();

        // Authenticate using AUTH LOGIN
        sendAndPrintInConsole(outToServer, "AUTH LOGIN");
        receiveAndPrintInConsole(inFromServer); // Server prompts for username

        // Send Base64-encoded username
        sendAndPrintInConsole(outToServer, Base64.getEncoder().encodeToString(config.userAccount.getBytes()));
        receiveAndPrintInConsole(inFromServer);

        // Send Base64-encoded password
        sendAndPrintInConsole(outToServer, Base64.getEncoder().encodeToString(config.password.getBytes()));
        String authResponse = receiveAndPrintInConsole(inFromServer);

        // pass sender via SMTP
        sendAndPrintInConsole(outToServer, "MAIL FROM:<" + config.senderAddress + ">");
        receiveAndPrintInConsole(inFromServer);

        // pass recipient via SMTP
        sendAndPrintInConsole(outToServer, "RCPT TO:<" + recipient + ">");
        receiveAndPrintInConsole(inFromServer);

        // pass email text via SMTP
        sendAndPrintInConsole(outToServer, "DATA");
        receiveAndPrintInConsole(inFromServer);

        // Email headers and body
        sendAndPrintInConsole(outToServer, "From: " + config.senderAddress);
        sendAndPrintInConsole(outToServer, "To: " + recipient);
        sendAndPrintInConsole(outToServer, "Subject: " + config.subject);
        sendAndPrintInConsole(outToServer, "MIME-Version: 1.0");
        sendAndPrintInConsole(outToServer, "Content-Type: multipart/mixed; boundary=\"boundaryHAW\"");
        sendAndPrintInConsole(outToServer, ""); // Sends CRLF
        //outToServer.writeBytes(CRLF);

        sendAndPrintInConsole(outToServer, "--boundaryHAW");
        sendAndPrintInConsole(outToServer, "Content-Type: text/plain; charset=\"utf-8\"");
        sendAndPrintInConsole(outToServer, "Content-Transfer-Encoding: 7bit");
        sendAndPrintInConsole(outToServer, "");
        sendAndPrintInConsole(outToServer, config.body);
        sendAndPrintInConsole(outToServer, "");

        // File attachment
        File file = new File(filePath);
        String fileName = file.getName();
        String encodedFile = encodeFileToBase64(file);

        sendAndPrintInConsole(outToServer, "--boundaryHAW");
        sendAndPrintInConsole(outToServer, "Content-Type: application/octet-stream; name=\"" + fileName + "\"");
        sendAndPrintInConsole(outToServer, "Content-Transfer-Encoding: base64");
        sendAndPrintInConsole(outToServer, "Content-Disposition: attachment; filename=\"" + fileName + "\"");
        sendAndPrintInConsole(outToServer, "");
        sendAndPrintInConsole(outToServer, encodedFile);
        sendAndPrintInConsole(outToServer, "");

        // End of email
        sendAndPrintInConsole(outToServer, "--boundaryHAW--");
        sendAndPrintInConsole(outToServer, ".");
        receiveAndPrintInConsole(inFromServer);

        sendAndPrintInConsole(outToServer, "QUIT");
        receiveAndPrintInConsole(inFromServer);

    }

    private static Config LoadConfig(String path) throws IOException {
        File file = new File(path);

        FileReader reader = new FileReader(file);
        Properties properties = new Properties();
        properties.load(reader);

        Config config = new Config();
        config.hostname = properties.getProperty("SMTP_ADDRESS");
        config.senderAddress = properties.getProperty("SENDER_ADDRESS");
        config.userAccount = properties.getProperty("USER_ACCOUNT");
        config.password = properties.getProperty("PASSWORD");
        config.port = Integer.parseInt(properties.getProperty("SMTP_PORT"));
        config.subject = properties.getProperty("SUBJECT");
        config.body = properties.getProperty("BODY");
        return config;
    }

    public static void sendAndPrintInConsole(DataOutputStream outToServer, String line) throws IOException {
        outToServer.writeBytes(line + CRLF);
        System.out.println("C: " + line);
    }

    public static String receiveAndPrintInConsole(BufferedReader inFromServer) throws IOException {
        String line = inFromServer.readLine();
        System.out.println("S: " + line);
        return line;
    }

    private static String encodeFileToBase64(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }
}