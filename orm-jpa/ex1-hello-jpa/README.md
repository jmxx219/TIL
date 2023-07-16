# JPA 시작


## 프로젝트 생성

- Language : JDK 1.8
- Build Tool : Maven
  - 자바 라이브러리, 빌드 관리
  - 라이브러리 자동 다운로드 및 의존성 관리
  - 최근에는 Gradle을 많이 사용함
- DB : [H2 Databse](http://www.h2database.com) 1.4.2.00
  - 최고의 실습용 DB
  - 용량이 적어 가벼움
  - 브라우저에서 쿼리 실행 가능
  - MySQL, Oracle DB 시뮬레이션 기능
  - 시퀀스, AUTO INCREMENT 기능 지원
- JPA : Hibernate 5.4.32.Final

<br/>

### 라이브러리 추가

`pom.xml`
```xml
<dependencies>
    <!-- JPA 하이버네이트 -->
    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-entitymanager</artifactId>
        <version>5.3.10.Final</version>
    </dependency>

    <!-- H2 데이터베이스 -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>1.4.200</version>
    </dependency>
</dependencies>
```

<br/>

### JPA 설정

`persistence.xml`
- JPA 설정 파일
  - `src/main/resources/META-INF`에 위치(생성)
- `persistence-unit`의 `name` 속성에 이름을 지정하여 영속성 유닛 등록
  - `javax.persistence`로 시작: JPA 표준 속성 추가
  - `hibernate`로 시작: 하이버네이트 전용 속성 추가
  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <persistence version="2.2"
               xmlns="http://xmlns.jcp.org/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd">
      <persistence-unit name="hello">
          <properties>
              <!-- 필수 속성 -->
              <property name="javax.persistence.jdbc.driver" value="org.h2.Driver"/>
              <property name="javax.persistence.jdbc.user" value="sa"/>
              <property name="javax.persistence.jdbc.password" value=""/>
              <property name="javax.persistence.jdbc.url" value="jdbc:h2:tcp://localhost/~/test"/>
              <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
  
              <!-- 옵션 -->
              <property name="hibernate.show_sql" value="true"/>
              <property name="hibernate.format_sql" value="true"/>
              <property name="hibernate.use_sql_comments" value="true"/>
              <!--<property name="hibernate.hbm2ddl.auto" value="create" />-->
          </properties>
      </persistence-unit>
  </persistence>
  ```
  - 하이버네이트는 40가지 이상의 데이터베이스 방언을 지원함
    - `hibernate.dialect` 속성에 지정
      - H2: `org.hibernate.dialect.H2Dialect`
      - Oracle 10g: `org.hibernate.dialect.Oracle10gDialect`
      - MySQL: `org.hibernate.dialect.MySQL5InnoDBDialect`
  - `hibernate.dialect` 속성은 각 DBMS마다 기능은 같거나 비슷하지만 명칭이 다름
  
**영속성 유닛(Persistence Unit**
- 일반적으로 연결할 데이터베이스당 하나의 영속성 유닛을 등록함
- 영속성 유닛에는 반드시 공유한 이름을 부여해야 함
  
**데이터베이스의 방언**
- JPA는 특정 데이터베이스에 종속되지 않음
- 각각의 데이터베이스가 제공하는 SQL 문법과 함수는 조금씩 다름
  - 가변 문자: MySQL -`VARCHAR`, Oracle - `VARCHAR2`
  - 문자열을 자르는 함수: SQL 표준 - `SUBSTRING()`, Oracle - `SUBSTR()`
  - 페이징: MySQL - `LIMIT` , Oracle - `ROWNUM`
- 방언: SQL 표준을 지키지 않는 특정 데이터베이스만의 고유한 기능


<br/>

## JPA 구동 방식


### 객체와 테이블을 생성하고 매핑
```Java
@Entity // JPA가 관리할 객체
public class Member {
    
    @Id // DB의 PK와 매핑
    private Long id;
    private String name;
    
    //Getter, Setter ...
}
```
```
CREATE TABLE MEMBER (
    ID BIGINT NOT NULL,
    NAME VARCHAR(255),
    PRIMARY KEY (ID)
);
```

<br/>

### JPA 동작 과정

1. Persistence의 설정 정보 조회
   - JPA의 Persistence 클래스에서 `META-INF/persistence.xml` 설정 정보를 읽음
2. EntityMangerFactory 객체 생성
  - Persistence가 조회한 설정 정보로 엔티티 매니저 팩토리를 생성함
    - `EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");`
  - EntityMangerFactory는 애플리케이션 전체에서 딱 한 번만 생성하고 공유해서 사용해야 함 
    - 웹 서버가 올라올 시점에 DB당 하나만 생성
3. EntityManger 생성
   - EntityMangerFactory에서 필요할 때마다 EntityManger를 생성해서 사용
     - `EntityManager em = emf.createEntityManager();`
   - EntityManger는 DB 커넥션과 밀접한 관계가 있어 스레드간에 공유하거나 재사용하면 안됨(쓰고 버림)
4. 종료
   - 사용이 끝난 EntityMangerFactory와 EntityManger는 애플리케이션을 종료할 때 반드시 종료시켜줌

```java
EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

EntityManager em = emf.createEntityManager();

EntityTransaction tx = em.getTransaction(); //[트랜잭션] - 획득

try {
    tx.begin();     // [트랜잭션] - 시작
        
    // 비즈니스 로직 실행
    Member member = new Member();
    member.setId(1L);
    member.setName("helloA");

    em.persist(member);
        
    tx.commit();    // [트랜잭션] - 커밋
} catch (Exception e) {
    tx.rollback();  // [트랜잭션] - 롤백
} finally {
    em.close();     // [엔티티 매니저] - 종료
}
emf.close(); // [엔티티 매니저 팩토리] - 종료
```
- 트랜잭션 관리
  - JPA의 모든 데이터 변경은 트랜잭션 안에서 실행되어야 함
    - 트랜잭션 없이 데이터를 변경하면 예외가 발생함
  - 기본적으로 비즈니스 로직이 정상 동작하면 트랜잭션을 커밋하고, 예외가 발생하면 롤백함
- 비즈니스 로직
  - 한 건 조회: `Member member = em.find(Member.class, id);`
  - 등록: `em.persist(member);`
  - 삭제: `em.remove(member);`
  - 수정: `member.setName("member01");`
    - JPA의 수정은 `em.update()`나 `em.persist()` 호출없이도 반영됨
    - JPA는 어떤 엔티티가 변경되었는지 추적하는 기능을 가지고 있음
  - 하나 이상 조회: `List<Member> result = em.createQuery("select m from Member m", Member.class).getResultList();`
    - `select m from Member m` 쿼리는 SQL과 유사한데, JPA에서 제공하는 JPQL임

<br/>


### JPQL(Java Persistence Query Language)

- JPA를 사용하면 엔티티 객체를 중심으로 개발함
- 문제는 검색 쿼리
  - 검색을 할 때도 테이블이 아닌 엔티티 객체를 대상으로 검색함
  - 모든 DB 데이터를 객체로 변환해서 검색하는 것은 불가능함
  - 애플리케이션에 필요한 데이터만 DB에서 불러오려면 결국 검색 조건이 포함된 SQL이 필요함
- JPA에서는 SQL을 추상화한 JPQL이라는 객체 지향 쿼리를 제공함

**JPQL**
- 객체 지향 SQL
- 테이블이 아닌 객체를 대상으로 검색하는 객체지향 쿼리
- SQL을 추상화해서 특정 DB에 의존하지 않음
- JPQL은 JPA를 사용하면서 검색 조건이 포함된 SQL이 필요할 때 사용함

**JPQL과 SQL**
- JPQL은 SQL과 문법이 거의 유사해서 SELECT, FROM, WHERE, GROUP BY, HAVING, JOIN 등을 사용할 수 있음
- 차이
  - JPQL은 `엔티티 객체`를 대상으로 쿼리
  - SQL은 `데이터베이스 테이블`을 대상으로 쿼리