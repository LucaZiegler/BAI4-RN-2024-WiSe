package Praktikum2;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;

public class MailFile {
    private static final String CRLF = "\r\n"; // Carriage Return + Line Feed

    public static void main(String[] args) throws IOException {

        //String recipient = args[0]; // uncomment only when running the program with this args, otherwise error
        //String filePath = args[1];

        File file = new File("MailFile.ini");

        FileReader reader = new FileReader(file);
        Properties config = new Properties();
        config.load(reader);


        String hostname = config.getProperty("SMTP_ADDRESS");
        String senderAddress = config.getProperty("SENDER_ADDRESS");
        String userAccount = config.getProperty("USER_ACCOUNT");
        String password = config.getProperty("PASSWORD");
        int port = Integer.parseInt(config.getProperty("SMTP_PORT"));
        String subject = config.getProperty("SUBJECT");
        String body = config.getProperty("BODY");

        SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket)factory.createSocket(hostname, port);

        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());

        String domainLine = receiveAndPrintInConsole(inFromServer);
        //System.out.println(domainLine);
        String[] domainLineParsed = domainLine.split(" "); // {code, domain, rest}
        if (Integer.parseInt(domainLineParsed[0]) != 220 || !domainLineParsed[1].equals(hostname)) {
            System.out.println("Service not ready or wrong server!");
            return;
        }

        sendAndPrintInConsole(outToServer, "EHLO localhost"); // send status line

        // The format for multiline replies requires that every line, except the last, begin with the reply code,
        // followed immediately by a hyphen, "-" (also known as minus), followed by text. The last line will begin
        // with the reply code, followed immediately by <SP>, optionally some text, and <CRLF>.
        // https://datatracker.ietf.org/doc/html/rfc5321#section-4.2.1
        String replyLine = receiveAndPrintInConsole(inFromServer);
        while(replyLine.charAt(3) == '-') {
            replyLine = receiveAndPrintInConsole(inFromServer);
        }

        // Authenticate using AUTH LOGIN
        sendAndPrintInConsole(outToServer, "AUTH LOGIN");
        receiveAndPrintInConsole(inFromServer); // Server prompts for username

        // Send Base64-encoded username
        sendAndPrintInConsole(outToServer, Base64.getEncoder().encodeToString(userAccount.getBytes()));
        receiveAndPrintInConsole(inFromServer);

        // Send Base64-encoded password
        sendAndPrintInConsole(outToServer, Base64.getEncoder().encodeToString(password.getBytes()));
        String authResponse = receiveAndPrintInConsole(inFromServer);
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
}