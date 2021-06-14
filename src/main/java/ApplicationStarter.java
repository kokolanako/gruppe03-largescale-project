import client.Client;

import java.io.File;
import java.io.IOException;

public class ApplicationStarter {

    public static void main(String[] args) throws IOException {
        String path = "src/main/resources/configs/config_1.json";
        try {
            Client client = new Client(path);
            System.out.println("Client created and registered");
            //TODO starte Timer oder so mit Duration. Wenn diese abgelaufen, beende Client.
            client.getDuration();
            //TODO: solange online (berücksichtige duration), warte ob eine nachricht erhalten wird. Wenn ja logging.
            client.runAllActions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
