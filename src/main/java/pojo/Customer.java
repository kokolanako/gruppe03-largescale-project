package pojo;

import lombok.Data;

import java.util.List;

@Data
public class Customer {
    private String id;
    private List<String> roles;
}
