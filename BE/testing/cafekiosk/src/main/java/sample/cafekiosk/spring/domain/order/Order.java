package sample.cafekiosk.spring.domain.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.aspectj.weaver.ast.Or;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sample.cafekiosk.spring.domain.BaseEntity;
import sample.cafekiosk.spring.domain.orderproduct.OrderProduct;
import sample.cafekiosk.spring.domain.product.Product;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders") // order 키워드가 존재하기 때문에 테이블명으로 order 불가능
@Entity
public class Order extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus;

	private int totalPrice;

	private LocalDateTime registeredDateTime;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
	private List<OrderProduct> orderProducts = new ArrayList<>();

	@Builder
	private Order(List<Product> products, OrderStatus orderStatus, LocalDateTime registeredDateTime) {
		this.orderStatus = orderStatus;
		this.totalPrice = calculateTotalPrice(products);
		this.registeredDateTime = registeredDateTime;
		this.orderProducts = products.stream()
			.map(product -> new OrderProduct(this, product))
			.collect(Collectors.toList());
	}

	public static Order create(List<Product> products, LocalDateTime registeredDateTime) {
		return Order.builder()
			.orderStatus(OrderStatus.INIT)
			.products(products)
			.registeredDateTime(registeredDateTime)
			.build();
	}

	private int calculateTotalPrice(List<Product> products) {
		return products.stream()
			.mapToInt(Product::getPrice)
			.sum();
	}
}
