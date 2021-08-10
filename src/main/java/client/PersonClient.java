package client;

import communication.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PersonClient extends Client{

    String personOrOrganisation = "person";
    public PersonClient(String path) throws IOException {
        super(path);
    }

    public String getPublicKey(){
        return jsonObject.getJSONObject("person").getJSONObject("keys").getString("public");
    }

    public String getPrivateKey(){
        return jsonObject.getJSONObject("person").getJSONObject("keys").getString("private");
    }
    public String getID() {
        return ((JSONObject) jsonObject.get("person")).getString("id");
    }

    public String[] getName(){
       String[] name =  (((JSONObject) jsonObject.get("person")).getString("name")).split(",");
        return name;
    }

    @Override
    public String getTypeInstance() {
        return "PERSON";
    }

    private void sendTransaction(Message messageTransaction) {
        System.out.println("-------------------- Trans " + messageTransaction);
        if (!connectionClosed) {
            try {
                this.serverCommunicator.streamOut(messageTransaction);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void runAllActions() {
        for (Object action : (JSONArray) jsonObject.get("actions")
        ) {
            if (connectionClosed) {
                break;
            }
            String fullAction = action.toString();
            String patternString = "\\[(.*?)\\]";//non-greedy match
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(fullAction);
            List<String> allMatches = new ArrayList<String>();

            while (matcher.find()) {
                allMatches.add(matcher.group(1).trim());
//        System.out.println(matcher.group(1));
            }
            if (fullAction.contains("SUB") || fullAction.contains("ADD")) {
                String messageType = null;
                Message messageTransaction = new Message();
                if (fullAction.contains("SUB")) {
                    messageType = "TRANSACTION_SUB";
                    messageTransaction.setIdReceiver(allMatches.get(0));
                    messageTransaction.setIbanFrom(allMatches.get(1));
                    messageTransaction.setAmount((this.convertToLong(allMatches.get(2))));

                } else if (fullAction.contains("ADD")) {
                    messageType = "TRANSACTION_ADD";
                    messageTransaction.setIbanFrom(allMatches.get(1));
                    messageTransaction.setIbanTo(allMatches.get(2));
                    messageTransaction.setAmount(this.convertToLong(allMatches.get(3)));

                }
                messageTransaction.setId(this.id);
                messageTransaction.setLastName(this.ownLastName);
                messageTransaction.setFirstName(this.ownFirstName);
                messageTransaction.setTYPE(messageType);
                this.sendTransaction(messageTransaction);

            } else {

                String messageType = "MESSAGE";
                if (allMatches.size() <= 3) {

                    if (allMatches.size() == 3) {
                        messageType = allMatches.get(2);
                    }
                    String message = allMatches.get(1);
                    String receiver = allMatches.get(0);
                    System.out.println("***********CHECK REGEX EXTRACTION BELOW**********");
                    System.out.println(receiver + " " + messageType + " " + message);
                    if (messageType.equals("BUSINESS")) {
                        messageType = "MESSAGE_BUSINESS";
                    } else if (messageType.equals("PRIVATE")) {
                        messageType = "MESSAGE_PRIVATE";
                    }
                    if (receiver.contains(",")) {
                        sendName(receiver, message, messageType);
                    } else {
                        sendID(receiver, message, messageType);
                    }

                }
            }
        }
    }

    /**
     * sends a Message via Name
     *
     * @param name
     * @param messageText
     * @param messageType
     */
    void sendName(String name, String messageText, String messageType) {
        try {
            //get public key of receiver
            Message askKeyMessage = new Message();
            String[] splitName = name.split(",");
            splitName[0] = splitName[0].toLowerCase().trim();
            splitName[1] = splitName[1].toLowerCase().trim();

            askKeyMessage.setLastNameReceiver(splitName[0]);
            askKeyMessage.setFirstNameReceiver(splitName[1]);
            askKeyMessage.setTypeInstance(getTypeInstance());
            try {
                String publicKey = getReceiverPublicKey(askKeyMessage);

                //encrypt message
                Message sendMessage = createSendMessage(messageText, publicKey, messageType);
                sendMessage.setLastNameReceiver(splitName[0]);
                sendMessage.setFirstNameReceiver(splitName[1]);
                sendMessage(sendMessage);
            } catch (NoKeyException e) {
                System.out.println("Can not get key from " + askKeyMessage.getFirstNameReceiver() + " " + askKeyMessage.getLastNameReceiver()
                        + ", maybe (s)he does not exist");
                // e.printStackTrace();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * sents an message via ID
     *
     * @param id
     * @param messageText
     * @param messageType
     */
    void sendID(String id, String messageText, String messageType) {
        try {
            //get public key of receiver
            Message askKeyMessage = new Message();
            askKeyMessage.setIdReceiver(id);
            try {
                String publicKey = getReceiverPublicKey(askKeyMessage);

                //encrypt message
                Message sendMessage = createSendMessage(messageText, publicKey, messageType);
                sendMessage.setIdReceiver(id);

                sendMessage(sendMessage);
            } catch (NoKeyException e) {
                System.out.println("Can not get key from " + askKeyMessage.getId() + ", maybe (s)he does not exist");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        // Beachte Timeout und Retry.
    }

    /**
     * Sends a Message  to Receiver
     *
     * @param message
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void sendMessage(Message message) throws IOException, ClassNotFoundException {
        //send message to receiver
        if (!connectionClosed) {
            Message answer = this.serverCommunicator.request(message, "OK");
            if (answer != null) {
                String logMsg = "MESSAGE OK answer in Thread " + Thread.currentThread().getName() + " " + answer.getMessage_ID() + " received: " + answer.getTYPE() + " "
                        + answer.getMessageText();
                System.out.println(logMsg);

            } else {
                System.out.println("Stop now retrying");
            }
        }
    }

}
