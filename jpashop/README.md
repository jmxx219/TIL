# 웹 애플리케이션 개발

### 목차
- [도메인 분석 설계](#도메인-분석-설계)
  - [요구사항 분석](#요구사항-분석)
  - [도메인 모델과 테이블 설계](#도메인-모델과-테이블-설계)


<br/>

## 도메인 분석 설계

### 요구사항 분석

<details>
<summary>회원 기능</summary>
<div>

- 회원 등록
- 회원 조회

</div>
</details>

<details>
<summary>상품 기능</summary>
<div>

- 상품 등록
- 상품 수정
- 상품 조회

</div>
</details>

<details>
<summary>주문 기능</summary>
<div>

- 상품 주문
- 주문 내역 조회
- 주문 취소

</div>
</details>

<details>
<summary>기타 요구사항</summary>
<div>

- 상품은 재고 관리가 필요하다. 
- 상품의 종류는 도서, 음반, 영화가 있다. 
- 상품을 카테고리로 구분할 수 있다. 
- 상품 주문시 배송 정보를 입력할 수 있다.

</div>
</details>

<br/>

### 도메인 모델과 테이블 설계

**회원, 주문, 상품의 관계**
- 주문과 상품은 `다대다(N:M)` 관계
  - 회원은 여러 상품을 주문할 수 있고, 한 번 주문할 때 여러 상품을 선택할 수 있음
  - 다대다 관계는 관계형 DB는 물론이고 엔티티에서도 거의 사용하지 않음
- 주문 상품 이라는 엔티티를 추가
  - 다대다 관계를 주문과 주문상품 `일대다(1:N)`, 주문상품과 상품 `다대일(N:1)` 관계로 풀어냄

**상품 분류**
- 상품은 도서, 음반, 영화로 구분됨
- 상품이라는 공통 속성을 사용하므로 상속 구조로 표현

<br/>

<details>
<summary><b>회원 엔티티 분석<b/></summary>
<img src="https://github.com/jmxx219/SpringJPA/assets/52346113/a39db14c-c1b9-4f3b-b0d0-f2ac35c84b50" width="500" height="300"/>
<div>

- 회원(`Member`)
  - 이름(`name`)과 임베디드 타입인 주소(`Address`), 그리고 주문(`orders`) 리스트를 가짐
- 주문(`Order`)
  - 상품을 주문한 회원(`member`)과 배송 정보(`delivery`), 주문 날짜(`orderDate`), 주문 상태(`status`)
  - 주문 상태는 열거형 사용, 주문(`ORDER`)과 취소(`CANCEL`) 표현
  - 한 번 주문시 여러 상품을 주문할 수 있으므로 주문과 주문상품은 일대다 관계
- 주문상품(`OrderItem`)
  - 주문한 상품 정보와 주문 금액(`orderPrice`), 주문 수량 정보(`count`)를 가짐
- 상품(`Item`)
  - 이름(`name`), 가격(`price`), 재고수량(`stockQuantity`)을 가짐
  - 상품을 주문하면 재고수량이 줄어듦
  - 상품의 종류(`category`)는 도서, 음반, 영화가 있고, 사용하는 속성은 조금씩 다름
- 배송(`Delivery`)
  - 주문 시, 하나의 배송 정보를 생성
  - 주문과 배송은 일대일 관계
- 카테고리(`Category`)
  - `parent`, `child`로 부모와 자식 카테고리를 연결함
  - 상품과 다대다 관계를 맺음
- 주소(`Address`)
  - 값 타입(임베디트 타입)
  - 회원과 배송에서 사용
</div>
</details>
<br/>

<details>
<summary><b>회원 테이블 분석<b/></summary>
<img src="https://github.com/jmxx219/SpringJPA/assets/52346113/8925a53e-f730-4930-8782-f3fb707a3161" width="450" height="400"/>
<div>

- `MEMBER`
  - 회원 엔티티의 `Address` 임베디드 타입 정보가 회원테이블로 그대로 들어감
- `ITEM`
  - 상품 타입을 통합해서 하나의 테이블로 만들고, `DTYPE` 컬럼으로 타입을 구분
- `ORDERS`
  - `ORDER`는 데이터베이스에서 `order by` 때문에 예약어로 잡고있는 경우가 많기 때문에 테이블명은 `ORDERS`를 많이 사용함

</div>
</details>
<br/>

<details>
<summary><b>연관관계 매핑 분석<b/></summary>
<div>

- 회원과 주문
  - 일대다, 다대일의 양방향 관계
  - 연관관계의 주인을 정할 때, 외래 키가 있는 주문을 연관관계의 주인으로 정하는 것이 좋음
  - `Order.member`를 `ORDERS.MEMBER_ID(FK)` 외래키와 매핑
- 주문상품과 주문
  - 다대일 양방향 관계
  - 외래키가 주문상품에 있으므로 주문상품이 연관관계의 주인
  - `OrderItem.order`를 `ORDER_ITEM.ORDER_ID(FK)` 외래 키와 매핑
- 주문상품과 상품
  - 다대일 단방향 관계
  - `OrderItem.item`을 `ORDER_ITEM.ITEM_ID(FK)` 외래 키와 매핑
- 주문과 배송
  - 일대일 양방향 관계
  - `Order.delivery`를 `ORDERS.DELIVERY_ID(FK)` 외래 키와 매핑
- 카테고리와 상품
  - `@ManyToMany`를 사용해서 매핑
  - 실무에서는 `@ManyToMany`를 사용하지 않도록 함
</div>
</details>
<br/>

> **외래키가 있는 곳을 연관관계의 주인으로 정하기**  
> 연관관계의 주인은 단순히 외래키를 누가 관리하냐의 문제이지 비즈니스상 우위에 있다고 주인으로 정하면 안된다



<br/>