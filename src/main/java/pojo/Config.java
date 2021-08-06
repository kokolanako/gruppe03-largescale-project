package pojo;

import io.ConfigParser;
import lombok.Data;

import java.util.List;

@Data
public class Config {
  //public Organisation organizations;
  private General general;
  private Server server;

  @Override
  public String toString() {

    return "";//ConfigParser.listOfObjectsToString(this.accounts);
  }
}
