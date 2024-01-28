package japbook.jpashop.service;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateItemDto {

    private String name;
    private int Price;
    private int stockQuantity;

}
