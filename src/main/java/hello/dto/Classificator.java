package hello.dto;


import lombok.Data;

/**
 * Created by otves on 15.10.2016.
 */
@Data
public class Classificator {
    String code;
    String name;
    String notes;
    int level;
    String parentCode;
    boolean hasChildren = true;
}
