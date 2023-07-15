# JPA 소개

### 목차
- [SQL 중심적인 개발의 문제점](#SQL-중심적인-개발의-문제점)

<br/>

## SQL 중심적인 개발의 문제점

> 지금 시대는 객체를 관계형 DB에 보관하고 관리함  
> - 애플리케이션을 개발할 때는 객체 지향 언어를 사용함(Java, Scala, ..)  
> - 데이터베이스 세계는 보통 관계형 DB를 많이 사용함(Oracle, MySQL)
> - 관계형 DB에 객체를 저장하려면 수 많은 SQL문을 작성해야 함  

<br/>

### 1. 무한 반복, 지루한 코드
- CRUD 쿼리를 계속해서 반복해야 함
- 객체에 필드를 추가하면, 쿼리문을 모두 수정해야 함
- 결국 관계형 DB를 사용하는 상황에서 SQL에 의존적인 개발을 피하기가 어려움

<br/>

### 2. 패러다임의 불일치(객체 지향 vs 관계형 데이터베이스)
- 관계형 DB는 데이터를 정교화해서 저장하는 것이 목표
- 객체는 필드나 메서드를 잘 캡슐화해서 사용하는 것이 목표
  - 객체를 영구 보관하는 다양한 저장소(RDB, NoSQL, File ...)
    - 현실적인 대안은 관계형 데이터베이스임(다른 대안이 없음)
- 객체를 관계형 데이터베이스에 저장하려면, 객체를 RDB가 인식할 수 있는 SQL로 변환해서 저장해야 함
  - 객체 ➔ `SQL 변환` ➔ RDB에 저장
  - 결국 개발자가 SQL 변환을 담당함(`개발자 == SQL 매퍼`)

<br/>

### 3. 객체와 관계형 데이터베이스의 차이
- 상속
  - 객체에는 `상속 관계`가 있지만, 관계형 DB에는 상속 관계가 없음
  - 그나마 객체의 상속 관계와 유사한 `Table 슈퍼타입 서브타입 관계`가 있음
    - 데이터를 저장할 경우, 슈퍼타입(`Item`)과 서브타입(`Album`) 테이블 2개에 SQL을 작성해야 함
    - 데이터를 조회하는 경우는 매우 복잡함
      - 슈퍼타입과 서브타입 테이블의 Join SQL을 작성하고, 각각의 객체에 필드 값을 넣어주는 등 매우 복잡함
      - 이렇게 개발자가 SQL 매핑 작업을 하게 되면, 생산성이 저하되고 실수를 할 가능성도 높아지게 됨
      - 그래서 DB에 저장할 객체는 상속관계를 쓰지 않음
  - **하지만 객체를 RDB가 아닌 자바 컬렉션에 저장하고 조회할 경우, 매우 단순해짐**
    - `ist.add(album);`, `Album album = list.get(albumId);`
    - 필요할 경우, 부모타입으로 조회하여 다형성 활용도 가능함
      - `Item item = list.get(albumId);`
- 연관관계
  - 객체는 레퍼런스를 가질 수 있지만, RDB는 PK와 FK를 사용하여 JOIN해야 함
    - 객체(Object)
      - **참조**(Reference)를 사용하여 연관 관계를 찾음(`member.getTeam()`)
      - 양방향으로 참조 불가능
    - 테이블(RDB)
      - **외래 키**(FK)를 사용 Join 쿼리를 통해 연관 관계를 찾음(`JOIN ON M.TEAM_ID = T.TEAM_ID`)
      - 양방향 참조 가능(테이블의 경우 PK와 FK로 JOIN하기 때문)

<br/>

### 4. 모델링 과정에서의 문제
- 객체를 테이블에 맞추어 모델링
  - 보통 객체를 테이블에 맞추어 모델링하기 때문에 외래키 값 그대로 필드에 추가함
  ```java
  class Member {
      String id;       // MEMBER_ID 컬럼 사용
      Long teamId;     // TEAM_ID FK 컬럼 사용 
      String username; // USERNAME 컬럼 사용
  }
  class Team { 
      Long id;         // TEAM_ID PK 사용 
      String name;     // NAME 컬럼 사용 
  }
  ```
  - 이렇게 하면 데이터를 저장할 때 INSERT 쿼리에 컬럼과 필드를 매핑해서 편리하게 넣을 수 있음
    - `INSERT INTO MEMBER(MEMBER_ID, TEAM_ID, USERNAME) VALUES ...` 
  - 하지만 이 방법은 객체지향스럽지 않다는 문제 존재
- 객체다운 모델링
  - `Member`가 `Team`의 외래키 값을 갖는게 아니라 `Team` 객체에 대한 참조를 가지는 것이 더 객체지향스러움
  ```java
  class Member { 
      String id;        // MEMBER_ID 컬럼 사용 
      Team team;        // 참조로 연관관계를 맺음
      String username;  // USERNAME 컬럼 사용 
                       
      Team getTeam() { 
          return team; 
      } 
  }
  ```
  - 데이터를 저장할 때, 외래키 값(`TEAM_ID`)을 나타내는 필드가 없어 참조를 통해 값을 얻어와야 함
    - `member.getTeam().getId();`
    - `INSERT INTO MEMBER(MEMBER_ID, TEAM_ID, USERNAME) VALUES...`
  - 데이터를 조회할 때, 객체지향 모델링의 문제점이 발생함
    - Member랑 Team을 조인하여 조회한 데이터를 각각의 객체에 값을 일일이 넣어주고, 마지막으로 연관관계 설정까지 해주어야 함 
      - `member.setTeam(team);`: 회원과 팀 관계 설정
      - 이러한 과정들은 너무 번거로움
    - 만약 자바 컬렉션에서 관리한다면 해당 과정들이 단순해짐
- 객체 모델링을 자바 컬렉션에서 관리
  ```java
  list.add(member);
  Member member = list.get(memberId);
  Team team = member.getTeam();
  ```
  - 단순하게 list에 add로 저장하고, get으로 조회해서 코드 한줄로 해결이 가능함

<br/>

### 5. 객체 그래프 탐색
- 객체는 자유롭게 객체 그래프를 탐색할 수 있어야 함
  - 서로 참조가 있는 객체들은 자유롭게 해당 참조를 타고 조회가 가능해야 함
  - `member.getTeam()`, `member.getOrder().getDelievery()` 등등
- 하지만 서비스 로직에서 RDB와 연관된 데이터를 탐색할 때 객체 그래프를 마음껏 호출할 수 없는 문제가 존재함
  - 처음 실행하는 SQL에 따라 탐색 범위가 결정되기 때문
  ```java
  SELECT M.*, T.*
  FROM MEMBER M
  JOIN TEAM T ON M.TEAM_ID = T.TEAM_ID
  
  member.getTeam(); //OK
  member.getOrder(); //null
  ```
  - 쿼리로 조회 후, `getTeam()`메서드를 통해 `Team` 객체를 조회하는 것은 가능하지만 `Order` 객체를 조회하는 것은 불가능
    - 처음 SQL문을 실행할 때 Member와 Team만 조회해서 가져오기 때문
    - 결국 코드를 Order를 조회해 왔는지, 아닌지 확인하고 쓰는 과정들이 필요함
- 엔티티 신뢰 문제 발생
  ```java
  class MemberService {
      public void process() {
          Member member = memberDAO.find(memberId);
          member.getTeam(); //???
          member.getOrder().getDelievery(); //???
      }
  }
  ```
  - memberDAO를 통해 조회해온 member 엔티티에는 `Team`과 `Order`, 그리고 `Delievery`라는 연관관계들 있음
    - 하지만 memberDAO에서 해당 데이터를 조회해왔는지 아닌지 확실히 알지 못하기 때문에 자유롭게 호출할 수 없게 됨 
    - 결국 memberDAO 코드를 직접 확인하지 않는 이상, 반환된 엔티티 객체를 신뢰하고 쓸 수가 없음
  - 계층형 아키텍처에서는 그 다음 계층에 대해서 신뢰를 하고 있어야함
    - 물리적으로는 서비스 DAO로 나누어져있지만, 논리적으로는 엮어 있음
    - 직접 DAO 계층을 확인해보지 않으면, 해당 계층이 반환환 엔티티에 대해 신회할 수 없음 
- 그렇다고 해서 모든 객체를 미리 로딩해볼 수도 없음
  - 회원을 조회할 때마다 모든 연관된 객체를 다 끌고올 수도 없기 때문에, 상황에 따라 동일한 회원 조회 메서드를 여러 번 생성해야 함
  - 이렇게 SQL을 직접 다루면 계층형 아키텍처(Layered 아키텍처)에서 진정한 의미의 계층 분할이 어려움
    - 물리적으로는 계층이 나누어져 있지만, 논리적으로는 굉장히 강결합되어 있음

<br/>

### 6. 비교하기에서의 차이
- SQL에서 조회하는 경우
  ```java
  class MemberDAO {
    public Member getMember(String memberId) {
      String sql = "SELECT * FROM MEMBER WHERE MEMBER_ID = ?";
      ...
      //JDBC API, SQL 실행
      return new Member(...);
    }
  }
  ```
  ```java
  String memberId = "100";
  Member member1 = memberDAO.getMember(memberId);
  Member member2 = memberDAO.getMember(memberId);
  
  member1 == member2; // 다름
  ```
  - 식별자가 같아도 조회한 두 객체 member1과 member2는 다름
  - `MemberDAO` 클래스에서 매번 new로 객체를 생성하기 때문
- 자바 컬렉션에서 조회하는 경우
  ```
  String memberId = "100";
  Member member1 = list.get(memberId);
  Member member2 = list.get(memberId);
  member1 == member2; // 같음
  ```
  - 컬렉션에서 두 객체를 조회하면 항상 같은 참조를 반환하기 때문에 두 객체는 같음

<br/>

### 결론

- 객체답게 모델링 할수록 매핑작업만 늘어남
  - 객체지향적으로 설계를 하면 할수록 더 번잡한 작업만 늘어남
  - 결국 SQL에 맞춰 설계하고, 데이터를 전송하는 역할(DTO)만 하도록 할 수 밖에 없음
- 객체를 자바 컬렉션에 저장하듯이 DB에 저장하는 방법은?
  - 이러한 고민의 결과로 `JPA(Java Persistence API)`가 나오게 됨