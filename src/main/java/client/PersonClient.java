package client;

import org.json.JSONObject;

import java.io.IOException;

public class PersonClient extends Client{
    public PersonClient(String path) throws IOException {
        super(path);
    }

    String getPublicKey(){
        return jsonObject.getJSONObject("person").getJSONObject("keys").getString("public");
    }

    String getPrivateKey(){
        return jsonObject.getJSONObject("person").getJSONObject("keys").getString("private");
    }
    String getID() {
        return ((JSONObject) jsonObject.get("person")).getString("id");
    }

    String[] getName(){
       String[] name =  (((JSONObject) jsonObject.get("person")).getString("name")).split(",");
        return name;
    }
}
