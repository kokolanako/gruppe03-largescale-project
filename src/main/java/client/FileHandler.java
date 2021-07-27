package client;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;


public class FileHandler {

    static String read(String path) throws IOException {
        File file = new File(path);
        String text = "";
        if (file.exists()){
            BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(file.getPath()))));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.print(line);
                text += line;
            }
        }
        System.out.print("\n");
            return text;
    }

  public static void write(String path, JSONObject jsonObject) throws IOException {
    File file = new File(path);
    BufferedWriter output = new BufferedWriter(new FileWriter(file));
    output.write(jsonObject.toString());
    output.close();
  }
}
