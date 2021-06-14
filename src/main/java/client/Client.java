package client;

import javax.net.SocketFactory;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import communication.Message;
import org.json.*;


public class Client {
    JSONObject jsonObject;
    Socket socket;

    public Client(String path) throws IOException {
        this.jsonObject = new JSONObject(FileReader.read(path));
        try {
            //Serverconnection
            this.socket = SocketFactory.getDefault().createSocket(
                    ((JSONObject) jsonObject.get("server")).getString("ip"),
                    ((JSONObject) jsonObject.get("server")).getInt("port"));
            System.out.println("Connected to Server:" + socket.getInetAddress());

            var objectInputStream = new ObjectInputStream(socket.getInputStream());
            var objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            register(objectInputStream, objectOutputStream);
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
        Message answer = (Message) objectInputStream.readObject();
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

    private void sendName(String Name, String messageText) {

    }

    private void sendID(String id, String messageText) {

    }


    public void runAllActions() {

        ArrayList<String> aktionsliste = new ArrayList<>();
        for (Object action : (JSONArray) jsonObject.get("actions")
        ) {
            String[] splitAction = action.toString().split("\\[");
            if (splitAction.length == 3) {
                if (splitAction[0].trim().equals("SEND")) {
                    String message = splitAction[2].trim().substring(0, splitAction[2].trim().length() - 1);
                    String reciever = splitAction[1].trim().substring(0, splitAction[1].trim().length() - 1);
                    if (reciever.contains(",")) {
                        sendName(reciever, message);
                    } else {
                        sendID(reciever, message);
                    }
                }
            }

            //Aktion entsprechend interpretieren und an Server schicken.
            // Beachten von Timeout und Retry. Ggfs. Hilfsmethode schreiben
        }
    }

    public int getDuration() {
        return ((JSONObject) this.jsonObject.get("general")).getInt("duration");
    }
}
