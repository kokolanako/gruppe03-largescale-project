package client;

import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;


public class FileReader {

    static JSONObject read(String path) throws IOException {
        File file = new File("src/main/resources/configs/config_1.json");
        String text = "";
        if (file.exists()){
            BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(file.getPath()))));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.print(line);
                text += line;
            }
        }
        return new JSONObject(text);
    }
}
