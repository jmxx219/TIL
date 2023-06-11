# 스프링 타입 컨버터

### 목차
- [스프링 타입 변환](#스프링-타입-변환)

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

