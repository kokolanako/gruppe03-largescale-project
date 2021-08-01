package client;

import communication.Message;
import communication.ServerCommunicator;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class OrganisationClient extends Client{

    public OrganisationClient(String path) throws IOException {
        super(path);
    }

    String getPublicKey(){
        return jsonObject.getJSONObject("organisation").getJSONObject("keys").getString("public");
    }

    String getPrivateKey(){
        return jsonObject.getJSONObject("organisation").getJSONObject("keys").getString("private");
    }

    String getID() {
        return ((JSONObject) jsonObject.get("organisation")).getString("id");
    }

    String[] getName() {
         String[] name= {((JSONObject) this.jsonObject.get("organisation")).getString("name"), ""};
         return  name;
    }


}
