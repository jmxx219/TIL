# 웹 애플리케이션 개발

### 목차
- [도메인 분석 설계](#도메인-분석-설계)
  - [요구사항 분석](#요구사항-분석)
  - [도메인 모델과 테이블 설계](#도메인-모델과-테이블-설계)
  - [엔티티 클래스 개발](#엔티티-클래스-개발)
  - [엔티티 설계 시 주의점](#엔티티-설계-시-주의점)
- [애플리케이션 구현 준비](#애플리케이션-구현-준비)
  - [구현 요구사항](#구현-요구사항)
  - [애플리케이션 아키텍처](#애플리케이션-아키텍처)
- [회원 도메인 개발](#회원-도메인-개발)
  - [회원 리포지토리 개발](#회원-리포지토리-개발)


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
<summary><b>회원 엔티티 분석</b></summary>
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

<details>
<summary><b>회원 테이블 분석</b></summary>
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

<details>
<summary><b>연관관계 매핑 분석</b></summary>
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

### 엔티티 클래스 개발

**Getter, Setter**
- 이론적으로는 `Getter`와 `Setter` 모두 제공하지 않고, 꼭 필요한 별도의 메서드를 제공하는 것이 가장 이상적
  - 실무에서는 엔티티의 데이터를 조회할 일이 많기 때문에 `Getter`의 경우 모두 열어두는 것이 편리함
  - `Setter`의 경우 호출하면 데이터가 변경될 수 있어, 엔티티가 왜 변경되었는지 추적하기 어려움
- 엔티티 변경 시, `Setter` 대신에 변경 지점이 명확하도록 변경을 위한 비즈니스 메서드를 별도로 제공해야 함

**id**
- 엔티티의 식별자는 `id`를 사용하고 PK 컬럼명은 `member_id`를 사용
  - 엔티티는 타입이 있으므로 `id` 필드만으로도 쉽게 구분 가능
  - 테이블은 타입이 없으므로 구분이 어려움
- 테이블은 관례상 `테이블명 + id`를 많이 사용함
- 객체에서 `id` 대신에 `memberId`와 같이 사용해도 됨(중요한 것은 일관성)

**`@ManyToMany`**
- 실무에서는 사용하지 않도록 함
  - `@ManyToMany`는 중간 테이블에 컬럼을 추가할 수 없고, 세밀하게 쿼리를 실행하기 어려움
- 중간 엔티티를 만들고 `@ManyToOne`, `@OneToMany`로 매핑해서 사용
  - 다대다 매핑을 일대다, 다대일 매핑으로 풀어서 사용하기

**값 타입**
- 값 타입은 변경 불가능하게 설계해야 함
- `@Setter`를 사용하지 않고, 생성자에서 값을 모두 초기화하여 변경 불가능한 클래스로 만들기
- JPA 스펙상 엔티티나 임베디드 타입(`@Embeddable`)은 자바 기본 생성자를 `public` 또는 `protected`로 설정해야 함
  - `public` 보다 `protected`로 설정하는 것이 그나마 더 안전함
  - JPA 구현 라이브러리가 객체를 생성할 때 리플랙션 같은 기술을 사용할 수 있도록 지원해야 하기 때문에 이러한 제약을 둠 

<br/>

### 엔티티 설계 시 주의점

**엔티티에는 Setter를 사용하지 않기**
- Setter가 열려있으면 변경 포인트가 너무 많아 유지보수하기 어려움
- 나중에 리팩토링으로 Setter 제거

**모든 연관관계는 지연로딩으로 설정**
- 즉시로딩(`EAGER`)은 예측이 어렵고, 어떤 SQL이 실행될지 추적하기 어려움
  - JPQL을 실행할 때 N+1 문제가 자주 발생함
- 실무에서 모든 연관관계는 지연로딩(`LAZY`)으로 설정해야 함
  - 연관된 엔티티를 함께 DB에서 조회해야 하면, fetch join 또는 엔티티 그래프 기능을 사용함
- @XToOne(`@OneToOne`, `@ManyToOne`) 관계는 기본이 즉시로딩이므로 직접 지연로딩으로 설정해야 함

**컬렉션은 필드에서 초기화하기**
- 컬렉션은 필드에서 바로 초기화하는 것이 안전함
- `null` 문제에 안전함
- 하이버네이트는 엔티티를 영속화할 때, 컬렉션을 감싸서 하이버네이트가 제공하는 내장 컬렉션으로 변경함
  - 만약 getOrders()처럼 임의의 메서드에서 컬렉션을 잘못 생성하면 하이버네이트 내부 매커니즘에 문제가 발생할 수 있음
  - 따라서 필드레벨에서 생성하는 것이 가장 안전하고, 코드도 간결해짐
  ```java
  Member member = new Member();
  System.out.println(member.getOrders().getClass());
  em.persist(member);
  System.out.println(member.getOrders().getClass());
  
  //출력 결과
  class java.util.ArrayList
  class org.hibernate.collection.internal.PersistentBag
  ```
  
**테이블, 컬럼명 생성 전략**
- 스프링 부트에서 하이버네이트 기본 매핑 전략을 변경해서 실제 테이블 필드명은 다름
  - 하이버네이트 기존 구현: 엔티티의 필드명을 그대로 테이블의 컬럼명으로 사용(`SpringPhysicalNamingStrategy`)
- 스프링 부트 신규 설정(`엔티티(필드)` ➜ `테이블(컬럼)`)
  1. 카멜 케이스 ➜ 언더스코어
  2. `.`(점) ➜ `_`(언더스코어)
  3. 대문자 ➜ 소문자

<br/>

## 애플리케이션 구현 준비

### 구현 요구사항

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

- 예제를 단순화하기 위해 다음 기능은 구현하지 않음
  - 로그인과 권한 관리 X
  - 파라미터 검증과 예외 처리 X
  - 상품은 도서만 사용
  - 카테고리 사용 X
  - 배송 정보 사용 X

<br/>

### 애플리케이션 아키텍처

<img src="https://github.com/jmxx219/SpringJPA/assets/52346113/2a699871-2d92-431e-9c93-e3f7eefeea49" width="500" height="150"/>

- 계층형 구조 사용
  - `controller`, `web` : 웹 계층
  - `service` :  비즈니스 로직, 트랜잭션 처리
  - `repository` : JPA를 직접 사용하는 계층, 엔티티 매니저 사용
  - `domain` : 엔티티가 모여있는 계층, 모든 계층에서 사용
- 패키지 구조
  - `jpabook.jpashop` 
    - `domain`
    - `exception`
    - `repository`
    - `service`
    - `web`
- 개발 순서: 서비스와 리포지토리 계층 먼저 개발, 테스트 케이스를 작성해서 검증, 마지막에 웹 계층 적용

<br/>


## 회원 도메인 개발


### 회원 리포지토리 개발

- `@Repository` : 스프링 빈으로 등록, JPA 예외를 스프링 기반 예외로 예외 변환 
- `@PersistenceContext` : 엔티티 메니저(`EntityManager`) 주입 
- `@PersistenceUnit` : 엔티티 메니터 팩토리(`EntityManagerFactory`) 주입

<br/>

