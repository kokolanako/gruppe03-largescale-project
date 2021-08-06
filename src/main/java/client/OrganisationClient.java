package client;

import communication.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrganisationClient extends Client{

    String personOrOrganisation = "organisation";
    public OrganisationClient(String path) throws IOException {
        super(path);
    }

    String getPublicKey(){
        return jsonObject.getJSONObject("organisation").getJSONObject("keys").getString("public");
    }

    String getPrivateKey(){
        return jsonObject.getJSONObject("organisation").getJSONObject("keys").getString("private");
    }

    public String getID() {
        return ((JSONObject) jsonObject.get("organisation")).getString("id");
    }

    public String[] getName() {
         String[] name= {"", ((JSONObject) this.jsonObject.get("organisation")).getString("name")};
         return  name;
    }

    @Override
    String getTypeInstance() {
        return "ORGANIZATION";
    }
}
