package pojo;

import lombok.Data;

import java.util.List;

@Data
public class Organisation{
    private Keys keys;
    private String name;
    private String id;
    private List<Account> accounts;
}
