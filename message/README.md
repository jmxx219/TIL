# 메시지, 국제화

### 목차
- [메시지와 국제화](#메시지와-국제화)
- [스프링 메시지 소스](#스프링-메시지-소스)
  - [설정](#설정)
  - [사용](#사용)
- [웹 애플리케이션에 메시지 적용](#웹-애플리케이션에-메시지-적용)


<br/>

---

## 메시지와 국제화
`properties file encoding` : `UTF-8`로 설정
> **스프링은 기본적인 메시지 관리 기능과 국제화 기능을 모두 제공**

### 메시지

- 메시지 기능
  - 다양한 메시지를 한 곳에서 관리하도록 하는 기능
    - HTML 파일에 하드 코딩되어 있는 메시지를 변경하려면 모든 화면들을 찾아가서 변경해야 함
    - 화면이 무수히 많다면 모든 파일을 고쳐야한다는 문제점을 해결
- `messages.properties`
  - 메시지 관리용 파일을 생성
  - HTML에서 해당 데이터를 key 값으로 불러서 사용



### 국제화

- 메시지 파일(`messages.properties`)을 각 나라별로 별도로 관리하여 서비스 국제화
  - `messages_en.properties`: 영어
  - `messages_ko.properties`: 한국어
- 접근한 나라를 인식하는 방법
  - HTTP `accept-language` 헤더 값 사용
  - 사용자가 직접 언어를 선택하도록 하고, 쿠키 등에서 사용하여 처

<br/>

## 스프링 메시지 소스

### 설정

메시지 관리 기능을 사용하기 위해 스프링이 제공하는 `MessageSource`를 스프링 빈으로 등록
- **직접 등록**
  - `MessageSource`는 인터페이스이기 때문에 구현체인 `ResourceBundleMessageSource`를 스프링 빈으로 등록
  ````java
  @Bean
  public MessageSource messageSource() {
      ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
      messageSource.setBasenames("messages", "errors");
      messageSource.setDefaultEncoding("utf-8");
      return messageSource;
  }
  ````
  - `basenames`: 설정 파일의 이름 지정
    - `messages`로 지정하면 `messages.properties` 파일을 읽어서 사용
    - 국제화 기능 적용 ➔ 파일명 마지막에 언어 정보 추가(`messages_en.properties`)
    - 파일 위치: `/resources/messages.properties`
    - 여러 파일 한 번에 지정 가능(`messages`, `errors`)
  - `defaultEncoding`: 인코딩 정보를 지정
- **스프링 부트 자동 등록**
  - `MessageSource`를 자동으로 스프링 빈으로 등록
  - 메시지 소스 설정
    - `application.properties`
      - 기본 값: `spring.messages.basename=messages`
      - `spring.messages.basename=messages,config.i18n.messages`
  - 메시지 파일 생성
    - `messages.properties`: 기본 값으로 사용(한글)
    - `messages_en.properties`: 영어 국제화 사용


### 사용

`MessageSource` 인터페이스
```java
public interface MessageSource {
    String getMessage(String code, @Nullable Object[] args, @Nullable String
            defaultMessage, Locale locale);
    String getMessage(String code, @Nullable Object[] args, Locale locale) throws
            NoSuchMessageException;
}
```
- 코드를 포함한 일부 파라미터로 메시지를 읽어오는 기능을 제공
- `code`
  - 메시지 코드(key 값)
  - 메시지가 없는 경우
    - `NoSuchMessageException`이 발생
    - 메시지가 없어도 기본 메시지(`defaultMessage`)를 사용하면 기본 메시지가 반환됨
- `args`
  - 메시지의 `{0}`부분에 매개 변수를 전달해서 치환할 수 있음
    - `hello.name=안녕 {0}` ➔ Spring 단어를 매개변수로 전달 ➔ `안녕 Spring`
- `locale`
  - 국제화 파일 선택
  - 구체적인 것에서 디폴트 순으로 탐색
    - `Locale`이 `en_US`의 경우,  `messages_en_US` ➔ `messages_en` ➔ `messages`
  - `locale`정보가 없는 경우, `Locale.getDefault()`을 호출해서 시스템의 기본 로케일을 사용
    - `locale = null` 인 경우, 시스템 기본 `locale`이 `ko_KR`이므로 `messages_ko.properties` 조회
      시도 ➔ 조회 실패 ➔ `messages.properties` 조회

      
<br/>

## 웹 애플리케이션에 메시지 적용

- `messages.properties`에 메시지 등록
- 타임리프 메시지 적용
  - `#{...}`: 타임리프 메시지 표현식
    - 스프링의 메시지를 편하게 조회 가능
  - 파라미터 사용
    - `hello.name=안녕 {0}`
    - `#{hello.name(${item.itemName})}`
    

<br/>