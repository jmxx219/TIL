# Spring MVC

### 목차
- [로깅](#로깅)
- [요청 매핑](#요청-매핑)
- [HTTP 요청](#HTTP-요청)
- [HTTP 응답](#HTTP-응답)
- [HTTP 메시지 컨버터](#HTTP-메시지-컨버터)
- [요청 매핑 핸들러 어댑터 구조](#요청-매핑-핸들러-어댑터-구조)

<br/>

## 로깅

운영 시스템에서는 시스템 콘솔(`System.out.println()`)을 사용하지 않고 별도의 로깅 라이버러리를 사용해서 로그를 출력함

### **로깅 라이브러리**

스프링 부트 라이브러리를 사용하면 스프링 부트 로깅 라이브러리가 함께 포함됨
- `SLF4J`
  - Logback, Log5j, Log4J2 등 수많은 라이브러리를 통합해서 인터페이스로 제공
  - `SLF4J`는 인터페이스이고, 구현체로 `Logback` 같은 로그 라이브러리를 선택하여 사용
- 로그 선언
  - `@Slf4j` // 롬복 사용
  - `private final Logger log = LoggerFactory.getLogger(getClass());`
  - `private final Logger log = LoggerFactory.getLogger(LogTestController.class);`
- 로그 호출
  - `log.info("info log={}", name);`
  - `System.out.println("name = " + name);`
- 로그 레벨 설정
  - LEVEL: `TRACE > DEBUG > INFO > WARN > ERROR`
    - 개발 서버는 debug, 운영 서버는 info를 출력함
  - `application.properties` 파일에서 로그 레벨을 설정
    - 전체 로그 레벨 설정(기본 info) : `logging.level.root=info`
    - hello.springmvc 패키지와 그 하위 로그 레벨 설정: `logging.level.hello.springmvc=debug`
- 로그 사용
  - 쓰레드 정보, 클래스 이름과 같은 부가 정보를 함께 볼 수 있음
  - 로그 레벨에 따라 개발 서버와 운영 서버 상황에 맞게 로그를 조절할 수 있음
  - 콘솔뿐만 아니라 파일(로그 분할 가능)이나 네트워크 등 로그를 별도의 위치에 남길 수 있음
  - 일반 출력보다 성능이 더 좋음 -> 실무에서는 꼭 로그를 사용!
    ```java
    @Slf4j
    @RestController
    public class LogTestController {
        @RequestMapping("/log-test")
        public String logTest() {
            String name = "Spring";
    
    //        System.out.println("name = " + name);
    //        log.trace("trace my log=", name); // 이렇게 사용 X -> 의미 없는 연산 발생
    
            log.trace("trace log={}", name);
            log.debug("debug log={}", name);
            log.info("info log={}", name);
            log.warn("warn log={}", name);
            log.error("error log={}", name);
    
            return "ok";
        }
    }
    ```

<br/>


## 요청 매핑

### 매핑 정보

`@RestController`
- `@Controller` 사용 시, 반환 값이 String이면 뷰 이름으로 인식 -> 뷰를 찾고 뷰가 렌더링됨
- `@RestController`는 반환 값으로 뷰를 찾지 않고, HTTP 메시지 바디에 바로 입력
  - 실행 결과로 메시지를 받을 수 있음

`@RestMappling("/hello-basic")`
- `/hello-basic` 또는 `/hello-basic/` URL이 호출되면 해당 메서드가 실행되도록 매핑함
- 대부분의 속성을 배열로 제공하기 때문에 다중 설정 가능 `{"/hello-basic", "/hello-go"}`
- HTTP 메서드 모두 허용(GET, POST, HEAD, PUT, PATCH, DELETE)
  - `method` 속성 지정 : `@RequestMapping(value = "/mapping-get", method = RequestMethod.GET)`
  - 지정한 HTTP 메서드와 다르게 요청할 경우, `HTTP 405 상태코드(Method Not Allowed)`를 반환함
  - HTTP 메서드 매핑 축약
    - `@GetMapping` `@PostMapping` `@PutMapping` `@DeleteMapping` `@PatchMapping`
    - 애노테이션을 사용하는 것이 더 직관적
    - 코드 내부적으로 `@RequestMapping`과 `method`를 지정하고 있음

`@PathVariable`
- 최근 HTTP API는 리소스 경로에 식별자를 넣는 스타일을 선호
  - `/mapping/userA`, `/users/1`
- `@RestMappling`은 URL 경로를 템플릿화 할 수 있음. 이때 `@PathVariable`를 사용하여 매칭 되는 부분을 편리하게 조회 가능
- `@PathVariable`의 이름과 파라미터 이름이 같으면 생략 가능
  - `@PathVariable("userId") String userId` -> `@PathVariable userId`
  ```java
  @GetMapping("/mapping/users/{userId}/orders/{orderId}")
  public String mappingPath(@PathVariable String userId, @PathVariable Long orderId) {
      log.info("mappingPath userId={}, orderId={}", userId, orderId);
      return "ok";
  }
  ```

**조건 매핑**
- 파라미터
  - `@GetMapping(value = "/mapping-param", params = "mode=debug")`
  - 특정 파라미터가 있거나 없는 조건을 추가 가능
- 헤더
  - `@GetMapping(value = "/mapping-header", headers = "mode=debug")`
  - HTTP 헤더 사용
- 미디어 타입 - HTTP 요청
  - Content-Type, consume
    - `@PostMapping(value = "/mapping-consume", consumes = MediaType.APPLICATION_JSON_VALUE)`
    - HTTP 요청의 Content-Type 헤더를 기반으로 Media Type으로 매핑
    - 타입이 맞지 않으면 `HTTP 415 상태코드(Unsupported Media Type)` 반환
  - Accept, produce
    - `@PostMapping(value = "/mapping-produce", produces = MediaType.TEXT_HTML_VALUE)`
    - HTTP 요청의 Accept 헤더를 기반으로 Media Type으로 매핑
    - 타입이 맞지 않으면 `HTTP 406 상태코드(Not Acceptable)` 반환

<br/>

## HTTP 요청

### 헤더 조회
```java
@RequestMapping("/headers")
public String headers(
    HttpServletRequest request,
    HttpServletResponse response,
    HttpMethod httpMethod,  // HTTP 메서드를 조회
    Locale locale,          // Locale 정보를 조회
    @RequestHeader MultiValueMap<String, String> headerMap, // 모든 HTTP 헤더를 MultiValueMap 형식으로 조회
    @RequestHeader("host") String host,     // 특정 HTTP 헤더를 조회
    @CookieValue(value = "myCookie", required = false) String cookie  // 특정 쿠키를 조회
    ) {
}
```

<br/>

### 요청 파라미터

> **클라이언트에서 서버로 요청 데이터를 전달하는 방법**
> - `GET - 쿼리 파라미터` : 메시지 바디 없이 URL의 쿼리 파라미터에 데이터를 포함해서 전달
> - `POST - HTML Form` : 메시지 바디에 쿼리 파라미터 형식으로 전달
> - `HTTP message body` : 데이터를 직접 담아서 요청

- 쿼리파라미터, HTML Form
  - `HttpServletRequest`의 `request.getParameter()`로 GET, POST 구분없이 조회 가능
- `@RequestParam`
  ```java
  @ResponseBody // View 조회를 무시하고 HTTP message body에 직접 입력
  @RequestMapping("/request-param")
  public String requestParam(@RequestParam String username, @RequestParam int age) {
      return "ok"; // Http 응답 메시지로 바로 반환
  }
  ```
  - 파라미터 이름으로 바인딩
  - HTTP 파라미터 이름이 변수 이름과 같으면 생략 가능
    - `@RequestParam("username") String username` -> `@RequestParam String username`
  - String, int 등 단순 타입이면 `@RequestParam`도 생략 가능
  - `@RequestParam(required = true, defaultValue = "guest")`
    - `required`: 파라미터 필수 여부, 기본값은 `true`
    - `defaultValue` : 파라미터에 값이 없는 경우(null, 빈문자) 기본값 적용 가능
  - `@RequestParam Map`, `@RequestParam MultiValueMap`으로 파라미터 조회 가능
- `@ModelAttribute`
  - 요청 파라미터를 받아 필요한 객체를 만들고, 그 객체에 값을 넣어주는 과정을 자동화해줌
  - 요청 파라미터를 바인딩 받을 객체 생성
      ```java
    @Data
    public class HelloData {    
        private String username;
        private int age;
    }
      ```
    - 롬복 `@Data` : `@Getter`, `@Setter`, `@ToString`, `@EqualsAndHashCode`, `@RequiredArgsConstructor` 자동 적용
  - Spring MVC는 `@ModelAttribute`가 있을 때 `HelloData` 객체를 생성하고, 요청 파라미터의 이름으로 `HelloData` 객체의 프로퍼티를 찾아 해당 프로퍼티의 setter를 호출하여 파라미터 값을 바인딩함
    ```java
    @ResponseBody
    @RequestMapping("/model-attribute-v1")
    public String modelAttributeV1(@ModelAttribute HelloData helloData) {
        log.info("username={}, age={}", helloData.getUsername(), helloData.getAge());
        return "ok";
    }
    ```
  - `@ModelAttribute` 생략 가능 but `@RequestParam`도 생략 가능하기 때문에 혼란이 발생할 수 있음
    - `String`, `int`, `Integer`와 같은 단순 타입은 `@RequestParam`로, 나머지는 `@ModelAttribute`로 적용함

<br/>

### 요청 메시지

요청 파라미터와 다르게 HTTP message body에 데이터를 담아 요청이 오는 경우는 `@ReqeustParam`이나 `@ModelAttribute` 사용 불가능

- **단순 텍스트**
  1. `InputStream`
     - `InputStream(Reader)`: HTTP 요청 메시지 바디의 내용을 직접 조회
     - `OutputStream(Writer)`: HTTP 응답 메시지의 바디에 직접 결과 출력
       ```java
       @PostMapping("/request-body-string-v1")
       // public void requestBodyJsonV1(HttpServletRequest request, HttpServletResponse response) throws IOException{
       public void requestBodyStringV2(InputStream inputStream, Writer responseWriter) throws IOException {
           // ServletInputStream inputStream = request.getInputStream();
           String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
           //response.getWriter().write("ok");
           responseWriter.write("ok");
       }
       ```

  2. `HttpEntity`
     - HTTP header, body 정보를 편리하게 조회 가능
       - `HttpMessageConverter` 사용 -> `StringHttpMessageConverter` 적용
     - 메시지 바디를 직접 조회하기 때문에 요청 파라미터를 조회하는 `@ReqeustParam`과 `@ModelAttribute`와 관계 없음
     - 응답에도 사용 가능 -> view 조회하지 않고 메시지 바디 정보를 직접 반환
       ```java
       @PostMapping("/request-body-string-v3")
       public HttpEntity<String> requestBodyStringV3(HttpEntity<String> httpEntity) {
           String messageBody = httpEntity.getBody();
           return new HttpEntity<>("ok");
       }
       ```
     - `HttpEntity`를 상속 받은 객체
       - `RequestEntity`: HttpMethod, URL 정보 추가 가능
       - `ResponseEntity`: Http 상태코드 설정 가능
         - `return new ResponseEntity<>("ok", HttpStatus.CREATED);`
  3. `@RequestBody`, `@ResponseBody`
     - `@RequestHeader`: 헤더 정보 조회
     - 메시지 바디 정보를 직접 조회하거나 반환할 때 사용
       - `HttpMessageConverter` 사용 -> `StringHttpMessageConverter` 적용
        ```java
        @ResponseBody
        @PostMapping("/request-body-string-v5")
        public String requestBodyStringV5(@RequestBody String messageBody) {
            log.info("messageBody={}", messageBody);
            return "ok";
        }
        ```
- **JSON** - HTTP API에서 주로 사용하는 데이터 형식
  1. `ObjectMapper`
     - messageBody를 `ObjectMapper`를 이용하여 자바 객체로 변환
        ```java
        private ObjectMapper objectMapper = new ObjectMapper();
        
        @ResponseBody
        @PostMapping("/request-body-json-v2")
        public String requestBodyJsonV2(@RequestBody String messageBody) throws IOException  {
            HelloData helloData = objectMapper.readValue(messageBody, HelloData.class);
            log.info("username={}, age={}", helloData.getUsername(), helloData.getAge());
            return "ok";
        }
       ```
  2. `@RequestBody`, `HttpEntity`
      - HTTP 메시지 컨버터가 HTTP 메시지 바디의 내용을 원하는 문자나 객체로 변환해줌(JSON 객체 포함)
        - HTTP 요청 시 `content-type`이 `application/json`이여야만 JSON을 처리할 수 있는 HTTP 메시지 컨버터가 실행됨
        ```java
        @ResponseBody
        @PostMapping("/request-body-json-v5")
        public HelloData requestBodyJsonV5(@RequestBody HelloData helloData) {
            return helloData;
        }
        ```
      - `@RequestBody` - `(content-type: application/json)`
        - JSON 요청 -> HTTP 메시지 컨버터 -> 객체
        - 생략 불가능(생략 할 경우 `@ModelAttribute`가 적용되어 요청 파라미터를 처리하게 됨)
      - `@ResponseBody` - `(Accept: application/json)`
        - 객체 -> HTTP 메시지 컨버터 -> JSON 요청 
        - 해당 객체를 HTTP 메시지 바디에 직접 넣을 수 있음
      - `HttpEntity`도 사용 가능

<br/>

## HTTP 응답

스프링에서 응답 데이터를 만드는 방법

### 정적 리소스
- 웹 브라우저에서 정적인 HTML, CSS, JS를 제공할 때 사용
- 스프링 부트는 클래스패스의 다음 디렉토리에 있는 정적 리소스를 제공
  - `/static`, `/public`, `/resources`, `/META-INF/recources`
- `src/main/resources`: 리소스를 보관하는 이며 클래스패스의 시작 경로
  - 해당 경로에 리소스를 넣어두면 스프링 부트가 정적 리소스로 서비스(파일 변경없이 그대로)를 제공
  - `src/main/resources/static`: 정적 리소스 경로

<br/>

### 뷰 템플릿
- 웹 브라우저에 동적인 HTML을 제공할 때 사용
- 뷰 템플릿을 거쳐서 HTML이 생성되고, 뷰가 응답을 만들어서 전달
- `src/main/resources/templates`: 스프링 뷰트가 제공하는 기본 뷰 템플릿 경로
    ```java
    @Controller
    public class ResponseViewController {
    
        @RequestMapping("/response-view-v1")
        public ModelAndView responseViewV1() {
            ModelAndView mv = new ModelAndView("response/hello").addObject("data", "hello!");
            return mv;
        }
    
        @RequestMapping("/response-view-v2")
        public String responseViewV2(Model model) {
            model.addAttribute("data", "hello!");
            return "response/hello"; // 뷰의 논리 이름
        }
    }
    ```
  1. ModelAndView 반환
  2. String 반환 - View or HTTP 메시지
     - `@ResponseBody`가 없으면 `response/hello`로 뷰 리졸버 실행되어 뷰를 찾고 렌더링함
       - 뷰의 논리 이름 반환 시 `templates/response/hello.html` 경로의 뷰 템플릿이 렌더링됨
     - `@ResponseBody`가 있으면 HTTP 메시지 바디에 직접 `response/hello` 문자 입력

<br/>

### HTTP API, 메시지 바디에 직접 입력
- HTTP API를 제공하는 경우 데이터를 전달하기 때문에 HTTP 메시지 바디에 JSON과 같은 형식으로 데이터를 보냄
    1. `HttpServletResponse`
       - 객체에 직접 응답 메시지 전달
       - `response.getWriter().write("ok");`
    2. `ResponseEntity`
       - HTTP 응답 코드 설정 가능
       - String: `return new ResponseEntity<>("ok", HttpStatus.OK);`
       - JSON: `return new ResponseEntity<>(helloData, HttpStatus.OK)`;
    3. `@ResponseBody`
       - view를 사용하지 않고 HTTP 메시지 컨버터를 통해서 HTTP 메시지를 직접 입력
       - 응답 코드를 설정하고 싶을 경우 `@ResponseStatus(HttpStatus.OK)` 함께 사용 가능
       - String: `return "ok";`
       - JSON: `return helloData;`
- `@RestController`
  - `@Controller` + `@ResponseBody`
  - 해당 컨트롤러에 모두 `@ResponseBody`가 적용됨
  - 뷰 템플릿이 아닌 HTTP 메시지 바디에 직접 데이터 입력


<br/>

## HTTP 메시지 컨버터

HTTP API처럼 JSON 데이터를 HTTP 메시지 바디에 직접 읽거나 쓰는 경우 사용하면 편리

### 스프링 MVC의 HTTP 메시지 컨버터 적용
- HTTP 요청: `@RequestBody`, `HttpEntity(RequestEntity)`
- HTTP 응답: `@ResponseBody`, `HttpEntity(ResponseBody)`

<br/>

### HTTP 메시지 컨버터 인터페이스

- **HTTP 요청 데이터 읽기**
  - `canRead()` : 메시지 컨버터가 해당 메시지를 읽을 수 있는지 확인
    - 대상 클래스 타입 지원 확인
      - ex) `@RequestBody`의 대상 클래스 (`byte[]`, `String`, `HelloData`)
    - HTTP 요청의 Content-Type 미디어 타입 지원 확인
      - ex) `text/plain`, `application/json`, `*/*`
  - `read()` : 메시지를 읽는 기능(객체를 생성하고 반환함)
- **HTTP 응답 데이터 생성**
  - `canWrite()` : 메시지 컨버터가 메시지를 쓸 수 있는지 확인
      - 대상 클래스 타입 지원 여부
        - ex) return의 대상 클래스 (`byte[]`, `String`, `HelloData`)
      - HTTP 요청의 Accept 미디어 타입을 지원 여부(`@RequestMapping`의 `produces`)
        - ex) `text/plain`, `application/json`, `*/*`
  - `write()` : 메시지를 쓰는 기능(HTTP 응답 메시지 바디에 데이터 생성)
  
<br/>

### 스프링 부트 기본 메시지 컨버터

다양한 메시지 컨버터를 제공하는데, 대상 클래스 타입과 미디어 타입을 체크해서 사용여부를 결정함. 만족하지 않으면 다음 우선순위로 넘어감

1. ByteArrayHttpMessageConverter
    - 클래스 타입: `byte[]`, 미디어타입: `*/*`
      - 요청: `@RequestBody byte[] data`
      - 응답: `@ResponseBody return byte[]`, 쓰기 미디어 타입 - `application/octet-stream`
2. StringHttpMessageConverter
    - 클래스 타입: `String`, 미디어타입: `*/*`
      - 요청: `@RequestBody String data`
      - 응답: `@ResponseBody return "ok"`, 쓰기 미디어 타입 - `text/plain`
3. MappingJackson2HttpMessageConverter
    - 클래스 타입: `객체` 또는 `HashMap`, 미디어타입: `application/json`
        - 요청: `@RequestBody HelloData data`
        - 응답: `@ResponseBody return helloData`, 쓰기 미디어 타입 - `application/json`

<br/>

## 요청 매핑 핸들러 어댑터 구조

### RequestMappingHandlerAdapter

`@RequestMapping`을 처리하는 핸들러 어댑터로 애노테이션 기반 컨트롤러를 처리함

- `ArgumentResolver`
  - HTTP 메시지를 처리해서 요청 데이터 생성(`@RequestBody`, `HttpEntity`)
    - HTTP 메시지 컨버터를 사용하여 컨트롤러(핸들러)가 필요로하는 다양한 파라미터의 값(객체)을 생성
    - 파라미터의 값이 준비가 되면 컨트롤러를 호출하여 값을 넘겨줌
- `ReturnValueHandler`
  - 응답 데이터를 HTTP 메시지에 입력(`@ResponseBody`, `HttpEntity`)
    - HTTP 메시지 컨버터를 호출해서 응답 결과를 생성

