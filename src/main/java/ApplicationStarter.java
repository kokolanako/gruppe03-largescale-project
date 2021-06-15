import client.Client;

import java.io.File;
import java.io.IOException;


public class ApplicationStarter {

    public static void main(String[] args) throws IOException {
        String path1 = "src/main/resources/configs/config_1.json";
        String path2 = "src/main/resources/configs/config_2.json";
        try {
            Client client1 = new Client(path1);
            Client client2 = new Client(path2);
            //TODO starte Timer oder so mit Duration. Wenn diese abgelaufen, beende Client.
            client1.getDuration();
            //TODO: solange online (berücksichtige duration), warte ob eine nachricht erhalten wird. Wenn ja logging.
            client1.runAllActions();
            client2.runAllActions();

            //TODO if duration over
            client1.closeConnection();
            client2.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
