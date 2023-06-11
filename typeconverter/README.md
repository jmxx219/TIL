# 스프링 타입 컨버터

### 목차
- [스프링 타입 변환](#스프링-타입-변환)
- [타입 컨버터(Converter)](#타입-컨버터(Converter))

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
