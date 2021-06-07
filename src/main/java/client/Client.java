package client;

import javax.net.SocketFactory;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import communication.Message;
import org.json.*;



public class Client {
    JSONObject jsonObject;

    public Client(String path) throws IOException {
        this.jsonObject = new JSONObject(FileReader.read(path));
        try {
            //Serverconnection
            Socket socket = SocketFactory.getDefault().createSocket(
                    ((JSONObject) jsonObject.get("server")).getString("ip"),
                    ((JSONObject) jsonObject.get("server")).getInt("port"));
            System.out.println("Connected to Server:" + socket.getInetAddress());

            var objectInputStream = new ObjectInputStream(socket.getInputStream());
            var objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            register(objectInputStream, objectOutputStream);

            //TODO: solange online (berücksichtige duration), warte ob eine nachricht erhalten wird. Wenn ja logging.
            //TODO: Parallel Actionen ausfuehren ??? wieso? man muss ja auf die registrierung warten ...
            // A: Ich meinte nach der Regestrierung und paraalel zum Warten, ob man Nachrichten von anderen erhaelt
            ArrayList<String> aktionsliste = new ArrayList<>();
            for (String aktion : aktionsliste
            ) {
                //Aktion entsprechend interpretieren und an Server schicken.
                // Beachten von Timeout und Retry. Ggfs. Hilfsmethode schreiben
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void register(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws IOException, ClassNotFoundException {
        //TODO Try to register
        //objectInputStream enthaelt ACK wenn erfolgreich else Fehlermeldung

        //create message for registration
        Message message = createRegistrationMessage();
        //send to server
        System.out.println("Start sending Message");
        objectOutputStream.writeObject(message); //switched message and objectoutputstream //todo: soll ein dataOutputstream erzeugt werden
        objectOutputStream.flush();
        System.out.println("Message send");
        //FIXME no error but also no answer from server

        System.out.println("Waiting for answer");
        Message answer= (Message)objectInputStream.readObject();
        System.out.println("Answer received");
        System.out.println(answer.getTYPE() + " " + answer.getMessageText());
        //oder so?
        // System.out.println(objectInputStream);
    }

    private Message createRegistrationMessage() {
        Message message = new Message();
        message.setTYPE("REGISTER");
        message.setId(((JSONObject) jsonObject.get("person")).getString("id"));
        String[] name = (((JSONObject) jsonObject.get("person")).getString("name")).split(",");
        message.setLastName(name[0]);
        message.setFirstName(name[1]);
        message.setPublicKey(((JSONObject) ((JSONObject) jsonObject.get("person")).get("keys")).getString("public"));
        message.setMessageText("");
        return message;
    }

    /*void send(String Name, String messageText){

    }

    void send(String id , String messageText){

    }*/
//TODO Hilfsmethoden zu schicken von Message an Server etc

}
