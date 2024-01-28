# 스프링 핵심 원리

[객체 지향 설계와 스프링 참고](https://github.com/jmxx219/SpringMVC/blob/main/basic-spring/OOD.md)

### 목차
- [스프링 컨테이너와 스프링 빈](#스프링-컨테이너와-스프링-빈)
  - [스프링 컨테이너](#스프링-컨테이너)
  - [스프링 컨테이너 생성](#스프링-컨테이너-생성)
  - [스프링 빈 조회](#스프링-빈-조회)
- [싱글톤 컨테이너](#싱글톤-컨테이너)
- [컨포넌트 스캔](#컴포넌트-스캔)
- [의존관계 자동 주입](#의존관계-자동-주입)
- [빈 생명주기 콜백](#빈-생명주기-콜백)
- [빈 스코프](#빈-스코프)

<br/>

## 스프링 컨테이너와 스프링 빈

### 스프링 컨테이너

- 기존 방법
    - 개발자가 `AppConfig`를 사용해서 직접 객체를 생성하고 DI 했음
    - 개발자가 필요한 객체를 `AppConfig`를 사용해서 직접 조회했음
    - 자바 코드로 모든 것을 직접함
- 스프링 컨테이너는 `@Configuration`이 붙은 `AppConfig`를 설정(구성)정보로 사용함
    - `@Bean`이라 적힌 메서드를 모두 호출해서 반환된 객체를 스프링 컨테이너에 등록함
        - 스프링 컨테이너에 등록된 객체를 `스프링 빈`이라고 함
        - 스프링 빈은 `@Bean`이 붙은 메서드의 명을 스프링 빈의 이름으로 사용
    - 스프링 컨테이너를 통해 객체를 `스프링 빈`으로 등록하고, `스프링 빈(객체)`을 찾음
        - 스프링 빈은 `applicationContext.getBean()` 메서드를 사용해서 찾을 수 있음
- `ApplicationContext`를 스프링 컨테이너라고 하며, 인터페이스임
  - XML 기반으로 만들 수 있고, 애노테이션 기반의 자바 설정 클래스로 만들 수 있음
    - `AppConfig` : 애노테이션 기반의 자바 설정 클래스로 만든 스프링 컨테이너

**BeanFactory와 ApplicationContext**
- `BeanFactory`
    - 스프링 컨테이너의 최상위 인터페이스
    - 스프링 빈을 관리하고 조회하는 역할을 담당
    - `getBean()`을 제공
- `ApplicationContext`
    - `BeanFactory` 기능을 모두 상속받아서 제공
    - **빈 관리 기능 + 편리한 부가 기능**을 제공
        - 메시지 소스를 활용한 국제화 기능
        - 환경변수
        - 애플리케이션 이벤트
        - 편리한 리소스 조회
    - `BeanFactory`를 직접 사용할 일은 거의 없고, `ApplicationContext`를 사용

**다양한 설정 형식 지원**
1. 애노테이션 기반 자바 코드 설정 사용
    - `new AnnotationConfigApplicationContext(AppConfig.class)`
    - `AnnotationConfigApplicationContext` 클래스를 사용하면서 자바 코드로된 설정 정보를 넘김
2. XML 설정 사용
    - `GenericXmlApplicationContext`를 사용하면서 xml 설정 파일을 넘김
    - 컴파일 없이 빈 설정 정보를 변경할 수 있는 장점 있음
    - xml 기반으로 설정하는 것은 최근에 잘 사용하지 않음

**스프링 빈 설정 메타 정보(`BeanDefinition`)**
- 스프링이 다양한 형태의 설정 정보를 `BeanDefinition`으로 추상화해서 사용함
- 역할과 구현을 개념적으로 나눈 것
  - 자바 코드, XML 등 설정 정보를 읽어서 `BeanDefinition` 생성
  - 스프링 컨테이너는 자바코드인지, XML인지 몰라도 `BeanDefinition`만 알면 됨
- `BeanDefinition`을 빈 설정 메타 정보라고 함
  - `@Bean`, `<bean>` 당 각각 하나씩 메타 정보가 생성
  - 스프링 컨테이너는 이 메타 정보를 기반으로 스프링 빈을 생성
- `BeanDefinition`을 직접 생성해서 스프링 컨테이너에 등록할 수 있지만, 실무에서 직접 정의하거나 사용할 일은 거의 없음
    
<br/>
  
### 스프링 컨테이너 생성

1. 스프링 컨테이너 생성
    ```java
    ApplicationContext applicationContext = 
            new AnnotationConfigApplicationContext(AppConfig.class);
    ```
   - 스프링 컨테이너를 생성할 때는 구성 정보를 지정해야 함
   - `AppConfig.class`를 구성 정보로 지정
2. 스프링 빈 등록
   - 스프링 컨테이너는 파라미터로 넘어온 설정 클래스 정보를 사용해서 스프링 빈을 등록
   - 빈 이름
     - 빈 이름은 메서드 이름을 사용하고, 직접 부여할 수도 있음
       - `@Bean(name="memberService2")`
     - 빈 이름은 항상 다른 이름을 부여해야 함
     - 같은 이름을 부여할 경우, 다른 빈이 무시되거나 기존 빈을 덮어버리는 등 오류 발생
3. 스프링 빈 의존관계 설정
   - 스프링 컨테이너는 설정 정보를 참고해서 의존 관계를 주입(`DI`)함

<br/>

### 스프링 빈 조회

**컨테이너에 등록된 모든 빈 조회**
```java
String[] beanDefinitionNames = ac.getBeanDefinitionNames();
for (String beanDefinitionName : beanDefinitionNames) {
    BeanDefinition beanDefinition = ac.getBeanDefinition(beanDefinitionName);

    // Role ROLE_APPLICATION: 직접 등록한 애플리케이션 빈
    // Role ROLE_INFRASTRUCTURE: 스프링이 내부에서 사용하는 빈
    if (beanDefinition.getRole() == BeanDefinition.ROLE_APPLICATION) {
        Object bean = ac.getBean(beanDefinitionName);
        System.out.println("2. name = " + beanDefinitionName + " object = " + bean);
    }
}
```
- 모든 빈 출력하기
  - 실행하면 스프링에 등록된 모든 빈 정보를 출력할 수 있음
  - `ac.getBeanDefinitionNames()`: 스프링에 등록된 모든 빈 이름을 조회
  - `ac.getBean()`: 빈 이름으로 빈 객체(인스턴스)를 조회
- 애플리케이션 빈 출력하기
  - 스프링이 내부에서 사용하는 빈은 제외하고, 내가 등록한 빈만 출력 
  - 스프링이 내부에서 사용하는 빈은 getRole()로 구분가능 
    - `ROLE_APPLICATION`: 일반적으로 사용자가 정의한 빈 
    - `ROLE_INFRASTRUCTURE`: 스프링이 내부에서 사용하는 빈

**기본 빈 조회**
```java
// 빈 이름으로 조회
MemberService memberService = ac.getBean("memberService", MemberService.class);
// 이름 없이 타입만으로 조회 
MemberService memberService = ac.getBean(MemberService.class);
// 구체 타입으로 조회
MemberServiceImpl memberService = ac.getBean("memberService", MemberServiceImpl.class);
```
- `ac.getBean(빈이름, 타입)`, `ac.getBean(타입)`
- 조회 대상 스프링 빈이 없으면 예외(`NoSuchBeanDefinitionException`) 발생 
- 구체 타입으로 조회하면 변경 시, 유연성이 떨어짐

**동일한 타입이 둘 이상**
```java
Map<String, MemberRepository> beansOfType = ac.getBeansOfType(MemberRepository.class);
for (String key : beansOfType.keySet()) {
    System.out.println("key = " + key + " value = " + beansOfType.get(key));
}
System.out.println("beansOfType = " + beansOfType);
```
- 타입으로 조회 시, 같은 타입의 스프링 빈이 둘 이상이면 오류 발생 ➜ 빈 이름을 지정해서 조회
- `ac.getBeansOfType()`을 사용하면 해당 타입의 모든 빈 조회 가능

**상속 관계**
```java
// 특정 하위 타입으로 조회
RateDiscountPolicy bean = ac.getBean(RateDiscountPolicy.class);

// 부모 타입으로 모두 조회
Map<String, DiscountPolicy> beansOfType = ac.getBeansOfType(DiscountPolicy.class);
for (String key : beansOfType.keySet()) {
    System.out.println("key = " + key + " value=" + beansOfType.get(key));
}
```
- 부모 타입으로 조회하면 자식 타입도 함께 조회됨
  - 자식이 둘 이상 있으면 중복 오류 발생 ➜ 빈 이름 지정해서 조회
- 모든 자바 객체의 최고 부모인 `Object` 타입으로 조회하면 모든 스프링 빈을 조회함



<br/>
