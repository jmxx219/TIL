# 객체 지향 설계와 스프링

### 목차

- [스프링](#스프링)
- [객체 지향 프로그래밍](#객체-지향-프로그래밍)
  - [역할과 구현의 분리](#역할과-구현의-분리)
  - [객체 지향 설계 원칙(SOLID)](#객체-지향-설계-원칙(SOLID))
  - [객체 지향 설계와 스프링](#객체-지향-설계와-스프링)
- [스프링 핵심 원리 이해](#스프링-핵심-원리-이해)
    - [회원 및 주문과 할인 도메인 설계](#회원-및-주문과-할인-도메인-설계)
    - [객체 지향 원리 적용](#객체-지향-원리-적용)

<br/>

### 스프링

**스프링 프레임워크**
- 핵심 기술: 스프링 DI 컨테이너, AOP, 이벤트, 기타
- 웹 기술: 스프링 MVC, 스프링 WebFlux
- 데이터 접근 기술: 트랜잭션, JDBC, ORM 지원, XML 지원
- 기술 통합: 캐시, 이메일, 원격 접근, 스케줄링
- 데스트: 스프링 기반 테스트 지원
- 언어: 코틀린, 그루비

**스프링 부트**
- 스프링을 편리하게 사용할 수 있도록 지원함(최근에는 기본으로 사용)
  - 단독으로 실행할 수 있는 스프링 애플리케이션을 쉽게 생성
    - Tomcat 같은 웹 서버를 내장 ➜ 별도의 웹 서버 설치 x
  - 손쉬운 빌드 구성을 위한 starter 종속성 제공
  - 스프링과 3rd party(외부) 라이브러리 자동 구성
  - 관계에 의한 간결한 설정

**스프링의 핵심**
- 스프링은 자바 언어 기반의 프레임워크
  - 자바 언어의 큰 특징: `객체 지향 언어`
- 스프링은 객체 지향 언어가 가진 강력한 특징을 살려내는 프레임워크
  - 좋은 **객체 지향** 어플리케이션을 개발할 수 있도록 도와줌

<br/>

## 객체 지향 프로그래밍

객체 지향 특징: `추상화`, `캡슐화`, `상속`, `다형성`
- 컴퓨터 프로그램을 명령어의 목록으로 보는 시각에서 벗어나, 여러 개의 독립된 단위인 `객체`들의 모임으로 파악하고 하는 것
  - 각각의 `객체`는 `메시지`를 주고받고, 데이터를 처리할 수 있음(`협력`)
- 객체 지향 프로그래밍은 프로그램을 `유연`하고 `변경이 용이`하게 만들기 때문에 대규모 소프트웨어 개발에 많이 사용됨

<br/>

### 역할과 구현의 분리

- `역할`과 `구현`으로 구분하면 세상이 단순해지고, 유연해지며 변경도 편리해짐
- 장점
  - 클라이언트는 대상의 역할(인터페이스)만 알고있으면 됨
  - 클라이언트는 구현 대상의 내부 구조를 몰라도 됨
  - 클라이언트는 구현 대상의 내부 구조가 변경되어도 영향을 받지 않음
  - 클라이언트는 구현 대상 자체를 변경해도 영향을 받지 않음

**자바 언어**
- 자바 언어의 다형성을 활용하여 역할과 구현을 분리함
  - `역할` = `인터페이스`
  - `구현` = `인터페이스를 구현한 클래스`, `구현 객체`
- 객체를 설계할 때 역할과 구현을 명확히 분리
  - 객체 설계 시, `역할(인터페이스)`을 먼저 부여하고, 그 역할을 수행하는 `구현 객체` 만들기

**자바 언어의 다형성**
- 오버라이딩
  - 상위 클래스에서 가지고 있는 메서드를 하위 클래스가 재정의해서 사용
  - 구현 객체가 정의한, 오버라이딩된 메서드가 실행됨
- 다형성의 본질
  - 인터페이스를 구현한 객체 인스턴스를 실행 시점에 유연하게 변경할 수 있음
  - 다형성의 본질을 이해하청)와 려면 협력이라는 객체 사이의 관계에서 시작
    - 혼자 있는 객체는 존재 x
    - 수 많은 객체 클라이언트(요객체 서버(응답)는 서로 협력 관계를 가짐
  - `클라이언트를 변경하지 않고, 서버의 구현 기능을 유연하게 변경 할 수 있음`

**다형성으로 역할과 구현 분리**
- 실세계의 `역할`과 `구현`이라는 컨셉을 다형성을 통해 겍체 세상으로 가져올 수 있음
  - 유연하고 변경에 용이함
  - 확장 가능한 설계
  - 클라이언트에 영향을 주지 않으면서 변경 가능
- 한계
  - 역할(인터페이스) 자체가 변경되면, 클라이언트와 서버 모두 큰 변경이 발생함
  - 인터페이스를 안정적으로 설계하는 것이 중요

<br/>

### 객체 지향 설계 원칙(SOLID)

**단일 책임 원칙(`SRP`)**  
- 한 클래스는 하나의 책임만 가져야 함
  - 클래스는 응집성이 높아야 함
- 하나의 책임이라는 것은 모호함(클 수도, 작을 수도 있으며 문맥과 상황에 따라 다름)
- **중요한 기준은 변경**
  - 변경이 있을 때, 파급 효과가 적으면 단일 책임 원칙을 잘 따른 것
  
**개방-폐쇄 원칙(`OCP`)**
- 코드 수정 없이 확장 가능 해야 함
  - 소프트웨어 요소는 확장에는 열려 있고, 변경에는 닫혀 있어야 함
- `다형성`을 활용(역할과 구현을 분리)
  ```java
  public class MemberService {
  //    private MemberRepository memberRepository = new MemoryMemberRepository();
      private MemberRepository memberRepository = new JdbcMemberRepository();
  }
  ```
  - 인터페이스를 구현한 새로운 클래스를 만들어서 새로운 기능을 구현
  - 클라이언트가 구현 클래스를 직접 선택해야 함
- 문제점
  - 구현 객체를 변경하려면 클라이언트 코드도 변경해야 함
  - 다형성을 사용했지만, OCP 원칙을 지킬 수 없음
- 해결 
  - **객체를 생성하고, 연관 관계를 맺어주는 별도의 조립, 설정자가 필요함**
  
**리스코프 치환 원칙(`LSP`)**
- 상위 타입은 하위 타입의 인스턴스로 교체가 가능해야 함
  - 프로그램의 객체는 프로그램의 정확성을 깨뜨리지 않으면서, 하위 타입의 인스턴스로 바꿀 수 있어야 함
- 다형성에서 하위 클래스는 인터페이스의 규약을 모두 지켜야 함
  - 다형성을 지원하기 위한 원칙으로, 인터페이스를 구현한 구현제를 믿고 사용하려면 해당 원칙이 필요함

**인터페이스 분리 원칙(`ISP`)**
- 필요 없는 것을 구현하도록 강요하지 않아야 함
- 특정 클라이언트를 위한 인터페이스 여러 개가 범용 인터페이스 하나보다 나음
  - 인터페이스가 명확해지고, 대체 가능성이 높아짐

**의존관계 역전 원칙(`DIP`)**
- 클래스는 구체적 클래스 대신에 `상위 추상 타입`이나 `인터페이스`에 의존해야 함
  - **역할에 의존해야 함**
  - 클라이언트가 인터페이스에 의존해야 유연하게 구현체를 변경할 수 있음
- `MemberService`는 인터페이스에 의존하지만, 구현 클래스도 동시에 의존함
  - 클라이언트가 구현 클래스를 직접 선택 ➜ DIP 위반
  
  
**한계**
- 객체 지향의 핵심은 다형성
- 하지만 다형성 만으로는 구현 객체를 변경할 때 클라이언트 코드도 함께 변경됨
  - `OCP`와 `DIP`를 지킬 수 없음

<br/>

### 객체 지향 설계와 스프링

- 스프링을 사용하기 전
  - 순수하게 자바로 OCP, DIP 원칙을 지키면서 개발해보면, 결국 스프링 프레임워크를 만들게 됨(정확히는 DI 컨테이너)
- 스프링은 `다형성`을 극대화해서 역할과 구현을 편리하게 다룰 수 있도록 지원함
  - 의존관계 주입(`DI`), `DI 컨테이너` 기술로 `OCP`와 `DIP`를 가능하게 지원함
  - 클라이언트 코드의 변경 없이 기능 확장 가능
    - 쉽게 부품을 교체하듯이 개발할 수 있음

<br/>
<br/>

## 스프링 핵심 원리 이해

### 회원 및 주문과 할인 도메인 설계

> 역할과 구현을 분리하기 위해 다형성을 활용하여 인터페이스와 구현 객체를 분리함

**문제점**
```java
public class OrderServiceImpl implements OrderService {
//    private final DiscountPolicy discountPolicy = new FixDiscountPolicy();
    private final DiscountPolicy discountPolicy = new RateDiscountPolicy();
}
```
- DIP 위반
  - 클래스의 의존 관계가 추상 클래스(인터페이스)뿐만 아니라 구체(구현) 클래스에도 의존
    - 주문 서비스 클라이언트(`OrderServiceImpl`)
      - 추상(인터페이스)에 의존함 ➜ `DiscountPolicy`
      - 구체(구현)클래스에 의존함 ➜ `FixDiscountPolicy`, `RateDiscountPolicy`
- OCP 위반
  - 기능을 확장해서 변경하면, 클라이언트 코드에 영향을 줌
    - 할인 정책을 변경하려면 클라이언트인 `OrderServiceImpl` 코드를 수정해야함.

**해결 방법**
- 클라이언트 코드인 `OrderServiceImpl`은 추상 클래스 뿐만 아니라 구체 클래스에도 함께 의존하고 있음
  - 구체 클래스를 변경할 때 클라이언트 코드도 함께 변경됨
- DIP를 위반하지 않도록 인터페이스에만 의존할 수 있게 의존 관계를 변경
  - 누군가가 클라이언트에 구현 객체를 대신 생성하고 주입해주어야 함
  
<br/>

### 객체 지향 원리 적용
  
**AppConfig**
- 애플리케이션의 **전체 동작 방식을 구성**하기 위해, `구현 객체를 생성`하고 `연결`하는 책임을 가지는 별도의 설정 클래스
  - 실제 동작에 필요한 구현 객체를 생성
  - 생성한 객체 인스턴스의 참초(레퍼런스)를 **생성자를 통해서 주입**
- 클라이언트 입장에서 보면 의존 관계를 마치 외부에서 주입 해주는 것 같다고 해서 `DI - 의존 관계 주입(의존성 주입)`이라고 함
  - `OrderServiceImpl`은 생성자를 통해 어떤 구현 객체가 들어올지(주입될지)는 알 수 없고, 이는 오직 외부(AppConfig)에서 결정함
  - 실행에만 집중하면 됨(`책임/관심사 분리`)
- AppConfig의 등장으로 애플리케이션이 크게 사용 영역과, 객체를 생성하고 구성(`Configuration`)하는 영역으로 분리
  - 구성 정보에서도 역할과 구현을 명확하게 분리(리팩토링)
  - AppConfig를 보면 역할과 구현 클래스가 한눈에 보임(전체 구성을 빠르게 파악할 수 있음)
- 새로운 구조와 할인 정책 적용
  - 할인 정책을 변경해도 AppConfig가 있는 구성 영역만 변경하고, 사용 영역은 변경할 필요가 없음.
  
  
**관심사의 분리**
- 애플리케이션에서 각각의 인터페이스는 자신의 역할에만 집중해야 함
  - 책임을 확실히 분리
- AppConfig로 애플리케이션의 전체 동작 방식을 구성
  - 구현 객체를 생성하고, 연결하는 책임
- 클라이언트 객체는 자신의 역학을 실행하는 것에만 집중)
  - 권한이 줄어들고, 책임이 명확해짐
  
  
**객체 지향 설계 원칙 적용**
- SRP(단일 책임 원칙)
  - 구현 객체를 생성하고 연결하는 책임은 `AppConfig`가 담당
  - 클라이언트 객체는 실행하는 책임만 담당
- DIP(의존관계 역전 원칙)
  - `AppConfig`가 클라이언트 코드 대신 객체 인스턴스를 생성해서 클라이언트 코드에 의존 관계를 주입함
- OCP(개방-폐쇄 원칙)
  - `AppConfig`가 의존관계를 변경해서 클라이언트 코드에 주입하므로 클라이언트 코드는 변경하지 않아도 됨
  - 요소를 새롭게 확장해도 사용 역역의 변경은 닫혀 있음
  
<br/>

**제어의 역전 `IoC`(Inversion of Control)**
- 기존 프로그램은 구현 객체가 프로그램의 제어 흐름을 스스로 조종했음
- AppConfig가 등장한 이후에 구현 객체는 자신의 로직을 실행하는 역할만 담당
  - 프로그램의 제어 흐름에 대한 권한은 모두 AppConfig가 가짐
- 프로그램의 제어 흐름을 직접 제어하는 것이 아니라 **외부에서 관리하는 것**을 `제어의 역전(IoC)`라고 함
  - 프레임워크(작성한 코드를 대신 제어하고 실행), 라이브러리(작성한 코드를 직접 제어하고 실행)

**의존관계 주입 `DI`(Dependency Injection)**
- 애플리케이션 **실행 시점(런타임)** 에 외부에서 실제 구현 객체를 생성하고 클라이언트에 전달해서 클라이언트와 서버의 실제 의존관계가 연결 되는 것
  - 객체 인스턴스를 생성하고, 그 참조 값을 전달해서 연결됨
- 의존관계는 **정적인 클래스 의존 관계**와, 실행 시점에 결정되는 **동적인 객체(인스턴스) 의존 관계** 둘을 분리해서 생각해야 함
  - 정적인 클래스 의존 관계: 애플리케이션을 실행하지 않아도 분석 가능
  - 동적인 객체(인스턴스) 의존 관계: 애플리케이션 실행 시점에 실제 생성된 객체 인스턴스의 참조가 연결된 의존 관계
- 의존 관계 주입을 사용
  - 클라이언트 코드를 변경하지 않고, 클라이언트가 호출하는 대상의 타입 인스턴스를 변경할 수 있음
  - 정적인 클래스 의존관계를 변경하지 않고, 동적인 객체(인스턴스) 의존관계를 쉽게 변경할 수 있음

**IoC, DI 컨테이너**
- AppConfig 처럼 객체를 생성하고 관리하면서 의존관계를 연결해 주는 것을 `IoC 컨테이너` 또는 `DI 컨테이너`라 함
- 의존관계 주입에 초점을 맞추어 최근에는 주로 `DI 컨테이너`라 함
 
<br/>

**스프링 컨테이너** `ApplicationContext`
- 기존 방법
  - 개발자가 `AppConfig`를 사용해서 직접 객체를 생성하고 DI 했음
  - 개발자가 필요한 객체를 `AppConfig`를 사용해서 직접 조회했음
  - 자바 코드로 모든 것을 직접함 
- 스프링 컨테이너는 `@Configuration`이 붙은 `AppConfig`를 설정(구성)정보로 사용함
  - `@Bean`이라 적힌 메서드를 모두 호출해서 반환된 객체를 스프링 컨테이너에 등록함
    - 스프링 컨테이너에 등록된 객체를 `스프링 빈`이라고 함
    - 스프링 빈은 `@Bean`이 붙은 메서드의 명을 스프링 빈의 이름으로 사용
- 스프링 컨테이너를 통해 스프링 빈(객체)를 찾음
  - 스프링 빈은 `applicationContext.getBean()` 메서드를 사용해서 찾을 수 있음 

> 이제부터는 `스프링 컨테이너`에 객체를 `스프링 빈`으로 등록하고, 스프링 컨테이너에서 스프링 빈을 찾아 사용하도록 함

<br/>