package client;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import org.json.*;


public class Client {
    JSONObject jsonObject;

    public Client(/*File config*/) throws IOException {
        this.jsonObject = new JSONObject(FileReader.read("src/main/resources/configs/config_1.json"));
        try {
            //Serverconnection
            Socket socket = SocketFactory.getDefault().createSocket(
                    ((JSONObject) jsonObject.get("server")).getString("ip"),
                    ((JSONObject) jsonObject.get("server")).getInt("port"));

            System.out.println("Connected to Server:" + socket.getInetAddress());

            var objectInputStream = new ObjectInputStream(socket.getInputStream());
            var objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            //TODO Try to register
            //objectInputStream enthaelt ACK wenn erfolgreich else Fehlermeldung

            Message message = new Message();
            message.setTYPE("REGISTER");
            message.setId(((JSONObject) jsonObject.get("person")).getString("id"));
            String[] name = (((JSONObject) jsonObject.get("person")).getString("name")).split(",");
            message.setLastName(name[0]);
            message.setFirstName(name[1]);
            message.setPublicKey(((JSONObject) ((JSONObject) jsonObject.get("person")).get("keys")).getString("public"));
            message.setMessageText("");
            //send to server
            message.writeObject(objectOutputStream);

            //FIXME no error but also no awnser from server
            Message awnser = new Message();
            awnser.readObject(objectInputStream);
            System.out.println(awnser.getTYPE() + " " + awnser.getMessageText());
            //oder so?
            // System.out.println(objectInputStream);

            //TODO: solange online (berücksichtige duration), warte ob eine nachricht erhalten wird. Wenn ja logging.
            //TODO: Parallel Actionen ausfuehren
            ArrayList<String> aktionsliste = new ArrayList<>();
            for (String aktion : aktionsliste
            ) {
                //Aktion entsprechend interpretieren und an Server schicken.
                // Beachten von Timeout und Retry. Ggfs. Hilfsmethode schreiben
            }
        } catch (IOException | ClassNotFoundException e) {
            //TODO
        }
    }
//TODO Hilfsmethoden zu schicken von Message an Server etc

}
