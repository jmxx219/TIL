# 예외처리

### 목차
- [서블릿 예외 처리](#서블릿-예외-처리)
  - [Exception과 sendError()](#서블릿-예외-처리-방식)
  - [오류 화면 제공](#오류-화면-제공)
  - [오류 페이지 작동 원리](#오류-페이지-작동-원리)
  - [필터](#필터)
  - [인터셉터](#인터셉터)
- [스프링 부트 오류 페이지](#스프링-부트-오류-페이지)
- [API 예외 처리](#API-예외-처리)
  - [서블릿 오류 페이지 방식](#서블릿-오류-페이지-방식)
  - [스프링 부트 기본 오류 처리](#스프링-부트-기본-오류-처리)
  - [HandlerExceptionResolver](#HandlerExceptionResolver)


<br/>

## 서블릿 예외 처리

스프링이 아닌 순수 서블릿 컨테이너의 예외 처리

### 서블릿 예외 처리 방식

**Exception(예외)**

- 자바 직접 실행
  - 자바의 메인 메서드를 직접 실행하는 경우, `main`이라는 이름의 쓰레드 실행
  - 실행 도중에 예외를 잡지 못하고 처음 실행한 `main()` 메서드를 넘어서 예외가 던겨질 경우, 예외 정보를 남기고 해당 쓰레드는 종료됨
- 웹 애플리케이션
    ```java
    @GetMapping("/error-ex")
    public void errorEx() {
        throw new RuntimeException("예외 발생!"); 
    }
    ```
  - 웹 애플리케이션은 사용자 요청별로 별도의 쓰레드 할당되고, 서블릿 컨테이너 안에서 실행됨
  - 애플리케이션에서 예외가 발생한 경우, 어디선가 `try ~ catch`로 예외를 잡아서 처리하면 문제가 없음
  - 애플리케이션에서 예외를 잡지 못하고 서블릿 밖으로까지 예외가 전달될 경우, 톰캣과 같은 `WAS`까지 예외가 전달됨
    - `WAS(여기까지 전파) ⬅ 필터 ⬅ 서블릿 ⬅ 인터셉터 ⬅ 컨트롤러(예외발생)`
- 오류 화면
  - 예외가 `WAS`까지 오면 `tomcat`이 기본으로 제공하는 오류 화면을 볼 수 있음
    - `HTTP Status 500 – Internal Server Error`
      - `Exception`의 경우에는 서버 내부에서 처리할 수 없는 오류가 발생한 것으로 생각해서 상태 코드 500을 반환함
    - `HTTP Status 404 – Not Found`

**response.sendError(HTTP 상태 코드, 오류 메시지)**

- `HttpServletResponse`가 제공
    ```java
    @GetMapping("/error-404")
    public void error404(HttpServletResponse response) throws IOException {
        response.sendError(404, "404 오류!"); 
    }
    ```
  - 당장 예외가 발생하지 않고, 서블릿 컨테이너에게 오류가 발생했다는 것을 전달할 수 있음
  - HTTP 상태 코드와 오류 메시지 추가 가능
- `sendError()` 흐름
  - `WAS(sendError 호출 기록 확인) ⬅ 필터 ⬅ 서블릿 ⬅ 인터셉터 ⬅ 컨트롤러(response.sendError())`
    - `response.sendError()`를 호출하면 `response` 내부에 오류가 발생했다는 상태를 저장
    - **서블릿 컨테이너**는 고객에게 응답하기 전에 `response`에 `sendError()`가 호출되었는지 확인함
    - 호출되었다면 설정한 오류 코드에 맞추어 기본 오류 페이지를 보여줌
      - 오류 화면: `HTTP Status 404 – Bad Request`


<br/>

### 오류 화면 제공

서블릿은 `Exception`(예외)가 발생해서 서블릿 밖으로 전달되거나 또는 `response.sendError()`가 호출되었을 때, 각각의 상황에 맞춘 오류 처리 기능을 제공함

- 과거에는 `web.xml` 파일에 오류 화면을 등록했음
- 지금은 스프링 부트를 통해 서블릿 컨테이너를 실행 ➡ 스프링 부트가 제공하는 기능을 사용해서 서블릿 오류 페이지를 등록함

**서블릿 오류 페이지 등록**
- 오류페이지는 예외를 다룰 때 해당 예외와 그 자식 타입의 오류를 함께 처리함
  ```java
  @Component
  public class WebServerCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
      @Override
      public void customize(ConfigurableWebServerFactory factory) {
          ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND, "/error-page/404");
          ErrorPage errorPage500 = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error-page/500");
          ErrorPage errorPageEx = new ErrorPage(RuntimeException.class, "/error-page/500");
          factory.addErrorPages(errorPage404, errorPage500, errorPageEx);
      }
  }
  ```
  - `response.sendError(404)`: `errorPage404` 호출 
  - `response.sendError(500)`: `errorPage500` 호출 
  - `RuntimeException` 또는 그 자식 타입의 예외: `errorPageEx` 호출

- 오류 처리 컨틀롤러
  ```java
    @Slf4j
    @Controller
    public class ErrorPageController {
        @RequestMapping("/error-page/404")
        public String errorPage404(HttpServletRequest request, HttpServletResponse response) {
            return "error-page/404";
        }
        
        @RequestMapping("/error-page/500")
        public String errorPage500(HttpServletRequest request, HttpServletResponse response) {
            return "error-page/500";
        }
    }
  ```
  - `RuntimeException` 예외가 발생하면 `errorPageEx`에서 지정한 `/error-page/500`이 호출됨

<br/>

### 오류 페이지 작동 원리

1. 예외 발생 흐름
   - `WAS(여기까지 전파) ⬅ 필터 ⬅ 서블릿 ⬅ 인터셉터 ⬅ 컨트롤러(예외 발생)`
   - `sendError` 흐름
     - `WAS(sendError 호출 기록 확인) ⬅ 필터 ⬅ 서블릿 ⬅ 인터셉터 ⬅ 컨트롤러(response.sendError())`
       - `WAS`는 해당 예외를 처리하는 오류 페이지 정보를 확인함
       - `new ErrorPage(RuntimeException.class, "/error-page/500")`
2. 오류 페이지 요청 흐름
   - `WAS('/error-page/500' 다시 요청) ➜ 필터 ➜ 서블릿 ➜ 인터셉터 ➜ 컨트롤러('/error-page/500') ➜ View`

> 웹 브라우저(클라이언트)는 서버 내부에서 이런 일이 일어나는지 전혀 모른다.  
> 오직 서버 내부에서 오류 페이지를 찾기 위해 추가적인 호출을 한다.

**오류 정보 추가**
- WAS는 오류 페이지를 단순히 요청만 하는 것이 아닌, 오류 정보를 `request`의 `attribute`에 추가해서 넘겨줌
  - 필요하면 오류 페이지에서 전달된 오류 정보 사용 가능
  - `request.getAttribute(ERROR_EXCEPTION))`
    - `ERROR_EXCEPTION`: 예외
    - `ERROR_EXCEPTION_TYPE`: 예외 타입
    - `ERROR_MESSAGE`: 오류 메시지
    - `ERROR_REQUEST_URI`: 클라이언트 요청 URI
    - `ERROR_SERVLET_NAME`: 오류가 발생한 서블릿 이름
    - `ERROR_STATUS_CODE`: HTTP 상태 코드

<br/>

### 필터

**예외 발생과 오류 페이지 요청 흐름**
1. `WAS(여기까지 전파) ⬅ 필터 ⬅ 서블릿 ⬅ 인터셉터 ⬅ 컨트롤러(예외 발생)`
2. `WAS('/error-page/500' 다시 요청) ➜ 필터 ➜ 서블릿 ➜ 인터셉터 ➜ 컨트롤러('/error-page/500') ➜ View`
- 오류가 발생하면 오류 페이지를 출력하기 위해 WAS 내부에서 다시 한 번 호출이 발생함
  - 이때 필터, 서블릿, 인터셉터 모두 다시 호출됨
    - 로그인 인증 체크 같은 경우, 한 번 필터나 인터셉터에서 체크를 완료했음
  - 서버 내부에서 오류 페이지를 호출한다고 해당 필터나 인터셉터가 다시 한 번 호출되는 것은 매우 비효율적임
- 클라이언트로 발생한 정상 요청인지, 오류 페이지 출력을 위한 내부 요청인지 구분할 필요가 있음
  - 서블릿은 `DispatcherType`이라는 추가 정보를 제공하여 해당 문제를 해결함

**DispatcherType**
  ```java
  public enum DispatcherType {
        FORWARD, INCLUDE, REQUEST, ASYNC, ERROR
  }
  ```
  - `REQUEST`: 클라이언트 요청
  - `ERROR`: 오류 요청
  - `FORWARD`: MVC에서 배웠던 서블릿에서 다른 서블릿이나 JSP를 호출할 때 `RequestDispatcher.forward(request, response)` 
  - `INCLUDE`: 서블릿에서 다른 서블릿이나 JSP의 결과를 포함할 때 `RequestDispatcher.include(request, response)` 
  - `ASYNC`: 서블릿 비동기 호출
  
**로그 필터 설정**

[참고](https://github.com/jmxx219/Spring-Study/blob/main/login/README.md#%EC%9A%94%EC%B2%AD-%EB%A1%9C%EA%B7%B8)
- `filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR)`
  - 클라이언트 요청과 오류 페이지 요청에서 해당 필터가 호출됨
  - 아무것도 넣지 않으면 기본 값은 `DispatcherType.REQUEST`
    - 클라이언트의 요청이 있는 경우에만 필터가 적용됨



<br/>

### 인터셉터

- 필터의 경우, 등록할 때 어떤 `DispatcherType`인 경우에 따라 필터를 적용 여부를 선택할 수 있었음
- 인터셉터는 서블릿이 제공하는 기능이 아닌, 스프링이 제공하는 기능
  - `DispatcherType`과 무관하게 항상 호출됨
- 인터셉터는 요청 경로에 따라서 추가하거나 제거하기 쉽게 되어 있음
  - 해당 설정을 사용해서 오류 페이지 경로에 `excludePathPatterns`를 사용하여 뺄 수 있음

**로그 인터셉터 설정**

[참고](https://github.com/jmxx219/Spring-Study/blob/main/login/README.md#%EC%9D%B8%ED%84%B0%EC%85%89%ED%84%B0-%EC%9A%94%EC%B2%AD-%EB%A1%9C%EA%B7%B8)
- `excludePathPatterns("/error-page/**")`
  - `/error-page/**`를 제거하면 `error-page/500` 같은 내부 호출의 경우에도 인터셉터가 호출됨

**전체 흐름**
- `/hello` 정상 요청
  - `WAS(/hello, dispatchType=REQUEST) ➜ 필터 ➜ 서블릿 ➜ 인터셉터 ➜ 컨트롤러 ➜ View`
- `/error-ex` 오류 요청
  - 필터: `DispatchType`으로 중복 호출 제거 (`dispatchType=REQUEST`)
  - 인터셉터: 경로 정보로 중복 호출 제거(`excludePathPatterns("/error-page/**")`)
  
1. `WAS(/error-ex, dispatchType=REQUEST) ➜ 필터 ➜ 서블릿 ➜ 인터셉터 ➜ 컨트롤러`
2. `WAS(여기까지 전파) ⬅ 필터 ⬅ 서블릿 ⬅ 인터셉터 ⬅ 컨트롤러(예외발생)`
3. `WAS 오류 페이지 확인`
4. `WAS(/error-page/500, dispatchType=ERROR) ➜ 필터(x) ➜ 서블릿 ➜ 인터셉터(x) ➜ 컨트롤러(/error-page/500) ➜ View`

<br/>

## 스프링 부트 오류 페이지

### 스프링 부트 예외 처리

- 서블릿 예외 처리와 같은 과정을 모두 기본으로 제공함
  - `WebServerCustomizer`을 만들고 예외 종류에 따라 `ErrorPage` 추가, 예외 처리용 컨트롤러 `ErrorPageController` 생성
- `ErrorPage`는 자동으로 등록함(`/error` 경로로 기본 오류 페이지를 설정)
  - `new ErrorPage("/error")`, 상태코드와 예외를 설정하지 않으면 기본 오류 페이지로 사용
  - 서블릿 밖으로 예외가 발생하거나, `response.sendError()`가 호출되면 모든 오류는 `/error`를 기본으로 호출함
- `BasicErrorController`라는 스프링 컨트롤러를 자동으로 등록함
  - `ErrorPage`에서 등록한 `/error`를 매핑해서 처리하는 컨트롤러
  - 기본적인 로직은 모두 개발되어 있기 때문에 개발자는 오류 페이지만 등록하면 됨
    - 제공하는 롤과 우선 순위에 따라 등록
    - 정적 HTML이면 정적 리소스, 동적 오류 화면이면 뷰 템플릿 경로에 오류 페이지 파일을 만들기만 하면 됨

**뷰 선택 우선 순위**

- `BasicErrorController`의 처리 순서
  1. 뷰 템플릿
     - `resources/templates/error/500.html`
     - `resources/templates/error/5xx.html`
  2. 정적 리소스(`static`, `public`)
     - `resources/static/error/400.html`
     - `resources/static/error/404.html`
     - `resources/static/error/4xx.html`
  3. 적용 대상이 없을 때 뷰 이름(`error`)
     - `resources/templates/error.html`


#### BasicErrorController가 제공하는 기본 정보들
- 다음 정보를 `model`에 담아서 뷰로 전달함
  ```html
  * timestamp: Fri Feb 05 00:00:00 KST 2021
  * status: 400
  * error: Bad Request
  * exception: org.springframework.validation.BindException * trace: 예외 trace
  * message: Validation failed for object='data'. Error count: 1 * errors: Errors(BindingResult)
  * path: 클라이언트 요청 경로 (`/hello`)
  ```
  - 오류 관련 내부 정보들은 고객에게 노출하지 않는 것이 좋음
- `BasicErrorController`에서 다음 오류 정보를 `model`에 포함할지 여부 선택 가능
  - `application.properties`
  ```
  server.error.include-exception=false       // exception 포함 여부(true, false)
  server.error.include-message=never         // message 포함 여부
  server.error.include-stacktrace=never      // trace 포함 여부
  server.error.include-binding-errors=never  // errors 포함 여부
  ```
  - 기본 값이 `never`인 부분은 다음 3가지 옵션 사용 가능
    - `never`: 사용하지 않음 
    - `always`:항상 사용 
    - `on_param`: 파라미터가 있을 때 사용
      - 파라미터가 있으면 해당 정보를 노출함
      - 디버그 시 문제를 확인하기 위해 사용 가능
      - 개발 서버에서는 사용할 수 있지만, 운영 서버에서는 권장 x

**스프링 부트 오류 관련 옵션**
- `server.error.whitelabel.enabled=true`
  - 오류 처리 화면을 못 찾을 시, 스프링 whitelabel 오류 페이지 적용
- `server.error.path=/error`
  - 오류 페이지 경로
  - 스프링이 자동 등록하는 서블릿 글로벌 오류 페이지 경로와 `BasicErrorController` 오류 컨트롤러 경로에 함께 사용됨

**확장**
- 에러 공통 처리 컨트롤러의 기능을 변경하고 싶은 경우
  - `ErrorController` 인터페이스를 상속 받아서 구현
  - `BasicErrorController

> 스프링 부트가 기본으로 제공하는 오류 페이지를 활용하면 오류 페이지와 관련된 대부분의 문제가 손쉽게 해결됨

<br/>

## API 예외 처리

- HTML 페이지의 경우, 4xx와 5xx와 같은 오류 페이지만 있으면 대부분의 문제 해결 가능
- API 경우, 각 오류 상황에 맞는 오류 응답 스펙을 정하고 JSON으로 데이터를 주어야 함

<br/>

### 서블릿 오류 페이지 방식

- `WebServerCustomizer`
  - `WAS`에 예외가 전달되거나, `response.sendError()`가 호출되면, 위에 등록한 예외 페이지 경로가 호출됨
- `ApiExceptionController`
  - API 예외 컨트롤러로, URL에 전달된 `id` 값이 `ex`이면 예외가 발생함
- `ErrorPageController`
  - API 응답 추가
  ```java
  @RequestMapping(value = "/error-page/500", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Object>> errorPage500Api(HttpServletRequest request, HttpServletResponse response) {
      log.info("API errorPage 500");
  
      HashMap<String, Object> result = new HashMap<>();
      Exception ex = (Exception) request.getAttribute(ERROR_EXCEPTION);
      result.put("status", request.getAttribute(ERROR_STATUS_CODE));
      result.put("message", ex.getMessage());
  
      Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
      return new ResponseEntity<>(result, HttpStatus.valueOf(statusCode));
  }
  ```
  - `produces = MediaType.APPLICATION_JSON_VALUE`
    - HTTP Header의 `Accept` 값이 `application/json` 경우, 해당 메서드가 호출됨
    - 클라이언트가 받고 싶은 미디어 타입이 `JSON`이면 해당 컨트롤러의 메서드가 호출됨
  - 응답 데이터를 위에 `Map`을 만들고 `status`, `message` 키에 값을 할당
  - `ResponseEntity`을 이용하여 응답하기 때문에 메시지 컨버터가 동작하면서 클라이언트에 `JSON`이 반환됨
- API 요청
  - 정상 호출
    - `http://localhost:8080/api/members/spring`
    - API로 JSON 형식으로 데이터가 정상 반환됨
  - 예외 발생 호출
    - `http://localhost:8080/api/members/ex`
    - HTTP Header에 `Accept`가 `application/json`인 경우, JSON 형식으로 데이터가 정상 반환됨
      ```json
      {
          "message": "잘못된 사용자",
          "status": 500
      }
      ```
    - HTTP Header에 `Accept`가 `application/json`이 아닌 경우, 미리 만들어둔 기존 오류 페이지 HTML이 반환됨

<br/>

### 스프링 부트 기본 오류 처리

**스프링 부트의 예외 처리**
- 스프링 부트의 기본 설정은 오류 발생 시 `/error`를 오류 페이지로 요청함
- `BasicErrorController`는 해당 경로를 기본으로 받음(`server.error.path`로 수정 가능, 기본 경로 `/error`)

**`BasicErrorController`**
  - API 예외처리도 스프링 부트가 제공하는 기본 오류 방식을 사용할 수 있음 
    - `BasicErrorController`를 사용하기 위해 `WebServerCustomizer`의 `@Component`는 주석처리함
  ```java
  @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
  public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {}
  
  @RequestMapping
  public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {}
  ```
  - `/error` 동일한 경로를 처리하는 `errorHtml()과 `error()` 두 메서드 존재
    - `errorHtml()`: 클라이언트 요청의 Accept 해더 값이 `text/html`인 경우에 호출해서 view를 제공
    - `error()`: 그외 경우에 호출되고 `ResponseEntity`로 HTTP Body에 `JSON` 데이터를 반환함
  - 스프링 부트는 [BasicErrorController가 제공하는 기본 정보들](#BasicErrorController가-제공하는-기본-정보들)을 활용해서 오류 API를 생성함

**HTML 페이지 VS API 오류**
- `BasicErrorController`를 확장하면 JSON 메시지도 변경할 수 있음
- `BasicErrorController`는 HTML 페이지를 제공하는 경우에 매우 편리함
  - 하지만 API 처리의 경우, API 마다 각각의 컨트롤러나 예외에서 서로 다른 응답 결과를 출력해야 할 수도 있음(복잡함)
  - API 오류 처리는 `@ExceptionHandler`가 제공하는 기능을 사용하는 것이 좋음

<br/>

### HandlerExceptionResolver

- 예외가 발생해서 서블릿을 넘어 WAS까지 예외가 전달되면, HTTP 상태코드가 500으로 처리됨
- 동작 방식을 변경하고 싶을 때 `HandlerExceptionResolver` 사용
  - 발생하는 예외에 따라서 400, 404 등등 다른 상태 코드로 처리하고 싶은 경우
  - 오류 메시지, 형식 등을 API 마다 다르게 처리하고 싶은 경우

**상태 코드 변환**

- `IllegalArgumentException` 예외를 처리하지 못해 컨트롤러 밖으로 넘어가는 일이 발생할 경우
  - HTTP 상태 코드를 400으로 처리하고 싶음
- `ApiExceptionController`
  ```java
  if (id.equals("bad")) {
      throw new IllegalArgumentException("잘못된 입력 값");
  }
  ```
  - `http://localhost:8080/api/members/bad` 호출하면 `IllegalArgumentException` 발생
  - 실행하면 상태 코드가 500으로 나옴
  
**HandlerExceptionResolver**

- 스프링 MVC는 컨트롤러(핸들러) 밖으로 예외가 던져진 경우, 예외를 해결하고 동작을 새로 정의할 수 있는 방법을 제공함
- 컨트롤러 밖으로 던져진 예외를 해결하고, 동작 방식을 변경하고 싶으면 `HandlerExceptionResolver`을 사용함
  - 줄여서 `ExceptionResolver`이라고 함
- ExceptionResolver 적용
  1. Dispatcher Servlet - `preHandle` 호출
  2. 핸들러 어댑터의 handle(handler) 호출
  3. 핸들러(컨트롤러)에서 예외 발생
  4. 핸들러 어댑터에서 Dispatcher Servlet으로 예외 전달
  - 적용 전
    5. Dispatcher Servlet - `afterCompletion` 호출
    6. WAS로 예외 전달
  - 적용 후
    5. **ExceptionResolver에서 예외 해결 시도**
    6. Dispatcher Servlet - render(model) 호출
    7. Dispatcher Servlet - `afterCompletion` 호출
    8. WAS로 정상 응답
    
> `ExceptionResolver`로 예외를 해결해도 `postHandle()`은 호출되지 않음

- `HandlerExceptionResolver`
  ```java
  public interface HandlerExceptionResolver { 
        ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex);
  }
  ```
  - `handler`: 핸들러(컨트롤러) 정보
  - `Exception ex`: 핸들러(컨트롤러)에서 발생한 발생한 예외
- `MyHandlerExceptionResolver`
  ```java
  public class MyHandlerExceptionResolver implements HandlerExceptionResolver {
      @Override
      public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
          try {
              if (ex instanceof IllegalArgumentException) {
                  log.info("IllegalArgumentException resolver to 400");
                  response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
                  return new ModelAndView();
              }
          } catch(IOException e){
              log.error("resolver ex", e);
          }
          return null;
      }
  }
  ```
  - `ExceptionResolver`가 `ModelAndView`를 반환하는 이유는 try, catch 하듯이 `Exception`을 처리해서 정상 흐름처럼 변경하기 위함
    - `Exception`을 Resolver(해결)하는 것이 목적
  - `IllegalArgumentException` 발생 시, `response.sendError(400)`을 호출해서 HTTP 상태 코드를 400으로 지정하고 빈 `ModelAndView`를 반환함
- `WebConfig`
  - `WebMvcConfigurer`를 통해 `MyHandlerExceptionResolver` 등 
  ```java
  @Override
  public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
      resolvers.add(new MyHandlerExceptionResolver());
  }
  ```
  - `configureHandlerExceptionResolvers(..)`를 사용하면 스프링이 기본으로 등록하는 `ExceptionResolver`가 제거되므로 주의
  - `extendHandlerExceptionResolvers`를 사용*


**반환 값에 따른 동작 방식**
- 빈 `ModelAndView`
  - `new ModelAndView()`처럼 빈 `ModelAndView`를 반환하면 뷰를 렌더링 하지 않고, 정상 흐름으로 서블릿이 리턴됨
- `ModelAndView` 지정
  - `ModelAndView`에 `View`, `Model` 등 정보를 저장해서 반환하면 뷰를 렌더링 함
- `null`
  - 다음 `ExceptionResolver`를 찾아서 실행함
  - 만약 처리할 수 있는 `ExceptionResolver`가 없으면 예외 처리가 안되고, 기존에 발생한 예외를 서블릿 밖으로 던짐

**ExceptionResolver 활용**

- 예외 상태 코드 변환
  - 예외를 response.sendError() 호출로 변경해서 서블릿에서 상태 코드에 따른 오류를 처리하도록 의임
  - 이후 WAS는 서블릿 오류 페이지를 찾아서 내부 호출함(스프링 부트가 기본으로 설정한 `/error` 호출)
- 뷰 템플릿 처리
  - `ModelAndView`에 값을 채워서 예외에 따른 새로운 오류 화면 뷰를 렌더링해서 고객에게 제공함 
- API 응답 처리
  - `response.getWriter().println("hello")`
    - HTTP 응답 바디에 직접 데이터를 넣어주는 것이 가능
    - JSON으로 응답하면 API 응답 처리를 할 수 있음



