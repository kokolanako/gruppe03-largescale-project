import client.Client;

import java.io.File;

public class ApplicationStarter {

  public static void main(String[] args) {
    String path=""; //TODO Pfad zu Config Datei
    Client client=new Client(new File(path));
  }

}
