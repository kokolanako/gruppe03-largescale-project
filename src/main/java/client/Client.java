package client;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import org.json.*;


public class Client {

    JSONObject jsonObject;

    public Client(File config) {
        //TODO read Config and init attribute
        this.jsonObject = new JSONObject("{\n" +
                "\t\"general\": {\n" +
                "\t\t\"duration\": \"360\",\n" +
                "\t\t\"retries\": \"5\",\n" +
                "\t\t\"timeout\": \"10\"\n" +
                "\t},\n" +
                "\t\"person\": {\n" +
                "\t\t\"id\": \"5856e6cd-0da6-4573-9a04-cbb11f5e68d3\",\n" +
                "\t\t\"name\": \"Küppers, Bastian\",\n" +
                "\t\t\"keys\": {\n" +
                "\t\t\"private\": \"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\",\n" +
                "\t\t\"public\": \"MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA33bh0ZPszl9wxWqP5TWtvO44QrqSdNA3yoPCRf8gBXvthOK9QK/xLART+q9yPIXjdUyR855GeAiPXvbee7IjzgY1ZGKBfpTxojPn13M+xRCae0SCezTWGZ1sxYYB1FRUsfQaTzzPC6wgJrYwh4BoRruWTruXzq3UbtQxmDKqCO2D0nIzrnubZQLfXBFMyGkVnqghiGblbXd7TcT6eJA7kGLnrCWhgt/TlLQwudOZ1VdfB7cHcNX8gCHV4E9rEoPMTEoc+kzXNEyWkdnivuNg7z1sGW2jDuHCcOEYJxwq6UaRn3qwe54VfkkMonR+d5UYuwJIbWuUhog5jcUbCQ5v8YhThk3vgiE6sDulAx1cOtCBk1JofTTnNOxzLOnxz82UUBYB0hUXRsWl8U15wELXIAw5glUzc0gVLMJeiLKwye7zCebpEL+HhKtTBcW6q7VWV4cu3dls18Tf+UjtMB+wRvh25y0mBNK+odKmVmko2Lf+IaAsbYvcjQTqxCVvIGqvQ9683RFBu1cPQkyiy60KldkRWVjTei98PjQafcqhxTAgUCBByoNuzTn+w0Mi1By4kIWkqOXEQWUQ0aprHPsk7v//aIJM2rBltcGk0EedwWvoiGaKzjdqIkXEP7RDM/h2V6VpYYAuPxsnSx1yPWfnixcoefQDDWXvBcvuvuRtAmcCAwEAAQ==\"\n" +
                "\t\t}\n" +
                "\t},\n" +
                "\t\"server\":\n" +
                "\t{\n" +
                "\t\t\"ip\": \"127.0.0.1\",\n" +
                "\t\t\"port\": \"13370\"\n" +
                "\t},\n" +
                "\t\"actions\": [\n" +
                "\t\t\"SEND [EIFERT, THOMAS] [HELLO THOMAS]\",\n" +
                "        \"SEND [6398C613619E4DCA88220ACA49603D87] [HELLO THOMAS 2]\",\n" +
                "        \"SEND [DOE, JOHN] [HELLO JOHN]\"\n" +
                "\t\t]\n" +
                "}");


        try {
            //Test
            System.out.println(jsonObject.get("person"));
            System.out.println(((JSONObject) jsonObject.get("server")).getInt("port"));

            //Serverconnection
            Socket socket = SocketFactory.getDefault().createSocket(
                    ((JSONObject) jsonObject.get("server")).getString("ip"),
                    ((JSONObject) jsonObject.get("server")).getInt("port"));

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
