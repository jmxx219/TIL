# 예외처리

### 목차
- [서블릿 예외 처리](#서블릿-예외-처리)
  - [Exception과 sendError()](#서블릿-예외-처리-방식)
  - [오류 화면 제공](#오류-화면-제공)
  - [오류 페이지 작동 원리](#오류-페이지-작동-원리)


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
    - `WAS(여기까지 전파)` ⬅ `필터` ⬅ `서블릿` ⬅ `인터셉터` ⬅ `컨트롤러(예외발생)`
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
  - `WAS(sendError 호출 기록 확인)` ⬅ `필터` ⬅ `서블릿` ⬅ `인터셉터` ⬅ `컨트롤러(response.sendError())`
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
   - `WAS(여기까지 전파)` ⬅ `필터` ⬅ `서블릿` ⬅ `인터셉터` ⬅ `컨트롤러(예외 발생)`
   - `sendError` 흐름
     - `WAS(sendError 호출 기록 확인)` ⬅ `필터` ⬅ `서블릿` ⬅ `인터셉터` ⬅ `컨트롤러(response.sendError())`
       - `WAS`는 해당 예외를 처리하는 오류 페이지 정보를 확인함
       - `new ErrorPage(RuntimeException.class, "/error-page/500")`
2. 오류 페이지 요청 흐름
   - `WAS('/error-page/500' 다시 요청)` ➡ `필터` ➡ `서블릿` ➡ `인터셉터` ➡ `컨트롤러('/error-page/500')` ➡ `View`

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