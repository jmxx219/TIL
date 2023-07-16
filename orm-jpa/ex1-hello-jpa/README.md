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