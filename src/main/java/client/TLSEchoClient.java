package client;

import javax.net.ssl.SSLSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TLSEchoClient {
    public static void main(String[] args) throws UnknownHostException, IOException {
        System.setProperty("javax.net.ssl.trustStore","src/main/resources/de/fhac/rn/truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword","geheim");

        System.out.println("client starts");

        Socket sslSocket= SSLSocketFactory.getDefault().createSocket("localhost",5001);
        var scanner = new Scanner(System.in);
        var dataInputStream = new DataInputStream(sslSocket.getInputStream());
        var dataOutputStream = new DataOutputStream(sslSocket.getOutputStream());
        while (scanner.hasNextLine()) {
            System.out.print("Next Message: ");
            dataOutputStream.writeUTF(scanner.nextLine());
            System.out.println(dataInputStream.readUTF());
        }
        scanner.close();
    }
}
