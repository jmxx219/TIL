# API 개발과 성능 최적화

### 목차

- [API 기본](#API-기본)
- [지연 로딩과 조회 성능 최적화](#지연-로딩과-조회-성능-최적화)
- [컬렉션 조회 최적화](#컬렉션-조회-최적화)
- [실무 필수 최적화](#실무-필수-최적화)
- [OSIV와 성능 최적화](#OSIV와-성능-최적화)



## API 기본

`MemberApiController`

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

`OrderSimpleApiController`

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


