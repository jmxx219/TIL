# 로그인 처리

> [상품 관리 시스템](https://github.com/jmxx219/Spring-Study/blob/main/item-service/README.md) 프로젝트에 로그인 기능을 구현해보자  
> [쿠키와 세션 설명 참고](https://github.com/jmxx219/CS-Study/blob/main/Network/%EC%BF%A0%ED%82%A4%EC%99%80%20%EC%84%B8%EC%85%98.md)

### 목차
- [쿠키](#쿠키)
- [세션](#세션)
  - [동작 방식](#동작-방식)
  - [세션 직접 생성](#세션-직접-생성)
  - [서블릿 HTTP 세션](#서블릿-HTTP-세션)
  - [세션 정보와 타임아웃 설정](#세션-정보와-타임아웃-설정)
- [서블릿 필터](#서블릿-필터)
  - [소개](#소개)
  - [요청 로그](#요청-로그)
  - [인증 필터](#인증-필터)


<br/>

### 패키지 구조 설계

**package 구조**
- hello.login 
  - domain 
    - item 
    - member 
    - login
  - web 
    - item
    - member
    - login

**도메인**

- 화면, UI, 기술 인프라 등등의 영역을 제외한 시스템이 구현해야 하는 핵심 비즈니스 업무 영역
- 향후 web을 다른 기술로 바꾸어도 도메인은 그대로 유지할 수 있어야 함
  - web은 domain을 알고있지만(의존), domain은 web을 모르도록(의존 X) 설계
  
  
---

<br/>

## 쿠키

- 로그인 상태 유지
  - 서버에서 로그인에 성공하면 HTTP 응답에 쿠키를 담아서 브라우저에 전달
  - 브라우저는 해당 쿠키를 다음 요청 때마다 지속해서 보내줌
- 종류
  - 영속 쿠키: 만료 날짜를 입력하면 해당 날짜까지 유지
  - 세션 쿠키: 만료 날짜를 생략하면 브라우저 종료시 까지만 유지
- 사용
  - 로그인 성공
    - 쿠키 생성
    ```java
    Cookie idCookie = new Cookie("memberId", String.valueOf(loginMember.getId()));
    response.addCookie(idCookie);
    ```
    - 세션 쿠키가 지속해서 유지
  - 웹 브라우저에서 서버에 요청 시 쿠키를 계속 보내줌
    - `@CookieValue`를 이용하여 편리하게 쿠키 조회 가능
  - 로그아웃 시, 서버에서 해당 쿠키의 종료 날짜를 0으로 지정하여 쿠키 종

**보안 문제**

- 쿠키 값은 임의로 변경할 수 있음
  - 클라이언트가 쿠키를 강제로 변경하면 다른 사용자가 될 수 있음
  - 실제 웹브라우저 개발자 모드 ➙ Application ➙ Cookie 변경으로 확인
- 쿠키에 보관된 정보는 훔쳐갈 수 있음
  - 쿠키에 개인정보나, 신용카드 정보 등이 있을 경우
    - 해당 정보가 웹 브라우저에도 보관되고, 네트워크 요청마다 계속 클라이언트에서 버로 전달됨
    - 쿠키의 정보가 나의 로컬 PC에서 털릴 수 도 있고, 네트워크 전송 구간에서도 털릴 수 있음
- 해커가 쿠키를 한 번 훔쳐가면 평생 사용할 수 있음
  - 해커가 쿠키를 훔쳐가 해당 쿠키로 악의적인 요청을 계속 시도할 수 있음

**대안**
- 쿠키에 중요한 값을 노출하지 않고, 사용자별로 예측 불가능한 임의의 토큰(랜덤 값)을 노출함
  - 서버에서 토큰과 사용자 id를 매핑해서 인식하고, 서버에서 토큰을 관리함
- 토큰은 해커가 임의의 값을 넣어도 찾을 수 없도록 예측 불가능해야 함
- 해커가 토큰을 털어가도 시간이 지나면 사용할 수 없도록 서버에서 토큰의 만료시간을 짧게 유지해야 함
  - 해킹이 의심되는 경우, 서버에서 해당 토큰을 강제로 제거

<br/>


## 세션

**쿠키의 보안문제**
- 쿠키에 중요한 정보를 보관하는 방법은 보안 이슈가 발생함
  - 쿠키 값 변조 가능 ➙ 예상 불가능하고 복잡한 **세션 ID**를 사용
  - 쿠키에 보관하는 정보는 클라이언트를 해킹할 시, 털릴 수 있음 ➙ 세션 ID가 털려도 해당 값에는 중요한 정보가 없음
  - 쿠키 탈취 후 사용 ➙ 해커가 토큰을 털어가도 시간이 지나면 사용할 수 없도록 서버에서 **세션의 만료시간을 짧게 유지**
  - 해킹이 의심되는 경우 서버에서 해당 **세션을 강제로 제거**할 수 있음
- 세션을 이용하여 중요한 정보를 보관하고 연결을 유지할 수 있음
  - 중요한 정보는 서버에 저장하고, 클라이언트와 서버는 추정 불가능한 임의의 식별자 값으로 연결
  

### 동작 방식

1. 로그인
   1. 사용자가 로그인 ID와 PW 정보를 전달
   2. 서버에서 해당 사용자가 맞는지 확인
2. 세션 생성
   1. 세션 ID 생성
      - 세션 ID는 추정이 불가능해야 함
      - **UUID**는 추청이 불가능함 (`Cookie: mySessionId=zz0101xx-bab9-4b92-9b32-dadb280f4b61`)
   2. 생성된 세션 ID와 세션에 보관할 값을 서버의 세션 저장소에 보관
3. 세션 ID를 응답 쿠키로 클라이언트에게 전달
   1. 서버는 클라이언트에 `mySessionId`라는 이름으로 세션 ID만 쿠키에 담아서 전달
      - 회원과 관련된 정보는 전혀 클라이언트에게 전달하지 않음
      - 오직 추정 불가능한 세션 ID만 쿠키를 통해 클라이언트에게 전달!
   2. 클라이언트는 쿠키 저장소에 `mySessionId` 쿠키를 보관함
4. 클라이언트의 세션 ID 쿠키를 서버에게 전달
   1. 요청 시, 항상 `mySessionId` 쿠키를 전달함
   2. 서버에서 클라이언트가 전달한 `mySessionId` 쿠키 정보로 세션 저장소를 조회
   3. 로그인시 보관한 세션 정보를 사용

<br/>

### 세션 직접 생성

- 세션 생성
  - sessionId 생성 (임의의 추정 불가능한 랜덤 값)
  - 세션 저장소에 sessionId와 보관할 값 저장
  - sessionId로 응답 쿠키를 생성해서 클라이언트에 전달 세션 조회
- 세션 조회
  - 클라이언트가 요청한 sessionId 쿠키의 값으로, 세션 저장소에 보관한 값 조회 세션 만료
- 세션 만료
  - 클라이언트가 요청한 sessionId 쿠키의 값으로, 세션 저장소에 보관한 sessionId와 값 제거

**테스트**
- 테스트 시에 `HttpServletRequest`와 `HttpservletResponse` 객체를 사용할 수 없음
- 비슷한 역할을 해주는 `MockHttpServletRequest`와 `MockHttpServletResponse` 사용

<br/>

### 서블릿 HTTP 세션

**HttpSession**
- 서블릿이 제공하는 세션 기능으로, 직접 만든 `SessionManager`와 같은 방식으로 동작
- 서블릿을 통해 `HttpSession`을 생성 시, 다음과 같은 쿠키가 생성됨
  - `Cookie: [쿠키 이름]=[추청 불가능한 랜덤 값]`  
    ex) Cookie: JSESSIONID=5B78E23B513F50164D6FDD8C97B0AD05

**사용**
- 세션 생성과 조회
  - `HttpSession session = request.getSession(true)`
    - `public HttpSession getSession(boolean create)`
  - 세션의 `create` 옵션
    - `request.getSession(true)`
      - 세션이 있으면 기존 세션을 반환하고 없으면 새로운 세션을 생성해서 반환함
      - 기본값
    - `request.getSession(false)`
      - 세션이 있으면 기존 세션을 반환하고 없으면 `null` 반환
- 세션에 로그인 회원 정보 보관
  - `session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember)`
    - 세션에 데이터를 보관하는 방법은 request.setAttribute(..)와 비슷함
    - 하나의 세션에 여러 값 저장 가능
  - `session.invalidate()`: 세션 제거
- 로그인 회원 정보 세션에서 조회
  - `session.getAttribute(SessionConst.LOGIN_MEMBER)`
    - 로그인 시점에 세션에 보관한 회원 객체를 찾음

`@SessionAttribute`
- 세션을 더 편리하게 사용할 수 있도록 스프링이 제공
- 이미 로그인된 사용자를 찾을 때
  - `@SessionAttribute(name = "loginMember", required = false) Member loginMember`
  - 세션을 찾고, 세션에 들어있는 데이터를 찾는 과정을 한 번에 처리해줌

**TrackingModes**
- `jsessionid`
  - 웹 브라우저가 쿠키를 지원하지 않을 때, 쿠키 대신 URL을 통해서 세션을 유지하는 방법
    - 로그인을 처음 시도하면 URL에 `jsessionid`를 포함하고 있음
  - 이 방법을 사용하려면 URL에 해당 값을 계속 포함해서 전달해야 함
    - 타임리프 같은 템플릿 엔진을 통해 링크를 걸어두면 `jsessionid`를 URL에 자동으로 포함해줌
  - 서버 입장에서는 웹 브라우저가 쿠키를 지원하는지 않하는지 최초에는 판단이 불가능함
    - 이 때문에 쿠키 값도 전달하고 URL에 `jsessionid`도 함께 전달함
- URL 전달 방식을 끄고 항상 쿠키를 통해서만 세션을 유지하고 싶을 때 사용하는 옵션
  - `application.properties` ➙ `server.servlet.session.tracking-modes=cookie`
  - URL에  `jsessionid`가 노출되지 않음

<br/>

### 세션 정보와 타임아웃 설정

**세션 정보 확인**
- `sessionId`
  - 세션Id, JSESSIONID 의 값
  - 예) 34B14F008AA3527C9F8ED620EFD7A4E1 
- `maxInactiveInterval`
  - 세션의 유효 시간
  - 예) 1800초 (30분)
- `creationTime`
  - 세션 생성일시
- `lastAccessedTime`
  - 세션과 연결된 사용자가 최근에 서버에 접근한 시간 
  - 클라이언트에서 서버로 `sessionId`(`JSESSIONID`)를 요청한 경우에 갱신
- `isNew`
  - 새로 생성된 세션인지, 아니면 이미 과거에 만들어졌고, 클라이언트에서 서버로 `sessionId`(`JSESSIONID`)를 요청해서 조회된 세션인지 여부

**세션 타임아웃 설정**

`session.invalidate()`
- 세션은 사용자가 로그아웃을 호출해서 해당 함수가 호출되는 경우에 삭제됨
- 하지만 대부분 로그아웃을 하지 않고, 웹 브라우저를 종료함
  - HTTP가 비연결성이기 때문에 서버 입장에서는 사용자가 웹 브라우저를 종료하였는지 아닌지 알 수 없음
  - 서버에서 세션 데이터를 언제 삭제해야 하는지 판단하기 어려움
- 이 경우 남아있는 세션을 무한정 보관하면 문제가 발생함
  - 세션과 관련된 쿠키를 탈취했을 경우, 오랜시간이 지나도 악의적인 요청을 할 수 있음
  - 세션은 기본적으로 메모리가 생성됨
    - 메모리의 크기가 무한하지 않기 때문에 꼭 필요한 경우에만 세션을 생성해야 사용해야 함

세션의 종료 시점
- 단순한 방법으로, **세션 생성 시점**으로부터 30분 정도로 설정
  - 30분마다 로그인을 해서 세션을 생성해야하기 때문에 번거로움
- 사용자가 최근에 서버로 요청한 시간을 기준으로 30분 정도 유지
  - 사용자가 서비스를 사용하고 있으면, 세션의 생존 시간이 30분으로 계속 늘어남
  - 30분마다 로그인해야 하는 번거로움이 사라짐
  - `HttpSession`는 해당 방식을 사용함
    - 서블릿의 `HttpSession`이 제공하는 타임아웃 기능으로 세션을 안전하고 편리하게 사용할 수 있음

세션 타임아웃 설정
- 스프링 부트로 글로벌 설정
  - `application.properties` ➙ `server.servlet.session.timeout=60 `
  - 글로벌 설정은 분단위로 설정해야 함
- 특정 세션 단위로 시간 설정
  - `session.setMaxInactiveInterval(1800)`

세션 타임아웃 발생
- 세션의 타임아웃 시간은 해당 세션과 관련된 `JSESSIONID`를 전달하는 HTTP 요청이 있으면 현재 시간으로 다시 초기화됨
  - 세션 타임아웃으로 설정한 시간동안 세션을 추가로 사용 가능
  - `session.getLastAccessedTime()`: 최근 세션 접근 시간
- `LastAccessedTime` 이후로 timeout 시간이 지나면, WAS가 내부에서 해당 세션을 제거함

실무에서 주의할 점
  - 세션에는 최소한의 데이터만 보관해야 함
    - `보관한 데이터 용량 * 사용자 수` 만큼 세션의 메모리 사용량이 급격하게 늘어나 장애로 이어질 수 있음
  - 적당한 시간 선택 필요(기본이 30분)
    - 세션의 시간을 너무 길게 가져가면 메모리 사용이 계속 누적될 수 있음

<br/>

## 서블릿 필터

**공통 관심 사항**
- 로그인을 한 사용자만 상품 관리 페이지에 들어가야 함
  - 로그인을 하지 않은 사용자에게는 상품 관리 페이지 버튼이 보이지 않음
  - But URL을 직접 호출하면 상품 관리 화면에 들어갈 수 있는 문제점 존재
    - 컨트롤러에 로그인 여부를 체크하는 로직을 모두 작성해도 되지만, 모든 컨트롤러 로직에 공통으로 로그인 여부를 체크해야 함
    - 향후 로그인과 관련된 로직이 수정되면, 작성한 모든 로직을 수정해야되는 큰 문제점 발생
- 공통 관심사(cross-cutting concern)
  - 애플리케이션의 여러 로직에서 공통으로 관심이 있는 것
    - ex) 등록, 수정 삭제, 조회 등등 로직에서 **인증**이 공통 관심사
  - 스프링의 `AOP`로도 해결 가능
  - 웹과 관련된 공통 관심사는 `서블릿 필터` 또는 `스프링 인터셉터`를 사용하는 것이 좋음
    - 웹과 관련된 공통 관심사를 처리할 때는 HTTP의 헤더나 URL 정보들이 필요함
      - `서블릿 필터`나 `스프링 인터셉터`는 `HttpServletRequest`를 제공함
      
### 소개

필터: 서블릿이 지원하는 수문장
- 필터 흐름
  - `HTTP 요청` ➙ `WAS` ➙ `필터` ➙ `서블릿(디스패처 서블릿)` ➙ `컨트롤러`
    - 필터를 적용하면 필터가 호출된 후에 서블릿이 호출됨
    - 모든 고객의 요청 로그를 남기는 요구사항이 있다면 필터를 사용
    - 필터는 특정 URL 패턴에 적용할 수 있음(`/*`: 모든 요청에 필터 적용)
- 필터 제한
  - 로그인 사용자: `HTTP 요청` ➙ `WAS` ➙ `필터` ➙ `서블릿` ➙ `컨트롤러`
  - 비 로그인 사용자: `HTTP 요청` ➙ `WAS` ➙ `필터(적절하지 않은 요청이라 판단, 서블릿 호출 X)`
- 필터 체인
  - `HTTP 요청` ➙ `WAS` ➙ `필터 1` ➙ `필터 2` ➙ `필터 3` ➙ `서블릿` ➙ `컨트롤러`
    - 필터는 체인으로 구성, 중간에 필터를 자유롭게 추가할 수 있음
- 필터 인터페이스
  ```java
   public interface Filter {
        public default void init(FilterConfig filterConfig) throws ServletException {}
        public void doFilter(ServletRequest request, ServletResponse response,
                FilterChain chain) throws IOException, ServletException;
        public default void destroy() {}
  }
  ```
  - 필터 인터페이스를 구현하고 등록하면, 서블릿 컨테이너가 필터를 싱글톤 객체로 생성하고 관리
    - `init()`: 필터 초기화 메소드로, 서블릿 컨테이너가 생성될 때 호출
    - `doFilter()`: 고객의 요청이 올 때 마다 해당 메서드가 호출됨. 필터의 로직을 구현
    - `destroy()`: 필터 종료 메소드로, 서블릿 컨테이너가 종료될 때 호출

<br/>

### 요청 로그

**로그 필터(`LogFilter`)**
- 모든 요청을 로그로 남기는 필터
- `public class LogFilter implements Filter {}`
  - 필터 인터페이스를 구현해야 필터 사용이 가능함
- `doFilter(ServletRequest request, ServletResponse response, FilterChain chain)`
  - HTTP 요청이 오면 `doFilter`가 호출됨
  - `ServletRequest`는 HTTP 요청이 아닌 경우까지 고려해서 만든 인터페이스
    - HTTP를 사용할 경우 `HttpServletRequest`로 다운 케스팅해서 사용
      - `HttpServletRequest httpRequest = (HttpServletRequest) request`
  - `chain.doFilter(request, response)`
    - 다음 필터가 있으면 필터를 호출하고, 없으면 서블릿을 호출함
    - 해당 로직을 호출하지 않으면 다음 단계로 진행 x

**필터 설정(`WebConfig`)**

- `FilterRegistrationBean`
  - 필터를 등록하는 방법은 여러가지 존재, 스프링 부트를 사용한다면 `FilterRegistrationBean` 사용
    - `setFilter(new LogFilter())`: 등록할 필터를 지정
    - `setOrder(1)`: 필터는 체인으로 동작하기 때문에 순서가 필요함(낮을 수록 먼저 동작) 
    - `addUrlPatterns("/*")`: 필터를 적용할 URL 패턴을 지정(여러 패턴 지정 가능)
      - 필터의 URL 패턴의 룰은 서블릿과 동일
- ` @ServletComponentScan`, `@WebFilter(filterName = "logFilter", urlPatterns = "/*")`
  - 필터 등록이 가능하지만 필터 순서 조절 불가능
  
> 실무에서 HTTP 요청 시, 같은 요청의 로그에 모두 같은 식별자를 자동으로 남기는 방법으로 **logback mdc**가 있음

<br/>

### 인증 필터

**인증 체크 필터(`LoginCheckFilter`)**
- 로그인 되지 않은 사용자는 상품 관리 뿐만 아니라 후에 개발될 페이지에도 접근하지 못하도록 함
- `whitelist = {"/", "/members/add", "/login", "/logout","/css/*"};`
  - 인증 필터를 적용해도 홈, 회원가입, 로그인 화면, css 같은 리소스에는 접근 가능해야 함
  - 화이트 리스트 경로는 인증과 무관하게 항상 허용
- `isLoginCheckPath(requestURI)`
  - 화이트 리스트를 제외한 나머지 모든 경로에는 인증 체크 로직을 적용함
- `httpResponse.sendRedirect("/login?redirectURL=" + requestURI);`
  - 미인증 사용자는 로그인 화면으로 리다이렉트 함
    - 로그인 후에 다시 홈으로 이동하면, 원하는 경로로 다시 찾아가야 하는 불편함이 존재
    - 현재 요청 경로인 `requestURI`를 `/login`에 쿼리 파라미터로 함께 전달하여, 컨트롤러에서 로그인 성공 시 해당 경로로 이동하도록 기능을 추가함
- `return;`
  - 미인증 사용자일 경우, 필터는 물론 서블릿, 컨트롤러가 더는 호출되지 않도록 함
  - `redirect`가 응답으로 적용되고 요청 종료

**필터 설정(`WebConfig`)**
- `setFilter(new LoginCheckFilter())`: 로그인 필터 등록
- `setOrder(2)`: 순서를 2번으로 하여 로그 필터 다음에 로그인 필터가 적용됨 
- `addUrlPatterns("/*")`: 모든 요청에 로그인 필터를 적용

**RedirectURL 처리**
- 로그인에 성공하면 처음 요청한 URL로 이동
- 로그인 체크 필터에서 미인증 사용자의 경우, 요청 경로를 포함해서 `/login`에 `redirectURL` 요청 파라미터를 추가해서 요청함
  - 해당 값을 이용해서 로그인 성공 시, 해당 경로로 `redirect`함

<br/>