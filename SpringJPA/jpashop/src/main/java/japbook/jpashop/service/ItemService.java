package japbook.jpashop.service;

import japbook.jpashop.domain.item.Book;
import japbook.jpashop.domain.item.Item;
import japbook.jpashop.repository.ItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    @Transactional
//    public void updateItem(Long itemId, UpdateItemDto itemDto) {
    public void updateItem(Long itemId, String name, int Price, int stockQuantity) {
        Item findItem = itemRepository.findOne(itemId); // param은 준영속 상태의 엔티티
//        findItem.change(name, price, stockQuantity); // setter를 직접 사용하는 것보다 수정 메서드를 만들어서 사용하는 것이 좋음
        findItem.setName(name);
        findItem.setPrice(Price);
        findItem.setStockQuantity(stockQuantity);
//        itemRepository.save(findItem); // findItem은 영속 상태이기 때문에 호출하지 않아도 데이터가 변경됨
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}