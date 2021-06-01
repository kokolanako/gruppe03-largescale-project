package client;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    InetAddress ip = null;
    int port=5001;
    ArrayList<String> aktionsliste=new ArrayList<>();
    String name;
    //Todo weitere Attribute

    public Client(File config) {
        //TODO read Config and init attributes
        try {

            Socket socket = SocketFactory.getDefault().createSocket(ip, port);
            var dataInputStream = new DataInputStream(socket.getInputStream());
            var dataOutputStream = new DataOutputStream(socket.getOutputStream());
            //TODO Try to register
            //dataOuputStream enthaelt ACK wenn erfolgreich else Fehlermeldung

            //TODO: solange online (berücksichtige duration), warte ob eine nachricht erhalten wird. Wenn ja logging.
            //TODO: Parallel Actionen ausfuehren
            for (String aktion: aktionsliste
                 ) {
                //Aktion entsprechend interpretieren und an Server schicken.
                // Beachten von Timeout und Retry. Ggfs. Hilfsmethode schreiben
            }
        } catch (IOException e) {
            //TODO
        }
    }


//    public static void main(String[] args) throws UnknownHostException, IOException {
//
//        System.out.println("client starts");
//
//        Socket socket= SocketFactory.getDefault().createSocket("localhost", 5001);
//        var scanner = new Scanner(System.in);
//        var dataInputStream = new DataInputStream(socket.getInputStream());
//        var dataOutputStream = new DataOutputStream(socket.getOutputStream());
//        while (scanner.hasNextLine()) {
//            System.out.print("Next Message: ");
//            dataOutputStream.writeUTF(scanner.nextLine());
//            System.out.println(dataInputStream.readUTF());
//        }
//        scanner.close();
//    }
}
