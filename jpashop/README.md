# 웹 애플리케이션 개발 - API 개발과 성능 최적화

### 목차
- [도메인 분석 설계](#도메인-분석-설계)
  - [요구사항 분석](#요구사항-분석)
  - [도메인 모델과 테이블 설계](#도메인-모델과-테이블-설계)
  - [엔티티 클래스 개발](#엔티티-클래스-개발)
  - [엔티티 설계 시 주의점](#엔티티-설계-시-주의점)
- [애플리케이션 구현 준비](#애플리케이션-구현-준비)
  - [구현 요구사항](#구현-요구사항)
  - [애플리케이션 아키텍처](#애플리케이션-아키텍처)
- [기능 구현](#기능-구현)
  - [회원 도메인 개발](#◽-회원-도메인-개발)
  - [상품 도메인 개발](#◽-상품-도메인-개발)
  - [주문 도메인 개발](#◽-주문-도메인-개발)
    - [주문 검색 기능](#주문-검색-기능)
  - [웹 계층 개발](#◽-웹-계층-개발)
    - [변경 감지와 병합](#변경-감지와-병합)
- [API 개발](#API-개발)
  - [API 기본](#0.-API-기본)
  - [지연 로딩과 조회 성능 최적화](#1.-지연-로딩과-조회-성능-최적화)
  - [컬렉션 조회 최적화](#2.-컬렉션-조회-최적화)
  - [실무 필수 최적화](#3.-실무-필수-최적화)
  - [OSIV와 성능 최적화](#4.-OSIV와-성능-최적화)

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

**✔️ Getter, Setter**
- 이론적으로는 `Getter`와 `Setter` 모두 제공하지 않고, 꼭 필요한 별도의 메서드를 제공하는 것이 가장 이상적
  - 실무에서는 엔티티의 데이터를 조회할 일이 많기 때문에 `Getter`의 경우 모두 열어두는 것이 편리함
  - `Setter`의 경우 호출하면 데이터가 변경될 수 있어, 엔티티가 왜 변경되었는지 추적하기 어려움
- 엔티티 변경 시, `Setter` 대신에 변경 지점이 명확하도록 변경을 위한 비즈니스 메서드를 별도로 제공해야 함

**✔️ id**
- 엔티티의 식별자는 `id`를 사용하고 PK 컬럼명은 `member_id`를 사용
  - 엔티티는 타입이 있으므로 `id` 필드만으로도 쉽게 구분 가능
  - 테이블은 타입이 없으므로 구분이 어려움
- 테이블은 관례상 `테이블명 + id`를 많이 사용함
- 객체에서 `id` 대신에 `memberId`와 같이 사용해도 됨(중요한 것은 일관성)

**✔️ `@ManyToMany`**
- 실무에서는 사용하지 않도록 함
  - `@ManyToMany`는 중간 테이블에 컬럼을 추가할 수 없고, 세밀하게 쿼리를 실행하기 어려움
- 중간 엔티티를 만들고 `@ManyToOne`, `@OneToMany`로 매핑해서 사용
  - 다대다 매핑을 일대다, 다대일 매핑으로 풀어서 사용하기

**✔️ 값 타입**
- 값 타입은 변경 불가능하게 설계해야 함
- `@Setter`를 사용하지 않고, 생성자에서 값을 모두 초기화하여 변경 불가능한 클래스로 만들기
- JPA 스펙상 엔티티나 임베디드 타입(`@Embeddable`)은 자바 기본 생성자를 `public` 또는 `protected`로 설정해야 함
  - `public` 보다 `protected`로 설정하는 것이 그나마 더 안전함
  - JPA 구현 라이브러리가 객체를 생성할 때 리플랙션 같은 기술을 사용할 수 있도록 지원해야 하기 때문에 이러한 제약을 둠 

<br/>

### 엔티티 설계 시 주의점

**✔️ 엔티티에는 Setter를 사용하지 않기**
- Setter가 열려있으면 변경 포인트가 너무 많아 유지보수하기 어려움
- 나중에 리팩토링으로 Setter 제거

**✔️ 모든 연관관계는 지연로딩으로 설정**
- 즉시로딩(`EAGER`)은 예측이 어렵고, 어떤 SQL이 실행될지 추적하기 어려움
  - JPQL을 실행할 때 N+1 문제가 자주 발생함
- 실무에서 모든 연관관계는 지연로딩(`LAZY`)으로 설정해야 함
  - 연관된 엔티티를 함께 DB에서 조회해야 하면, fetch join 또는 엔티티 그래프 기능을 사용함
- @XToOne(`@OneToOne`, `@ManyToOne`) 관계는 기본이 즉시로딩이므로 직접 지연로딩으로 설정해야 함

**✔️ 컬렉션은 필드에서 초기화하기**
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
  
**✔️ 테이블, 컬럼명 생성 전략**
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

## 기능 구현

<br/>

### ◽ 회원 도메인 개발


#### 회원 리포지토리 개발

- `@Repository` : 스프링 빈으로 등록, JPA 예외를 스프링 기반 예외로 예외 변환 
- `@PersistenceContext` : 엔티티 메니저(`EntityManager`) 주입 
- `@PersistenceUnit` : 엔티티 메니터 팩토리(`EntityManagerFactory`) 주입

<br/>

#### 회원 서비스 개발
- `@Service`
- `@Transactional` : 트랜잭션, 영속성 컨텍스트
  - `readOnly=true` : 데이터의 변경이 없는 읽기 전용 메서드에 사용, 영속성 컨텍스트를 플러시 하지 않으므로 약간의 성능 향상(읽기 전용에는 다 적용)
  - 데이터베이스 드라이버가 지원하면 DB에서 성능 향상
- `@Autowired` : 생성자 Injection 많이 사용, 생성자가 하나면 생략 가능

> 실무에서는 검증 로직이 있어도 멀티 쓰레드 상황을 고려해서 회원 테이블의 회원명 컬럼에 유니크 제약 조건을 추가하는 것이 안전함

<br/> 

**스프링 필드 주입 대신에 생성자 주입을 사용**
- 생성자 주입 방식을 권장
- 변경 불가능한 안전한 객체 생성 가능
- 생성자가 하나면, `@Autowired` 생략 가능
- `final` 키워드를 추가하면 컴파일 시점에 `memberRepository`를 설정하지 않는 오류를 체크할 수 있음

> 스프링 데이터 JPA를 사용하면 `EntityManager`도 주입 가능

<br/>

#### 회원 기능 테스트

- 테스트 요구사항
  - 회원가입을 성공해야 함(회원가입 테스트)
  - 회원가입 할 때 같은 이름이 있으면 예외가 발생해야 함(중복 회원 예외처리 테스트)
- 기술
  - `@RunWith(SpringRunner.class)` : 스프링과 테스트 통합
  - `@SpringBootTest` : 스프링 부트 띄우고 테스트(해당 어노테이션이 없으면 `@Autowired` 다 실패)
  - `@Transactional`
    - 반복 가능한 테스트 지원
    - 각각의 테스트를 실행할 때마다 트랜잭션을 시작하고 테스트가 끝나면 트랜잭션을 강제로 롤백 
      - 해당 어노테이션이 테스트 케이스에서 사용될 때만 롤백
- 테스트 케이스를 위한 설정
  - 테스트는 케이스가 격리된 환경에서 실행하고, 끝나면 데이터를 초기화하는 것이 좋음
    - 이런 면에서 메모리 DB를 사용하는 것이 가장 이상적
  - 테스트 케이스를 위한 스프링 환경과 일반적으로 애플리케이션을 실행하는 환경은 보통 다르므로 설정 파일을 다르게 사용함
    - `test/resources/application.yml` : 테스트용 설정 파일 추가
    - 테스트에서 스프링을 실행하면 해당 위치에 있는 설정 파일을 읽음
  - 스프링부트는 데이터 소스나, JPA 관련된 별도의 추가 설정을 하지 않아도 됨
    - 스프링 부트는 `datasource` 설정이 없으면 기본적으로 메모리 DB를 사용 
    - `driver-class`도 현재 등록된 라이브러를 보고 찾아줌
    - `dl-auto`도 `create-drop` 모드로 동작함
  
<br/>

### ◽ 상품 도메인 개발

**상품 엔티티 개발(비즈니스 로직 추가)**
- `addStock()`
  - 파라미터로 넘어온 수만큼 재고를 늘림
  - 재고가 증가하거나 상품 주문을 취소해서 재고를 다시 늘려야 할 때 사용 
- `removeStock()`
  - 파라미터로 넘어온 수만큼 재고를 줄임
  - 만약 재고가 부족하면 예외가 발생하고, 주로 상품을 주문할 때 사용

**상품 레포지토리**
- `save()`
  - `id`가 없으면 신규로 보고 `persist()` 실행
  - `id`가 있으면 이미 DB에 저장된 엔티티를 수정한다고 보고 `merge()`를 실행

**상품 서비스**
- 상품 리포지토리에 단순히 위임만 하는 클래스

<br/>

### ◽ 주문 도메인 개발

#### 주문 엔티티 개발 
- 생성 메서드(`createOrder()`)
  - 주문 엔티티를 생성할 때 사용
  - 주문 회원, 배송정보, 주문 상품의 정보를 받아서 실제 주문 엔티티를 생성함
- 주문 취소(`cancel()`)
  - 주문 취소 시 사용
  - 주문 상태를 취소로 변경하고, 주문상품에 주문 취소를 알림
    - 만약 이미 배송을 완료한 상품이면 주문을 취소하지 못하도록 예외를 발생시킴
- 전체 주문 가격 조회
  - 주문 시 사용한 전체 주문 가격을 조회
  - 전체 주문 가격을 알려면 각각의 주문상품 가격을 알아야 함
    - 연관된 주문상품들의 가격을 조회해서 더한 값을 반환함

<br/>

#### 주문상품 엔티티 개발
- 생성 메서드(`createOrder()`)
  - 주문 상품, 가격, 수량 정보를 사용해서 주문상품 엔티티를 생성
  - `item.removeStock(count)`를 호출해서 주문한 수량만큼 상품의 재고를 줄임
- 주문 취소(`cancel()`)
  - `getItem().addStock(count)`를 호출해서 취소한 주문 수량만큼 상품의 재고를 증가시킴
- 주문 가격 조회(`getTotalPrice()`)
  - 주문 가격에 수량을 곱한 값을 반환

<br/>

#### 주문 레포지토리
- `save()`: 주문 엔티티 저장
- `findOne()`: 주문 엔티티 조회
- `findAll(OrderSearch)`: 주문 검색 기능

<br/>

#### 주문 서비스 개발
- 주문(`order()`)
  - 주문하는 회원 식별자, 상품 식별자, 주문 수량 정보를 받아서 실제 주문 엔티티 생성 후, 저장
- 주문 취소(`cancelOrder()`)
  - 주문 식별자를 받아서 주문 엔티티를 조회한 후, 주문 엔티티에 주문 취소를 요청함
- 주문 검색(`findOrders()`)
  - `OrderSearch`라는 검색 조건을 가진 객체로 주문 엔티티를 검색

<br/>

> **도메인 모델 패턴**  
> : 엔티티가 비즈니스 로직을 가지고 객체 지향의 특성을 적극 활용하는 것
> - 주문과 주문 취소 메서드를 보면 비즈니스 로직 대부분이 엔티티에 있음  
> - 서비스 계층은 단순히 엔티티에 필요한 요청을 위임하는 역할을 함

<br/>

#### 주문 기능 테스트

- 테스트 요구사항
  - 상품 주문이 성공해야 한다.
  - 상품을 주문할 때 재고 수량을 초과하면 안된다.
  - 주문 취소가 성공해야 한다.

<br/>

#### 주문 검색 기능

`JPA에서 동적 쿼리 처리`
- JPQL로 처리
  - 쿼리를 문자로 생성하기 번거롭고, 실수로 인한 버그가 충분히 발생할 수 있음
- JPA Criteria로 처리
  - JPA 표준 스펙이지만 실무에서 사용하기 너무 복잡함(다른 대안 필요)
  - 해결책: `Querydsl`

<br/>

### ◽ 웹 계층 개발

#### 폼 객체 vs 엔티티 직접 사용
- 요구사항이 정말 단순할 때는 폼 객체(`MemberForm`)없이 엔티티(`Member`)를 직접 등록과 수정 화면에서 사용해도 됨
- 하지만 화면 요구사항이 복잡해질 경우, 엔티티에 화면을 처리하기 위한 기능이 점점 증가됨
  - 엔티티는 점점 화면에 종속적으로 변하고, 화면 기능때문에 지저분해진 엔티티는 결국 유지보수하기 어려워짐
- 실무에서 **엔티티는 핵심 비즈니스 로직만 가지고 있고, 화면을 위한 로직은 없어야 함**
  - 화면이나 API에 맞는 폼 객체나 DTO를 사용하기
  - 화면이나 API 요구사항을 이것들로 처리하고, 엔티티는 최대한 순수하게 유지하도록 함

<br/>

#### 상품 수정

- 상품 수정 폼 이동
  1. 수정 버튼 클릭 시, `/items/{itemId}/edit` URL을 GET 방식으로 요청하고, `updateItemForm()` 메서드 실행
  2. `itemService.findOne(itemId)` 호출해서 수정할 상품을 조회
  3. 조회 결과를 모델 객체에 담아서 뷰(`items/updateItemForm`)에 전달
- 상품 수정 실행
  1. 상품 수정 폼에서 정보를 수정하고 폼 제출
  2. `/items/{itemId}/edit` URL을 POST 방식으로 요청하고, `updateItem()` 메서드 실행
  - 이때 컨트롤러에 파라미터로 넘어온 `item` 엔티티 인스턴스는 현재 준영속 상태
    - 영속성 컨텍스트의 지원을 받을 수 없고, 데이터를 수정해도 변경 감지 기능은 동작하지 않음

<br/>

### 변경 감지와 병합

> **정말 중요한 내용! 꼭 완벽하게 이해하고 넘어가기**

<br/>

#### 준영속 엔티티
- 영속성 컨텍스트가 더는 관리하지 않는 엔티티
  - DB에 한 번 갔다온 엔티티, DB에 한 번 저장되어 식별자가 존재하는 객체
  - 준영속 엔티티는 JPA가 관리하지 않기 때문에 객체를 수정해도 DB에 Update가 일어나지 않음
- `itemService.saveItem(book)`에서 수정을 시도하는 `Book` 객체는 준영속 엔티티임
  - `Book` 객체는 이미 DB에 한 번 저장되어서 식별자가 존재함
  - 임의로 만들어낸 엔티티도 기존 식별자를 가지고 있으면 준영속 엔티티로 볼 수 있음
- 준영속 엔티티를 수정하는 방법
  - 변경 감지 기능 사용
  - 병합(`merge`) 사용

<br/>

#### 변경 감지 기능 사용

- 영속성 컨텍스트에서 엔티티를 다시 조회한 후에 데이터를 수정하는 방법
  ```java
  @Transactional
  void update(Item itemParam) { // itemParam: 파리미터로 넘어온 준영속 상태의 엔티티
      Item findItem = em.find(Item.class, itemParam.getId()); // 같은 엔티티를 조회
      findItem.setPrice(itemParam.getPrice()); // 데이터를 수정
  }
  ```
  1. 트랜잭션 안에서 엔티티를 다시 조회 후, 변경할 값 선택
  2. 트랜잭션 커밋 시점에 변경 감지(`Dirty Checking`)가 동작해서 데이터베이스에 UPDATE SQL 실행

<br/>

#### 병합(`merge`) 사용

- 준영속 상태의 엔티티를 영속 상태로 변경할 때 사용하는 기능
  ```java
  @Transactional
  void update(Item itemParam) { //itemParam: 파리미터로 넘어온 준영속 상태의 엔티티
      Item mergeItem = em.merge(itemParam);
  }
  ```
  1. 준영속 엔티티의 식별자 값으로 영속 엔티티를 조회
  2. 영속 엔티티의 값을 준영속 엔티티의 값으로 모두 교체(병합함)
  3. 트랜잭션 커밋 시점에 변경감지 기능이 동작해서 데이터베이스에 UPDATE SQL 실행
- 병합 동작방식
  1. `merge()`를 실행함
  2. 파라미터로 넘어온 준영속 엔티티의 식별자 값으로 1차 캐시에서 엔티티를 조회함
     - 만약 1차 캐시에 엔티티가 없으면 데이터베이스에서 엔티티를 조회하고, 1차 캐시에 저장함
  3. 조회한 영속 엔티티(`mergeMember`)에 `member` 엔티티의 값을 채워 넣음
     - member 엔티티의 모든 값을 mergeMember에 밀어 넣음(이때 값 변경)
  4. 영속 상태인 mergeMember를 반환함

> 변경 감지 기능을 사용하면 원하는 속성만 선택해서 변경할 수 있지만, 병합을 사용하면 모든 속성이 변경됨  
> 병합 시, 값이 없으면 `null`로 업데이트할 위험도 있음(병압은 모든 필드를 교체함)

<br/>

**ItemRepository**

- 저장 메서드(`save()`)
  ```java
    private final EntityManager em;
  
    public void save(Item item) {
        if (item.getId() == null) {
            em.persist(item);
        } else {
            em.merge(item);
        }
    }
  ```
  - 식별자 값이 없으면(`null`) 새로운 엔티티로 판단해서 영속화(persist)하고, 식별자가 있으면 병합(merge)함
    - 식별자가 있으면 이미 한 번 영속화되었던 엔티티로 판단함
  - 지금처럼 준영속 상태인 상품 엔티티를 수정할 때는 `id` 값이 있으므로 병합 수행
  - `save()`는 식별자를 자동 생성해야 정상 동작함
    - `Item` 엔티티의 식별자는 자동으로 생성되도록 `@GeneratedValue`를 선언했음
      - 식별자 없이 해당 메서드를 호출하면 `persist()`가 호출하면서 식별자 값이 자동으로 할당됨
    - 식별자를 직접 할당하도록 `@Id`만 할 경우, 식별자를 직접 할당하지 않음
      - `save()` 메서드를 호출하면 식별자가 없는 상태로 `persist()`를 호출함
      - 식별자가 없다는 예외가 발생하게 됨

**새로운 엔티티 저장과 준영속 엔티티 병합을 편리하게 한 번에 처리**
- `ItemRepository`에서 `save()` 메서드 하나로 저장과 수정(병합)을 모두 처리함
  - 신규 데이터를 저장하는 것뿐만 아니라 변경된 데이터의 저장이라는 의미도 포함
  - 이 메서드를 사용하는 클라이언트는 저장과 수정을 구분하지 않아도 되므로 클라이언트 로직이 단순해짐
- `save()`에서 수정(병합)은 준영속 상태의 엔티티를 수정할 때 사용함
  - 영속 상태의 엔티티는 변경 감지(dirty checking)기능이 동작해서 트랜잭션을 커밋할 때 자동으로 수정됨(수정 메서드를 만들고 호출할 필요 x)

<br/>

> 실무에서는 보통 업데이트 기능이 매우 제한적 
> - 병합은 모든 필드를 변경해버리고, 데이터가 없으면 `null`로 업데이트 함  
> - 해당 문제를 해결하려면, 변경 폼 화면에서 모든 데이터를 항상 유지해야 함 
> - 실무에서는 보통 변경가능한 데이터만 노출하기 때문에, 병합을 사용하는 것이 더 번거로움

<br/>

### 가장 좋은 해결 방법

**엔티티를 변경할 때는 항상 변경 감지를 사용하기**
- 컨트롤러에서 어설프게 엔티티를 생성하지 않기
- 트랜잭션이 있는 서비스 계층에 식별자(`id`)와 변경할 데이터를 명확하게 전달하기(파라미터 or dto)
- 트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티를 조회하고, 엔티티의 데이터를 직접 변경하기
- 트랜잭션 커밋 시점에 변경 감지가 실행됨

<br/>

## API 개발

### 0. API 기본

`MemberApiController`

#### 회원 등록

1. 엔티티를 Request Body에 직접 매핑
   - 요청 값으로 `Member` 엔티티를 직접 받음
   - 문제점
     - 엔티티에 프레젠테이션 계층을 위한 로직이 추가됨
     - 엔티티에 API 검증을 위한 로직이 들어감 (`@NotEmpty` 등등)
     - 실무에서는 회원 엔티티를 위한 API가 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 모든 요청 요구사항을 담기는 어려움
     - 엔티티가 변경되면 API 스펙이 변함 
   - 결론
     - API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받기
2. 엔티티 대신에 별도의 DTO를 RequestBody에 매핑
   - `CreateMemberRequest`를 `Member` 엔티티 대신에 RequestBody와 매핑함
   - 엔티티와 프레젠테이션 계층을 위한 로직을 분리할 수 있음 
   - 엔티티와 API 스펙을 명확하게 분리할 수 있음 
   - 엔티티가 변해도 API 스펙이 변하지 않음
   - 실무에서는 엔티티를 API 스펙에 노출하면 안됨!

<br/>

#### 회원 조회

1. 응답 값으로 엔티티를 직접 외부에 노출
   - 문제점
     - 엔티티에 프레젠테이션 계층을 위한 로직이 추가됨
       - 기본적으로 엔티티의 모든 값이 노출되고, 응답 스펙을 맞추기 위한 로직이 따로 필요해짐 (`@JsonIgnore`, 별도의 뷰 로직 등등)
       - 실무에서는 같은 엔티티에 대해 API가 용도에 따라 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 프레젠테이션 응답 로직을 담기는 어려움
     - 엔티티가 변경되면 API 스펙이 변하게 됨
       - 추가로 컬렉션을 직접 반환하면 항후 API 스펙을 변경하기 어려움 (별도의 `Result` 클래스 생성으로 해결)
  - 결론
    - API 요청 스펙에 맞추어 별도의 `DTO`를를 반환하기 
2. 응답 값으로 엔티티가 아닌 별도의 DTO 사용
   - 엔티티를 DTO로 변환해서 반환하기 때문에 엔티티가 변해도 API 스펙이 변경되지 않음
   - 추가로 `Result` 클래스로 컬렉션을 감싸서 향후 필요한 필드를 추가할 수 있음
   
<br/>

> 지연 로딩으로 인해 발생하는 성능 문제를 단계적으로 해결하기

<br/>

### 1. 지연 로딩과 조회 성능 최적화

`OrderSimpleApiController`

#### 1) 엔티티를 직접 노출

- Entity 직접 노출시킬 경우, 양방향 관계에서 문제 발생
  - 주문 내역 조회를 위해 `Order`를 조회하게 되면 무한루프에 빠지게 됨
    - `Order`와 `Member`는 다대일 관계이고, `Member`와 `Order`는 일대다 관계
    - 결국 `Order → Member → Order → ... `를 무한 반복하게 됨
- 이렇게 양방향 연관관계 무한 순회를 방지하기 위한 해결책으로 `@JsonIgnore`를 사용할 수 있음
  - 다시 양방향이 걸리는 부분을 모두 `@JsonIgnore` 처리를 함
    ```java
    /* Member.java */
    public class Member {
        ...
        @JsonIgnore
        @OneToMany(mappedBy = "member")
        private List<Order> orders = new ArrayList<>();
    }
    ```
- 하지만 이 경우에도 `org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor`에서 `Type definition` 에러가 발생함
  ```java
  public class Order {
      ...
      @ManyToOne(fetch = FetchType.LAZY)
      @JoinColumn(name = "member_id")
      private Member member; // member = new ByteBuddyInterceptor();
  }
  ```
  - `Order`에서 `Member`에 대한 전략이 `FetchType.LAZY`로 지연 로딩으로 되어 있음
    - 지연 로딩은 실제 엔티티가 아닌 프록시 객체를 가지기 때문에 실제 `Member` 객체를 가지고 있지 않음
  - 이때 Json 라이브러리가 `Order`를 `Json`으로 변환시킬 때, `member`가 순수한 `Member` 클래스가 아니기 때문에 변환시켜 줄 수 없다는 문제가 발생함
    - 기본적으로 `jackson` 라이브러리는 프록시 객체를 Json으로 어떻게 생성해야 하는지 모름 → **예외 발생**
- `Hibernate5Module`을 스프링 빈으로 추가하여 Json 라이브러리가 아무것도 하지 않도록 명시해주면 해결할 수 있음
  ```java
  /** build.gradle **/
  implementation 'com.fasterxml.jackson.datatype:jackson-datatype-hibernate5'
  ```
  - 스프링 부트 3.0 이상의 경우 `Hibernate5JakartaModule`을 등록해야 함
  ```java
  public class JpashopApplication {
      @Bean
      Hibernate5Module hibernate5Module() {
          return new Hibernate5Module();
      }
  }
  ```
  - 기본적으로 초기화된 프록시 객체만 노출하고, 초기화되지 않은 프록시 객체는 노출하지 않음
  ```java
  public class JpashopApplication {
      @Bean
      Hibernate5Module hibernate5Module() {
          Hibernate5Module hibernate5Module = new Hibernate5Module(); //강제 지연 로딩 설정
          hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);
          return hibernate5Module;
      }
  }
  ```
  - 다음과 같이 설정하면 강제로 지연로딩이 가능해짐
  - 해당 옵션을 키게 될 경우 양방향 연관관계를 계속 로딩(`order → member → orders`)되기 때문에 `@JsonIgnore` 옵션을 한 곳에 설정 해주어야 함

<br/>

#### ❗ 중요 
- Entity를 직접 노출시킬 경우는 위의 방식처럼 사용이 가능함(단, 한 곳은 무조건 `@JsonIgnore` 처리)
- 하지만 Entity를 API 응답으로 외부에 직접 노출하는 방법은 좋지 않음
- 따라서 `Hibernate5Module`을 사용하기 보다는 `DTO`로 변환해서 반환하는 것이 더 좋은 방법!
- 또한, 지연 로딩을 피하기 위해서 즉시 로딩으로 설정하면 절대 안됨
  - 즉시 로딩 때문에 연관관계가 필요없는 경우에도 데이터를 항상 조회해서 성능문제가 발생할 수 있고, 성능 튜닝이 매우 어려워짐
  - 그래서 항상 지연 로딩을 기본으로 하고, 성능 최적화가 필요한 경우에는 `fetch join`을 사용하기

<br/>

#### 2) 엔티티를 DTO로 변환

- 엔티티를 DTO로 변환하는 일반적인 방법
  ```java
  public class OrderSimpleApiController {
      @GetMapping("/api/v2/simple-orders")
      public List<SimpleOrderDto> ordersV2() {
          // N + 1 문제 -> 1 + 회원 N + 배송 N
          List<Order> orders = orderRepository.findAllByString(new OrderSearch());
          List<SimpleOrderDto> result = orders.stream()
                  .map(SimpleOrderDto::new)
                  .collect(Collectors.toList());
          return result;
      }
  }
  ```
- 지연로딩으로 인해 쿼리가 총 `1 + N + N`번 실행되는 문제가 발생(`N + 1 문제`)
  - `order` 조회 1번으로 `SimpleOrderDto`에 있는 `Member`와 `Delivery`의 지연 로딩 조회가 각각 `N`번이 실행됨
    - `N`: `order` 조회 결과 수
  - ex) orders의 결과가 2개라면 최악의 경우 1 + 2 + 2번 실행됨
- 결국 Order 조회가 많아질수록 쿼리 수행이 많아지고 성능 저하가 일어날 수 있음
  - `Fetch Join`으로 성능을 개선할 수 있음

<br/>

#### 3) 엔티티를 DTO로 변환 - 페치 조인 최적화

[N + 1 문제 참고](https://github.com/jmxx219/SpringJPA/blob/main/orm-jpa/%ED%94%84%EB%A1%9D%EC%8B%9C%EC%99%80%20%EC%97%B0%EA%B4%80%EA%B4%80%EA%B3%84%20%EA%B4%80%EB%A6%AC.md#%ED%94%84%EB%A1%9D%EC%8B%9C%EC%99%80-%EC%A6%89%EC%8B%9C-%EB%A1%9C%EB%94%A9-%EC%A3%BC%EC%9D%98)

- Entity를 JPQL의 페키 조인(fetch join)을 이용하면 쿼리 1번으로 모두 조회가 가능함 
  ```java
  public class OrderRepository {
      public List<Order> findAllWithMemberDelivery() {
          return em.createQuery(
                      "select o from Order o" +
                              " join fetch o.member m" +
                              " join fetch o.delivery d", Order.class)
              .getResultList();
      }
  }
  ```
  - 페치 조인으로 `order → member`, `order → delivery`가 이미 조회된 상태이기 때문에 지연 로딩이 발생하지 않음
  - 즉, order에 대한 정보를 가져올 때 fetch join한 member와 delivery에 대한 데이터도 한 번에 가져옴

<br/>

#### 4) JPA에서 DTO로 바로 조회

- 일반적인 SQL을 사용할 때 처럼 원하는 값을 선택해서 조회함
  ```java
  public List<OrderSimpleQueryDto> findOrderDtos() {
      return em.createQuery(
      "select new japbook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
              " from Order o" +
              " join o.member m" +
              " join o.delivery d", OrderSimpleQueryDto.class)
      .getResultList();
  }
  ```
  - Join Fetch를 사용하지 않고, `new` 연산자를 사용해서 DTO를 select 절에 명시하여 JPQL의 결과를 DTO로 즉시 변환함
    - 대신 full package path를 입력해야 함
  - `SELECT` 절에서 원하는 데이터를 직접 선택하므로 DB 애플리케이션 네트웍크 용량 최적화(생각보다 미비함)
    - `SELECT` 절에 필드 몇 개 추가된다고 성능이 저하되지 않음
    - 성능 이슈의 대부분은 `JOIN` 절에서 발생함
  - 리포지토리 재사용성 떨어짐
    - API 스펙에 맞춘 코드가 리포지토리에 들어간다는 단점 존재
    - 리포지토리는 원래 엔티티에 대한 조회 용도로 사용되어야 하는데, API 스펙에 맞춘 코드가 작성됨
    - 논리적으로 API 스펙이 repository까지 의존되어 API 스펙이 변경되면 repository 내부도 모두 수정해야 함
  - 추가적으로 DTO용 repository를 따로 만들어서 해결할 수 있음(`OrderSimpleQueryRepository`, `OrderSimpleQueryDto`)
  
<br/>

#### ❗쿼리 방식 선택 권장 순서
> 엔티티를 DTO로 변환하거나, DTO로 바로 조회하는 두가지 방법 각각의 장단점이 있기 때문에 상황에 따라 선택하면 됨  
> 하지만 엔티티로 조회하면 리포지토리 재사용성도 좋고, 개발이 단순해지기 때문에 다음과 같이 권장함
1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다.
2. 필요하면 페치 조인으로 성능을 최적화하면 대부분의 성능 이슈가 해결된다.
3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다.
4. 최후의 방법은 JPA가 제공하는 `네이티브 SQL`이나 `스프링 JDBC Template`을 사용해서 SQL을 직접 사용한다.




