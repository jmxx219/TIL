# API 개발과 성능 최적화

### 목차

- [API 기본](#API-기본)
- [지연 로딩과 조회 성능 최적화](#지연-로딩과-조회-성능-최적화)
- [컬렉션 조회 최적화](#컬렉션-조회-최적화)
- [OSIV와 성능 최적화](#OSIV와-성능-최적화)

<br/>

## API 기본

> `MemberApiController`

### 회원 등록

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

### 회원 조회

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
<br/>

## 지연 로딩과 조회 성능 최적화

> `OrderSimpleApiController`, `N:1`과 `1:1` 관계를 조회하고 최적화함

### 1) 엔티티를 직접 노출

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

### 2) 엔티티를 DTO로 변환

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

### 3) 엔티티를 DTO로 변환 - 페치 조인 최적화

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

### 4) JPA에서 DTO로 바로 조회

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

<br/>
<br/>

## 컬렉션 조회 최적화

> `OrderApiController`, 컬렉션인 `1:N` 관계를 조회하고 최적화함

### 1) 엔티티 직접 노출
- `item`과 `orderItem`의 상세 정보를 조회하기 위해서 Lazy로 설정되어 있는 연관관계는 프록시의 강제 초기화가 필요함
    ```java
        @GetMapping("/api/v1/orders")
        public List<Order> ordersV1() {
            List<Order> all = orderRepository.findAllByString(new OrderSearch());
            for (Order order : all) {
                order.getMember().getName(); // Lazy 강제 초기화
                order.getDelivery().getAddress(); // Lazy 강제 초기환
                List<OrderItem> orderItems = order.getOrderItems();
                orderItems.stream().forEach(o -> o.getItem().getName()); //Lazy 강제 초기화
            }
            return all;
        }
    ```
- 양방향 관계의 경우, 한 곳에 `@JsonIgnore`를 꼭 추가해야 함
- 엔티티를 직접 노출하기 때문에 좋은 방법은 아님


<br/>


### 2) 엔티티를 DTO로 변환

- `Order`를 `OrderDto`로 변경하고, `Order`에 있던 `OrderItem`을 `OrderItemDto`로 변환함
    ```java
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return result;
    }
    ```
    - DTO로 변환할 때 엔티티의 의존을 완전히 끊기 위해서 DTO 안에 있는 엔티티도 DTO로 변환해야 함
- 지연 로딩으로 인해 너무 많은 SQL이 실행됨(`N + 1 문제`)
  - SQL 실행 수
    - order 1번 
    - member , address N번(order 조회 수 만큼) 
    - orderItem N번(order 조회 수 만큼)
    - item N번(orderItem 조회 수 만큼)


<br/>

### 3) 엔티티를 DTO로 변환 - 페치 조인 최적화

- 쿼리가 너무 많이 나가는 문제를 페치 조인으로 해결
    ```java
    List<Order> orders = orderRepository.findAllWithItem();
    ```
    ```java
    public List<Order> findAllWithItem() {
        return em.createQuery(
        "select distinct o from Order o" +
                " join fetch o.member m" +
                " join fetch o.delivery d" +
                " join fetch o.orderItems oi" +
                " join fetch oi.item i", Order.class)
            .getResultList();
    }
    ```

<br/>

#### 페치 조인 사용 시, 문제점
1. 중복되는 데이터 발생
   - 페치 조인을 하게 되면 DB에서는 join과 같기 때문에 `order_item`의 개수만큼 데이터가 늘어나게 됨
       - `order`가 2개이고, `order_item`이 2개라면 총 4개의 행 결과가 나옴
   - JPA에서는 PK가 같으면 같은 참조값을 가지기 때문에 늘어난 데이터는 레퍼런스도 똑같음
       - 결국 똑같은 데이터가 중복해서 조회됨
       - 따라서 `distinct` 키워드로 `1:N 관계`에서 발생하는 데이터 중복을 제거할 수 있음
   - JPA의 `distinct`는 DB의 `distinct`과 동일하게 작동하고, 추가적으로 엔티티가 중복인 id 값을 가진다면 애플리케이션에서 중복을 제거해줌
     - DB의 `distinct`는 모든 값이 완전히 일치해야만 중복을 제거하지만, JPA는 id가 같다면 중복으로 보고 제거해줌
2. 컬렉션 페치 조인을 사용하면 페이징이 불가능함
   - `일대다(1:N)`에서 `일(1)`기준으로 페이징을 하는 것이 목적인데, 데이터는 `다(N)`기준으로 생성됨
   - JPA의 `distinct`를 사용하여 중복된 데이터를 제거하더라도 DB에서는 모든 값이 일치하지 않는 이상 데이터 중복이 제거 되지 않음
     - 원하는 것은 Order 값 2개 기준으로 페이징 하는 것인데, DB에서는 데이터가 뻥튀기 되어서 4개의 Order가 존재함
     - 따라서 결국에는 Order(`1`)가 아닌 OrderItem(`N`) 기준으로 페이징을 하게 되는 것과 같음
   - 결국 모든 데이터를 DB에서 읽어와서 메모리 상에서 페이징 처리를 하게 됨(매우 위험함)
     - 이때, 데이터가 무수히 많은 경우 OutOfMemory가 발생하면서 큰 장애가 발생할 수 있음
3. 여러 개의 컬렉션일 경우, 페치 조인을 사용할 수 없음(컬렉션 페치 조인은 1개만 사용 가능)
   - 컬렉션 둘 이상에 페치 조인을 사용하면 데이터의 중복 제거가 애매해지고, 데이터가 부정합하게 조회될 수 있음


<br/>

### 4) 엔티티를 DTO로 변환 - 페이징과 한계 돌파(권장)

1. ToOne(OneToOne, ManyToOne) 관계는 모두 페치조인 함
   - ToOne 관계는 row 수를 증가시 키지 않기 때문에 페이징 쿼리에 영향을 주지 않음
   - ex) `order-member`, `order-delivery`
   - `Delivery`, `Member`는 `Order`와 `1:1` 관계이기 때문에 페치 조인을 통해 한 번에 가져옴
       ```java
       public List<Order> findAllWithMemberDelivery(int offset, int limit) {
           return em.createQuery(
                   "select o from Order o" +
                   " join fetch o.member m" +
                   " join fetch o.delivery d", Order.class)
               .setFirstResult(offset)
               .setMaxResults(limit)
               .getResultList();
       }
       ```
2. 컬렉션(OntToMany)은 지연 로딩으로 조회함
   - ex) `order-orderItem`
   - `OrderItem`은 `1:N` 관계이기 때문에 페이징 처리를 할 수 없음. 따라서 페치 조인을 하지 않고 페치 조인을 통해 가져온 결과에서 지연 로딩으로 조회함
       - 즉, 컬렉션은 지연 로딩해서 가져오는데 이때 `N + 1 문제`가 발생함(`Order → OrderItem → Item` = `1 : N : M`)
3. 이때 지연 로딩 최적화를 위해서  `hibernate.default_batch_fetch_size`, `@BatchSize`를 적용함
   - `hibernate.default_batch_fetch_size`: 글로벌 설정
     ```yml
     spring:
       jpa:
         properties:
           hibernate:
             # 미리 in 절로 땡겨 올 데이터 개수
             default_batch_fetch_size: 1000
     ```
   - `@BatchSize`: 개별 최적화
     - `toMany` 관계의 경우, 엔티티의 컬렉션 위에 적용
     - `toOne` 관계인 경우, 클래스 레벨에 적용
   - 이 옵션을 사용하면 컬렉션이나, 프록시 객체를 한꺼번에 설정한 size 만큼 `IN` 쿼리로 조회한

<br/>

#### 장점

- 쿼리 호출 수가 `1+N` ➜ `1+1`로 최적화됨
  - `Order → OrderItem → Item` = `1 : 1 : 1`로 쿼리가 나감
- 조인보다 DB 데이터 전송량이 최적화 됨
  - Order와 OrderItem을 조인하면 Order가 OrderItem 만큼 중복해서 조회됨
  - 이 방법은 각각 조회하므로 전송해야할 중복 데이터가 없음
- 페치 조인 방식과 비교해서 쿼리 호출 수가 약간 증가하지만, 중복이 제거되어 DB 데이터 전송량이 감소함
  - 조회한 데이터가 정규화된 것처럼 중복이 없는 상태로 가져오는 것이 가능함
- 또한, 컬렉션 페치 조인은 페이징이 불가능 하지만 이 방법은 페이징이 가능함

<br/>

#### default_batch_fetch_size

- 크기는 적당한 사이즈로 골라야 하는데 대체로 100 ~ 1000 사이를 선택 하는 것을 권장함
  - 1000개 이상은 DB에 순간 부하가 증가할 수 있어 사용하지 않음
  - DB에 따라 IN 절 파라미터를 1000으로 제한하기도 함
- 하지만 애플리케이션은 100이든 1000이든 결국 전체 데이터를 돌면서 로딩해야 함
  - 따라서 WAS 입장에서는 메모리 사용량이 같음
- 1000으로 설정하는 것이 성능상 가장 좋지만, DB와 애플리케이션 모두 순간 부하를 견딜 수 있는 값으로 결정함

<br/>

> ❗ 결론  
> `ToOne` 관계는 페치 조인해도 페이징에 영향을 주지 않기 때문에  ToOne 관계는 페치조인으로 쿼리 수를 줄이고, 나머지는 hibernate.default_batch_fetch_size로 최적화 한다.


<br/>

### 5) JPA에서 DTO 직접 조회

- `ToOne(N:1, 1:1)` 관계들을 먼저 조회하고, `ToMany(1:N)` 관계는 각각 별도로 처리함
    - `ToOne` 관계는 조인해도 데이터 row 수가 증가하지 않고,`ToMany(1:N)` 관계는 조인하면 row 수가 증가하기 때문
- Query: 루트 1번, 컬렉션 N 번 실행
    ````java
    public List<OrderQueryDto> findOrderQueryDtos() {
        //루트 조회(toOne 코드를 모두 한번에 조회)
        List<OrderQueryDto> result = findOrders(); //Query 1번 -> 결과 N개
    
        //루프를 돌면서 컬렉션 추가(추가 쿼리 실행)
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId()); //Query N번
            o.setOrderItems(orderItems);
        });
    
        return result;
    }
    ````
    - `OrderItems`는 컬렉션 타입(N개)이기 때문에 JPQL의 `new` 키워드를 이용해 데이터를 바로 넣는 것이 불가능함
    - 루트 쿼리(`Order`) 1번 실행으로 컬렉션(`OrderItem`)을 조회하는 쿼리가 N번 실행되어 `N + 1 문제`가 발생함
