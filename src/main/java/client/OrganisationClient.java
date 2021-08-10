package client;

import org.json.JSONObject;

import java.io.IOException;

public class OrganisationClient extends Client{

    String personOrOrganisation = "organisation";
    public OrganisationClient(String path) throws IOException {
        super(path);
    }

    public String getPublicKey(){
        return jsonObject.getJSONObject("organisation").getJSONObject("keys").getString("public");
    }

    public String getPrivateKey(){
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
    public String getTypeInstance() {
        return "ORGANIZATION";
    }
}
