package pojo;

import lombok.Data;

import java.util.List;

@Data
public class Organisation{
    List<String> keys;
    private String name;
    private String id;
    List<Account> accounts;
}
