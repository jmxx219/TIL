# Spring MVC

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

---

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