package sample.cafekiosk.spring.api.service.product;

import static sample.cafekiosk.spring.domain.product.ProductSellingStatus.*;
import static sample.cafekiosk.spring.domain.product.ProductType.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import sample.cafekiosk.spring.api.controller.product.dto.request.ProductCreateRequest;
import sample.cafekiosk.spring.api.service.product.request.ProductCreateServiceRequest;
import sample.cafekiosk.spring.api.service.product.response.ProductResponse;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;

/**
 * readOnly = true : 읽기전용
 * CRUD 에서 CUD 동작 x, only Read
 * JPA : CUD 스냅샷 저장, 변경감지 x (성능 향상)
 *
 * CQRS - Command와 Query를 분리하자
 */
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductNumberFactory productNumberFactory;

	@Transactional
	public ProductResponse createProduct(ProductCreateServiceRequest request) {
		// nextProductNumber -> DB에서 마지막 저장된 Product의 상품 번호를 읽어와서 +1
		String nextProductNumber = productNumberFactory.createNextProductNumber();

		Product product = request.toEntity(nextProductNumber);
		Product savedProduct = productRepository.save(product);

		return ProductResponse.of(savedProduct);
	}

	public List<ProductResponse> getSellingProducts() {
		List<Product> products = productRepository.findAllBySellingStatusIn(forDisplay());

		return products.stream()
			.map(ProductResponse::of)
			.collect(Collectors.toList());
	}

	// private String createNextProductNumber() {
	// 	String latestProductNumber = productRepository.findLatestProductNumber();
	// 	if(latestProductNumber == null) {
	// 		return "001";
	// 	}
	//
	// 	int latestProductNumberInt = Integer.parseInt(latestProductNumber);
	// 	int nextProductNumberInt = latestProductNumberInt + 1;
	//
	// 	return String.format("%03d", nextProductNumberInt);
	// }
}