- row 수가 증가하지 않는 `ToOne` 관계는 조인으로 최적화 하기 쉬우므로 한번에 조회함
  ```java
  private List<OrderQueryDto> findOrders() {
      return em.createQuery(
      "select new japbook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
              " from Order o" +
              " join o.member m" +
              " join o.delivery d", OrderQueryDto.class)
          .getResultList();
  }
  ```
  - `1:N 관계(컬렉션)`를 제외한 나머지를 한번에 조회
- `ToMany` 관계는 최적화 하기 어려우므로 별도의 메서드로 조회함
    ````java
    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
            "select new japbook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                    " from OrderItem oi" +
                    " join oi.item i" +
                    " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }
    ````
  - `1:N` 관계인 `orderItems` 조회


<br/>

### 6) JPA에서 DTO 직접 조회 - 컬렉션 조회 최적화



- `ToOne` 관계들을 먼저 조회하고, 여기서 얻은 식별자 `orderId`로 `ToMany` 관계인 `OrderItem`을 한꺼번에 조회함
- Query: 루트 1번, 컬렉션 1번
    ```java
    public List<OrderQueryDto> findAllByDto_optimization() {
        List<OrderQueryDto> result = findOrders(); //Query 1번(ToOne 조회)
    
        // OrderId 조회
        List<Long> orderIds = result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
    
        // Query 1번
        List<OrderItemQueryDto> orderItems = em.createQuery(
                "select new japbook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                " from OrderItem oi" +
                " join oi.item i" +
                " where oi.order.id in :orderIds", OrderItemQueryDto.class)
            .setParameter("orderIds", orderIds)
            .getResultList();
    
        // 메모리에 세팅
        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
            .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
        
        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));
    
        return result;
    }
    ```
