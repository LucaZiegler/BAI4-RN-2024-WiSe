package praktikum_2;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.util.Properties;

public class MailFile {
    private static final String CRLF = "\r\n"; // Carriage Return + Line Feed

    public static void main(String[] args) throws IOException {
        FileReader reader = new FileReader("MailFile.ini");
        Properties config = new Properties();
        config.load(reader);

        String hostname = config.getProperty("SMTP_ADDRESS");
        int port = Integer.parseInt(config.getProperty("SMTP_PORT"));

        SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket)factory.createSocket(hostname, port);

        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
        String domainLine = inFromServer.readLine();
        //System.out.println(domainLine);
        String[] domainLineParsed = domainLine.split(" "); // {code, domain, rest}
        if (Integer.parseInt(domainLineParsed[0]) != 220 || !domainLineParsed[1].equals(hostname)) {
            System.out.println("Service not ready or wrong server!");
            return;
        }
        String userAccount = config.getProperty("USER_ACCOUNT");
        outToServer.writeBytes("EHLO localhost" + CRLF); // send status line

    }
}
