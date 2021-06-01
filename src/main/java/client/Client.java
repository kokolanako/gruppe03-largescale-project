package client;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws UnknownHostException, IOException {

        System.out.println("client starts");

        Socket socket= SocketFactory.getDefault().createSocket("localhost", 5001);
        var scanner = new Scanner(System.in);
        var dataInputStream = new DataInputStream(socket.getInputStream());
        var dataOutputStream = new DataOutputStream(socket.getOutputStream());
        while (scanner.hasNextLine()) {
            System.out.print("Next Message: ");
            dataOutputStream.writeUTF(scanner.nextLine());
            System.out.println(dataInputStream.readUTF());
        }
        scanner.close();
    }
}