- 컬렉션 조회 시, `IN`절로 여러 `orderId`에 대한 `orderItem`을 한 번에 가져와, 메모리 상에서 값을 매칭해 가져옴
  - 이때 `Map`을 사용해서 매칭하여 성능이 향상됨(`O(1)`)


<br/>

### 7) JPA에서 DTO 직접 조회, 플랫 데이터 최적화

- 쿼리 한 번으로 데이터를 모두 가져오기 위해 조회하려는 테이블을 한 번에 모두 조인해서 쿼리를 날림
- Query: 1번
    ```java
    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery(
            "select new japbook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                    " from Order o" +
                    " join o.member m" +
                    " join o.delivery d" +
                    " join o.orderItems oi" +
                    " join oi.item i", OrderFlatDto.class)
            .getResultList();
    }
    ```
    - 모든 데이터를 join해서 조회하면 중복 데이터를 반환함
    - Order가 아닌 OrderItem이 기준이 되기 때문에 페이징은 불가능함
- 한 번에 새로운 데이터를 가져오기 위해 DTO 생성
  ```java
  public class OrderFlatDto {
      private Long orderId;
      private String name;
      private LocalDateTime orderDate;
      private OrderStatus orderStatus;
      private Address address;
    
      // OrderItem
      private String itemName;
      private int orderPrice;
      private int count;
  }
  ```
