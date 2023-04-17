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
