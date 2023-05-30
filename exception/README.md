# 예외처리

### 목차
- [서블릿 예외 처리](#서블릿-예외-처리)
  - [Exception과 sendError()](#서블릿-예외-처리-방식)


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