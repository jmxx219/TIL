# 예외처리

### 목차
- [서블릿 예외 처리](#서블릿-예외-처리)
  - [Exception과 sendError()](#서블릿-예외-처리-방식)
  - [오류 화면 제공](#오류-화면-제공)
  - [오류 페이지 작동 원리](#오류-페이지-작동-원리)
  - [필터](#필터)
  - [인터셉터](#인터셉터)


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
  
1. `WAS(/error-ex, dispatchType=REQUEST)` ➜ `필터` ➜ `서블릿` ➜ `인터셉터` ➜ `컨트롤러`
2. `WAS(여기까지 전파)` ⬅ `필터` ⬅ `서블릿` ⬅ `인터셉터` ⬅ `컨트롤러(예외발생)`
3. `WAS 오류 페이지 확인`
4. `WAS(/error-page/500, dispatchType=ERROR)` ➜ `필터(x)` ➜ `서블릿` ➜ `인터셉터(x)` ➜ `컨트롤러(/error-page/500)` ➜ `View`
