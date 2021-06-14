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
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;
    public Client(String path) throws IOException {
        this.jsonObject = new JSONObject(FileReader.read(path));
        try {
            //Serverconnection
            this.socket = SocketFactory.getDefault().createSocket(
                    ((JSONObject) jsonObject.get("server")).getString("ip"),
                    ((JSONObject) jsonObject.get("server")).getInt("port"));
            System.out.println("Connected to Server:" + socket.getInetAddress());

            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            register(objectInputStream, objectOutputStream);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void register(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws IOException, ClassNotFoundException {
        //objectInputStream enthaelt ACK wenn erfolgreich else Fehlermeldung

        //create message for registration
        Message message = createRegistrationMessage();
        //send to server
        System.out.println("Start sending Message");
        objectOutputStream.writeObject(message); //switched message and objectoutputstream //todo: soll ein dataOutputstream erzeugt werden
        objectOutputStream.flush();
        System.out.println("Message send");

        System.out.println("Waiting for answer");
        Message answer = (Message) objectInputStream.readObject();
        System.out.println("Answer received");
        System.out.println(answer.getTYPE() + " " + answer.getMessageText());
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

    private void sendName(String name, String messageText) {
        try {


            Message askKeyMessage = new Message();
            askKeyMessage.setTYPE("ASK_PUBLIC_KEY");
            String[] splitName = name.split(",");
            askKeyMessage.setLastName(splitName[0].trim());
            askKeyMessage.setFirstName(splitName[1].trim());

            System.out.println("Start sending ASK_PUBLIC_KEY_Message");
            objectOutputStream.writeObject(askKeyMessage);
            objectOutputStream.flush();
            System.out.println("Message send");

            System.out.println("Waiting for answer");
            //FIXME java.net.SocketException: Software caused connection abort: recv failed
            Message answer = (Message) objectInputStream.readObject();
            System.out.println("Answer received");
            System.out.println(answer.getTYPE() + " " + answer.getMessageText());

            Message sendMessage;
            if(answer.getTYPE().equals("ASK_PUBLIC_KEY")){
                sendMessage=createSendMessage(messageText,answer.getMessageText());
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }


        //TODO Aktion an Server schicken.
        // Beachte Timeout und Retry.
    }

    private void sendID(String id, String messageText) {
        //TODO Aktion an Server schicken.
        // Beachte Timeout und Retry.
    }

    private Message createSendMessage(String messageText, String recieverPublicKey) {
        Message message = new Message();
        message.setTYPE("MESSAGE");
        //TODO messageText verschluesseln
        String cryptedText = messageText;
        message.setMessageText(cryptedText);

        return message;
    }

    public void runAllActions() {
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
        }
    }

    public int getDuration() {
        return ((JSONObject) this.jsonObject.get("general")).getInt("duration");
    }
}
