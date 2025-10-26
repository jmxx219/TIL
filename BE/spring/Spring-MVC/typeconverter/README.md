# 스프링 타입 컨버터

### 목차
- [스프링 타입 변환](#스프링-타입-변환)
- [타입 컨버터(Converter)](#타입-컨버터(Converter))
- [ConversionService](#ConversionService)
- [Converter 적용](#Converter-적용)
  - [스프링](#스프링)
  - [view 템플릿](#view-템플릿)
- [Formatter](#Formatter)
  - [소개](#소개)
  - [포맷터를 지원하는 컨버전 서비스](#포맷터를-지원하는-컨버전-서비스)
  - [적용](#적용)
  - [스프링이 제공하는 기본 포맷터](#스프링이-제공하는-기본-포맷터)

<br/>

## 스프링 타입 변환

- 기존
  - HTTP 요청 파라미터는 모두 문자로 처리되기 때문에, 다른 타입으로 변환하는 과정을 거쳐야 함
    - `String data = request.getParameter("data")`
    - `Integer intValue = Integer.valueOf(data)`
- 스프링 타입 변환 적용
  - 스프링 MVC 요청 파라미터: `@RequestParam`, `@ModelAttribute`, `@PathVariable`
      - 스프링 MVC가 제공하는 `@RequestParam`을 이용하면 스프링이 중간에서 자동으로 타입을 변환해줌
  - `@Value` 등으로 YML 읽기
  - XML에 넣은 스프링 빈 정보를 변환
  - 뷰 렌더링할 때

**컨버터 인터페이스**
- 스프링은 확장 가능한 컨버터 인터페이스를 제공함
```java
package org.springframework.core.convert.converter;
public interface Converter<S, T> { 
    T convert(S source);
}
```
- 스프링에 추가적인 새로운 타입 변환이 필요하면, 컨버터 인터페이스를 구현해서 등록하면 됨
  - 모든 타입에 적용 가능
- 과거에는 `PropertyEditor` 사용
  - 동시성 문제가 있어 타입을 변환할 때마다 객체를 계속 생성해야하는 단점 존재
  - 현재는 `Converter` 사용

<br/>

## 타입 컨버터(Converter)

- 타입 컨버터를 사용하려면 `Converter` 인터페이스를 구현
  - `org.springframework.core.convert.converter.Converter`

**사용자 정의 타입 컨버터**
- `IpPort`
  ```java
  @Getter
  @EqualsAndHashCode
  public class IpPort {
      private String ip;
      private int port;
  
      public IpPort(String ip, int port) {
          this.ip = ip;
          this.port = port;
      }
  }
  ```
  - 롬복의 `@EqualsAndHashCode`
    - 모든 필드를 사용해서 `equals()`, `hashCode()`를 생성함
    - 모든 필드의 값이 같다면 `a.equals(b)`의 결과는 `true`
- `StringToIpPortConverter`
  ```java
  @Slf4j
  public class StringToIpPortConverter implements Converter<String, IpPort> {
      @Override
      public IpPort convert(String source) {
          log.info("converter source={}", source);
          // "127.0.0.1:8080" -> IpPort 객체
          String[] split = source.split(":");
          String ip = split[0];
          int port = Integer.parseInt(split[1]);
          return new IpPort(ip, port);
      }
  }
  ```
  - `127.0.0.1:8080` 문자를 입력하면 IpPort 객체로 변환하여 반환함

**용도에 따른 타입 컨버터**
- `Converter`: 기본 타입 컨버터
- `ConverterFactory`: 전체 클래스 계층 구조가 필요할 때
- `GenericConverter`: 정교한 구현, 대상 필드의 애노테이션 정보 사용 가능
- `ConditionalGenericConverter`: 특정 조건이 참인 경우에만 실행

> 타입 컨버터를 직접 모두 사용하면, 개발자가 직접 컨버팅하는 것과 차이가 없음   
> 타입 컨버터를 등록하고 관리하면서 편리하게 변환 기능을 제공하는 역할이 필요함

<br/>

## ConversionService

- 스프링은 개별 컨버터를 모아두고, 묶어서 편리하게 사용할 수 있는 컨버전 서비스(`ConversionService`) 기능을 제공함

**ConversionService 인터페이스**
- `canConvert()`: 컨버팅이 가능한지 판단
- `convert()`: 컨버팅 기능

**사용**
```java
DefaultConversionService conversionService = new DefaultConversionService();
conversionService.addConverter(new StringToIntegerConverter());
conversionService.addConverter(new StringToIpPortConverter());

assertThat(conversionService.convert("10", Integer.class)).isEqualTo(10);

IpPort ipPort = conversionService.convert("127.0.0.1:8080", IpPort.class);
assertThat(ipPort).isEqualTo(new IpPort("127.0.0.1", 8080));
```
- `DefaultConversionService`
  - `ConversionService` 인터페이스를 구현하고, 컨버터를 등록하는 기능을 제공
  - `addConverter()`: `Converter` 등록
  - `convert(source, 타입)`: 컨버팅 기능 사용

**등록과 사용 분리**
- 컨버터를 등록할 때는 타입 컨버터를 명확하게 알아야 함
- 컨버터를 사용할 때는 타입 컨버터를 전혀 몰라도 됨
- 타입 컨버터들은 모두 컨버전 서비스 내부에 숨어서 제공됨
  - 타입을 원하는 사용자는 컨버전 서비스 인터페이스에만 의존
  - 컨버전 서비스를 등록하는 부분과 사용하는 부분을 분리하고, 의존 관계 주입을 사용

**ISP(인터페이스 분리 원칙)**
- 인터페이스 분리 원칙은 클라이언트가 자신이 이용하지 않는 메서드에는 의존하지 않아야 함
- `DefaultConversionService`는 두 인터페이스를 구현하고 있음
  - `ConversionService`: 컨버전 **사용**에 초점
  - `ConversionRegistry`: 컨버전 **등록**에 초점
- 인터페이스를 분리하면 컨버터를 사용하는 클라이언트와 컨버터를 등록하고 관리하는 클라이언트이 관심사를 명확하게 구분할 수 있음
  - 컨버터를 사용하는 클라이언트는 `ConversionService`만 의존하면 되기 때문에, 컨버터를 어떻게 등록하고 관리하는지는 몰라도 됨
    - 컨버터를 사용하는 클라이언트는 꼭 필요한 메서드만 알게됨

> 스프링은 `@RequestParam` 같은 곳에서 `ConversionService`를 사용해서 타입을 변환함

<br/>

## Converter 적용

### 스프링

웹 애플리케이션에 적용

- `WebConfig`
  - 컨버터 등록
  ```java
  @Override
  public void addFormatters(FormatterRegistry registry) {
      registry.addConverter(new StringToIntegerConverter());
      registry.addConverter(new StringToIpPortConverter());
  }
  ```
  - `WebMvcConfigurer`가 제공하는 `addFormatters()`를 사용해서 추가하고 싶은 컨버터를 등록함
    - 스프링은 내부에서 사용하는 `ConversionService`에 컨버터를 추가해줌
  - `StringToIntegerConverter` 컨버터를 등록하기 전에도 타입 변환이 잘 수행되었음
    - 스프링은 내부에서 수많은 기본 컨버터를 제공하고 있음
    - 컨버터를 추가하면 추가한 컨버터가 기본 컨버터보다 높은 우선순위를 가짐
- `Controller`
  ```java
  @GetMapping("/ip-port")
  public String ipPort(@RequestParam IpPort ipPort) {
      return "ok";
  }
  ```
  - `http://localhost:8080/ip-port?ipPort=127.0.0.1:8080` 실행
    - `?ipPort=127.0.0.1:8080` 쿼리 스트링이 `@RequestParam IpPort ipPort`에서 객체 타입으로 잘 변환됨
  - `@RequestParam`을 처리하는 `ArgumentResolver`인 `RequestParamMethodArgumentResolver`에서 `ConversionService`를 사용해서 타입을 변환함
    - 부모 클래스와 다양한 외부 클래스를 호출하는 등 복잡한 내부 과정을 거치기 때문에 대략 위와 같이 처리되는 것으로 이해할 수 있음


<br/>

### view 템플릿

**타임리프**
- 타임리프는 렌더링 시, 컨버터를 적용해서 렌더링 하는 방법을 편리하게 지원함
- `${...}`: 변수 표현식
- `${{...}}`: 자동으로 컨버런 서비스를 사용해서 변환된 결과를 출력해줌(컨버전 서비스 적용)

**기본 적용**
- `ConverterController`
  ```java
  @GetMapping("/converter-view")
  public String converterView(Model model) {
      model.addAttribute("number", 10000);
      model.addAttribute("ipPort", new IpPort("127.0.0.1", 8080));
      return "converter-view";
  }
  ```
  - `Model`에 숫자와 객체를 담아서 뷰 템플릿에 전달함
- `converter-view.html`
  ```html
  <ul>
      <li>${number}: <span th:text="${number}" ></span></li>
      <li>${{number}}: <span th:text="${{number}}" ></span></li>
      <li>${ipPort}: <span th:text="${ipPort}" ></span></li>
      <li>${{ipPort}}: <span th:text="${{ipPort}}" ></span></li>
  </ul>
  ```
  - 뷰 템플릿은 데이터를 문자로 출력함
  - `${{number}}`
    - 컨버터를 적용하면 `Integer` 타입인 `10000`을 `String` 타입으로 변환하는 컨버터인 `IntegerToStringConverter`가 실행됨
      - 컨버터를 실행하지 않아도 타임리프가 숫자를 문자로 자동으로 변환하기 때문에 컨버터를 적용할 때와 하지 않을 때가 같음
  - `${{ipPort}}`
    - 컨버터를 적용하면 `IpPort` 타입을 `String` 타입으로 변환해야 하기 때문에 `IpPortToStringConverter`가 적용됨

**Form 적용**
- `Controller`
  ```java
  @GetMapping("/converter/edit")
  public String converterForm(Model model) {
      IpPort ipPort = new IpPort("127.0.0.1", 8080);
      Form form = new Form(ipPort);
      model.addAttribute("form", form);
      return "converter-form";
  }
  
  @PostMapping("/converter/edit")
  public String converterEdit(@ModelAttribute Form form, Model model) {
      IpPort ipPort = form.getIpPort();
      model.addAttribute("ipPort", ipPort);
      return "converter-view";
  }
  ```
  - `GET /coverter/edit`
    - `IpPort`를 뷰 템플릿 폼에 출력
    - `th:field`가 자동으로 컨버전 서비스를 적용해서 `${{ipPort}} 처럼 적용됨
    - `IpPort` ➜ `String으로 변환됨
  - `POST /converter/edit`
    - 뷰 템플릿 폼의 `IpPort`를 받아서 출력
    - `@ModelAttribute`를 사용해서 `String` ➜ `IpPort`로 변환
- `converter-form.html`
  ```html
  <form th:object="${form}" th:method="post">
      th:field <input type="text" th:field="*{ipPort}"><br/>
      th:value <input type="text" th:value="*{ipPort}">(보여주기 용도)<br/>
      <input type="submit"/>
  </form>
  ```
  - `th:field`
    - 타임리프의 `th:field`는 다양한 기능이 있는데, 컨버전 서비스도 함께 적용됨

<br/>

## Formatter

### 소개

- `Converter`는 입력과 출력 타입에 제한이 없는 범용 타입 변환 기능을 제공함
- 일반적인 웹 애플리케이션 환경에서 불린 타입을 숫자로 바꾸는 것과 같은 범용 기능 보다는, 문자를 다른 타입으로 변환하거나 다른 타입을 문자로 변환하는 상황이 대부분임
- `Formatter`
  - 객체를 특정한 포맷에 맞추어 문자로 출력하거나 또는 그 반대의 역할을 하는 것에 특화된 기능
  - 컨버터의 특별한 버전

**Converter vs Formatter**
- `Converter`: 범용(객체 ➜ 객체)
- `Formatter`: 문자에 틱화(객체 ➜ 문자, 문자 ➜ 객체) + 현지화(`Locale`)

**Formatter 인터페이스**
```java
public interface Printer<T> {
  String print(T object, Locale locale);
}
public interface Parser<T> {
  T parse(String text, Locale locale) throws ParseException;
}
public interface Formatter<T> extends Printer<T>, Parser<T> {
}
```
- `String print(T object, Locale locale)`: 객체를 문자로 변경 
- `T parse(String text, Locale locale)`: 문자를 객체로 변경

<br/>

### 포맷터를 지원하는 컨버전 서비스

- 컨버전 서비스에는 컨버터만 등록 가능(포맷터 등록 불가능)
  - 포맷터는 `객체 ➜ 문자`, `문자 ➜ 객체`로 변환하는 특별한 컨버터임
  - 포맷터를 지원하는 컨버전 서비스를 사용하면 컨버전 서비스에 포맷터를 추가할 수 있음
    - 내부에서 어탭터 패턴을 사용해서 `Formatter`가 `Converter`처럼 동작하도록 지원함
  - `FormattingConversionService`
    - 포맷터를 지원하는 컨버전 서비스
    ```java
      @Test
      void formattingConversionService() {
          DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
  
          // 컨버터 등록
          conversionService.addConverter(new StringToIpPortConverter());
          conversionService.addConverter(new IpPortToStringConverter());
          // 포맷터 등록
          conversionService.addFormatter(new MyNumberFormatter());
  
          // 컨버터 사용
          IpPort ipPort = conversionService.convert("127.0.0.1:8080", IpPort.class);
          assertThat(ipPort).isEqualTo(new IpPort("127.0.0.1", 8080));
          // 포맷터 사용
          assertThat(conversionService.convert(1000, String.class)).isEqualTo("1,000");
          assertThat(conversionService.convert("1,000", Long.class)).isEqualTo(1000L);
      }
    ```
    - `addConverter()`와 `addFormatter()`로 컨버터와 포맷터를 등록함
    - `convert()`로 컨버터와 포맷터 모두 사용 가능
  - `DefaultFormattingConversionService`
    - `FormattingConversionService`에 기본적인 통화, 숫자 관련 몇가지 기본 포맷터를 추가해서 제공함
    - `FormattingConversionService`는 `ConversionService` 관련 기능을 상속받음
      - 컨버터와 포맷터 모두 등록 및 사용 가능

<br/>

### 적용

`WebConfig`
- 기존 컨버터는 주석 처리
  - 우선순위는 컨버터가 우선하기 때문에, 포맷터가 적용되지 않고 컨버터가 적용됨
- `registry.addFormatter(new MyNumberFormatter())` 추가
  - 컨버전 서비스를 적용한 결과 `MyNumberFormatter`가 적용됨

<br/>

### 스프링이 제공하는 기본 포맷터

- 스프링은 자바에서 기본으로 제공하는 타입들에 대해 수 많은 포맷터를 기본으로 제공함
  - IDE에서 `Formatter` 인터페이스의 구현 클래스를 찾아볼 수 있음
  - 포맷터를 기본 형식이 지정되어 있어, 객체의 각 필드마다 다른 형식으로 포맷을 지정하기에는 어려움
- 스프링은 애노테이션 기반으로 원하는 형식을 지정해서 사용할 수 있는 포맷터 두 가지를 제공함
  - `@NumberFormat`: 숫자 관련 형식 지정 포맷터 사용, `NumberFormatAnnotationFormatterFactory`
  - `@DateTimeFormat`: 날짜 관련 형식 지정 포맷터 사용, `Jsr310DateTimeFormatAnnotationFormatterFactory`
  ````java
  @Data
  static class Form {
      @NumberFormat(pattern = "###,###")
      private Integer number;
  
      @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
      private LocalDateTime localDateTime;
  }
  ````
  
<br/>


### 주의

**메시지 컨버터(`HttpMessageConverter`)**
- 컨버전 서비스가 적용되지 않음
- `HttpMessageConverter`의 역할은 HTTP 메시지 바디의 내용을 객체로 변환하거나 객체를 HTTP 메시지 바디에 입력하는 것
  - JSON을 객체로 변환하는 메시지 컨버터는 내부에서 Jackson 같은 라이브러리를 사용함
  - 객체를 JSON으로 변환한다면 그 결과는 라이브러리에 달린 것
  - JSON 결과로 만들어지는 숫자나 날짜 포맷을 변경하고 싶을 때, 해당 라이브러리가 제공하는 설정을 통해 포맷을 지정해야 함
  - 이것은 컨버전 서비스와 전혀 관계가 없음
- 컨버전 서비스는 `@RequestParam`, `@ModelAttribute`, `@PathVariable`, 뷰 템플릿 등에서 사용할 수 있음