- API 스펙인 `OrderQueryDto`로 맞추기 위해서 추가적인 작업으로 중복을 제거해서 변환할 수 있음
    ```java
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
        return flats.stream().collect(
                    groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                    mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList()))
                ).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(),e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }
    ```
  - `@EqualsAndHashCode(of = "orderId")`

<br/>

#### 단점
- 쿼리는 1번이지만, 조인이 많아지기 때문에 데이터의 중복이 추가되어 성능이 더 안좋아질 수 있음
- 또한, 중복 데이터를 모두 가져오기 때문에 API 스펙에 맞게 복잡한 추가적인 작업을 진행해야 함
- 페이징이 불가능함
  - `N`을 기준으로 페이징을 처리하는 것은 가능할 수 있지만, `1`을 기준으로 페이징하는 것은 불가능함
  - 데이터가 중복되기 때문에 정확한 페이징 결과가 나오지 않음

<br/>

### ❗ API 개발 고급 정리

- 엔티티 조회
  - v1: 엔티티를 조회해서 그대로 반환 
    - 사용하지 않는걸 권장함
  - v2: 엔티티 조회 후 DTO로 변환
    - 여러 테이블을 조인할 때 성능이 잘 나오지 않음
  - v3: 페치 조인으로 쿼리 수 최적화 
  - v3.1: 컬렉션 페이징과 한계 돌파
    - 컬렉션은 페치 조인시 페이징이 불가능
    - ToOne 관계는 페치 조인으로 쿼리 수 최적화
    - 컬렉션은 페치 조인 대신에 지연 로딩을 유지하고, `hibernate.default_batch_fetch_size`나 `@BatchSize`로 최적화
- DTO 직접 조회
  - v4: JPA에서 DTO를 직접 조회
    - 코드가 단순함
    - 특정 주문 한 건만 조회하면 성능이 잘 나옴
  - v5: 컬렉션 조회 최적화 - 일대다 관계인 컬렉션은 IN 절을 활용해서 메모리에 미리 조회해서 최적화
    - 코드가 복잡함
    - 여러 주문을 한꺼번에 조회하는 경우, v4 대신에 v5 방식을 사용해야 함
    - 보통 DTO를 직접 조회할 때는 V5 방식을 많이 사용함
  - v6: 플랫 데이터 최적화 - JOIN 결과를 그대로 조회 후 애플리케이션에서 원하는 모양으로 직접 변환
    - 쿼리 한 번으로 최적화되어서 상당히 좋아보이지만, Order를 기준으로 페이징이 불가능함
    - 실무에서 이 정도 데이터면 수백이나 수천건 단위로 페이징 처리가 꼭 필요하기 때문에 선택하기 어려운 방법임
    - 또한, 데이터가 많은면 중복 전송이 증가해서 v5와 비교해서 성능 차이도 미비함

<br/>

#### 권장 순서
1. 엔티티 조회 방식으로 우선 접근
   1. 페치조인으로 쿼리 수를 최적화 
   2. 컬렉션 최적화
      1. 페이징 필요 ➜ `hibernate.default_batch_fetch_size`, `@BatchSize`로 최적화
      2. 페이징 필요 x ➜ 페치 조인 사용
2. 엔티티 조회 방식으로 해결이 안되면 DTO 조회 방식 사용
3. DTO 조회 방식으로 해결이 안되면 `NativeSQL` or `스프링 JdbcTemplate`

<br/>

#### 참고
- Entity 조회 방식
  - 거의 코드를 거의 수정하지 않음
    - `fetch join`, `hibernate.default_batch_fetch_size`, `@BatchSize` 등 옵션만 약간 변경해서 다양한 성능 최적화를 시도할 수 있음
  - JPA가 많은 부분을 최적화해주기 때문에 단순한 코드를 유지하면서 성능을 최적화할 수 있음
  - Entity 조회 방식으로 대부분의 문제가 해결됨
    - 해결이 안되는 문제는 정말 트래픽이 많은 것이므로 캐시 등 다른 방법을 생각해봐야 함
    - 참고로 Entity는 영속성 컨텍스트에서 관리되는 상태가 있기 때문에 캐싱하면 안되고, Entity를 DTO로 변환한 값을 캐시해야 함
- DTO 직접 조회 방식
  - 성능을 최적화하거나 성능 최적화 방식을 변경할 때 많은 코드를 변경해야 함
  - SQL을 직접 다루는 것과 유사하기 때문에 성능 최적화와 코드 복잡도 사이에서 줄타기를 해야함


<br/>
<br/>
