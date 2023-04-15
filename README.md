# 서블릿(Servlet)

<br>

---


### 1. 서블릿 환경 구성

<br>

- 서블릿은 웹 애플리케이션 서버(톰캣)를 직접 설치하고, 그 위에 서블릿 코드를 클래스 파일로 빌드해서 올려서 톰캣 서버를 실행함 -> 번거로움
- 스프링 부트는 톰캣 서버를 내장하고 있음 -> 톰캣 서버 설치없이 편리하게 서블릿 코드 실행 가능

<br>

#### 스프링 부트 서블릿 환경 설정

`ServletComponentScan` : 스프링부트가 서블릿을 직접 등록해서 사용할 수 있도록 지원
- 스프링이 자동으로 현재 내 패키지를 포함한 하위 패키지에 있는 모든 서블릿을 찾아 자동으로 서블릿을 등록해서 실행하도록 도와줌

<br>

#### 서블릿 등록
`WevServlet` : 서블릿 애노테이션
- name: 서블릿 이름
- urlPatterns: URL 매핑

- HTTP 요청을 통해 매핑된 URL 호출 시, 서블릿 컨테이너는 service 메소드를 실행함

```java
protected void service(HttpServletRequest request, HttpServletResponse response) {}
```

<br>

---

### 2. HttpServletRequest

<br>

- HTTP 요청 메시지를 직접 파싱해서 사용 가능하지만 불편함
- 서블릿은 개발자가 HTTP 요청 메시지를 편리하게 사용할 수 있도록 개발자 대신에 HTTP 요청 메시지를 파싱함
- 그 결과를 HttpServletRequest 객체에 담아서 제공 -> HTTP 요청 메시지를 편리하게 조회 가능

<br>


**HTTP 요청 메시지**
```java
POST /save HTTP/1.1
Host: localhost:8080
Content-Type: application/x-www-form-urlencoded
        
username=kim&age=20
```
- Start Line : HTTP 메소드, URL, 쿼리 스트링, 스키마, 프로토콜
- 헤더: 헤더 조회
- 바디: form 파라미터 형식 조회, message body 데이터 직접 조회

<br>

---


### 3. HTTP 요청 데이터

<br>

**HTTP 요청 메시지 이용하여 클라이언트에서 서버로 데이터 전달하는 방법**

1. GET - 쿼리 파라미터
   - /url?**username=hello&age=20**
   - `?`으로 시작, `&`로 파라미터 구분
   - 메시지 바디 없이(content-type 없음) URL의 쿼리 파라미터에 데이터를 포함해서 전달
   - 검색, 필터, 페이징 등에서 많이 사용


2. POST - HTML Form
   - content-type:application/x-www-form-urlencoded
   - 메시지 바디에 쿼리 파라미터 형식으로 데이터 전달
   - 바디에 포함된 데이터 형식 content-type을 꼭 지정해야함


3. HTTP message body
   - HTTP API에서 주로 사용. 
   - TEXT, JSON, XML. 데이터 형식은 주로 JSON 사용
   - POST, PUT, PATCH
   
   - TEXT(단순 텍스트 메시지)
     - InputStream을 사용하여 직접 읽음
     - inputStream은 byte 코드를 반환 -> byte 코드를 문자(String)으로 보기 위해 문자표(Charset)를 지정
         ```java
         String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
         ```
   - JSON
     - JSON 형식을 파싱할 수 있는 자바 객체로 변환하기 위해 Jackson, Gson같은 JSON 변환 라이버르를 사용.
     - 스프링 부트로 Spring MVC를 선택하면 기본으로 Jackson 라이브러리(ObjectMapper)를 함께 제공
         ```java
         private ObjectMapper objectMapper = new ObjectMapper();
         HelloData helloData = objectMapper.readValue(messageBody, HelloData.class);
         ```
<br>

---

### 4. HttpServletResponse

<br>

역할
- HTTP 응답 메시지 생성
- HTTP 응답코드 지정
- 헤더 및 바디 생성
- 편의 기능 제공 - Content-Type, 쿠키, Redirect
````java
response.setStatus(HttpServletResponse.SC_OK); // 200
        
response.setHeader("Content-Type", "text/plain;charset=utf-8");

response.setContentType("text/plain");
response.setCharacterEncoding("utf-8");

Cookie cookie = new Cookie("myCookie", "good");
cookie.setMaxAge(600); //600초
response.addCookie(cookie);

response.sendRedirect("/basic/hello-form.html");
````

<br>

---

### 5. HTTP 응답 데이터

<br>

1. 단순 텍스트
   ```java
    writer.println("ok");
    ```

2. HTML 응답
   - content-type을 `text/html`로 지정


3. HTTP API - MessageBody JSON 응답
   - content-type을 `application/json`로 지정
   - Jackson 라이브러리가 제공하는 `objectMapper.writeValueAsString()`를 사용하여 객체를 JSON 문자로 변경
   - `application/json`은 스팩상 utf-8 형식을 사용하도록 정의 -> `charset=utf-8`과 같은 추가 파라미터 지원 x



<br>
<br>
<br>


# 서블릿, JSP, MVC 패턴

<br>

---


### 1. 템플릿 엔진

<br>

- 서블릿을 이용하여 동적으로 HTML을 만들수 있음
- 자바 코드로 HTML을 만드는 것은 복잡하고 비효율적
- 템플릿 엔진은 HTMl 문서에 동적으로 변경해야하는 부분만(필요한 부분만) 코드를 적용해서 동적으로 변경할 수 있게 해줌
- JSP, Thymeleaf, Freemarker, Velocity 등
- JSP는 성능과 기능면에서 다른 템플릿 엔진에 밀리고 있음. Thymeleaf는 스프링과 잘 통합됨

<br>

---


### 2. JSP


```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="hello.servlet.domain.member.Member" %>
<%@ page import="hello.servlet.domain.member.MemberRepository" %>
<%
    MemberRepository memberRepository = MemberRepository.getInstance();
    
    String username = request.getParameter("username");
    int age = Integer.parseInt(request.getParameter("age"));
    
    Member member = new Member(username, age);
    memberRepository.save(member);
%>
<html>
<head>
    <title>Title</title>
</head>
<body>
<ul>
    <li>id=<%=member.getId()%></li>
    <li>username=<%=member.getUsername()%></li>
    <li>age=<%=member.getAge()%></li>
</ul>
</body>
</html>
```
- JSP는 자바 코드를 그대로 사용할 수 있음
  - `<%@ page contentType="text/html;charset=UTF-8" language="java" %>`
    - 자바의 import문과 같이 JSP 문서는 다음과 같이 시작함
  - `<% ~~ %>` 자바 코드를 입력
  - `<%= ~~ %>` 자바 코드를 출력
- 서블릿과 다르게 HTML을 중심으로, HTML 코드 부분에 자바 코드를 입력함

<br>

**서블릿과 JSP 한계**

- 서블릿을 이용하여 뷰(view)를 작업할 때는 자바 코드에 HTML을 넣어 지저분하고 복잡함
- JSP를 사용하면서 뷰를 생성하는 HTML 작업이 깔끔해지고, 동적으로 변경이 필요한 부분만 자바 코드에 적용 가능해짐
- JSP 코드의 절반은 비즈니스 로직이고 나머지는 HTML로 보여주기 위한 뷰 영역으로, 많은 코드가 JSP에 노출됨
  - JSP가 많은 역할을 담당
- 해결책! -> **MVC 패턴 등장**
  - 비즈니스 로직은 서블릿처럼 다른 곳에서 처리
  - JSP는 목적에 맞게 HTML로 뷰를 그리는 역할만 담당
  

<br>

---


### 3. MVC 패턴

- 너무 많은 역할
  - 서블릿과 JSP로 비즈니스 로직과 뷰 렌더링 모두 처리 -> 많은 역할을 담당 -> 유지보수가 어려워짐
- 변경의 라이프 사이클
  - 비즈니스 로직과 뷰 UI를 수정하는 라이프 사이클이 다른 것이 문제
  - 각각 다르게 발생할 가능성이 높고 대부분 서로에게 영향 X
  - 변경 라이프 사이클이 다른 부분을 하나의 코드로 관리하는 것은 유지보수에 좋지 않음
- 기능 특화
  - JSP와 같은 뷰 템플릿은 화면을 렌더링 하는데 최적화 -> 해당 부분만 담당하는 것이 효과적

<br>

**Model View Controller**
- 모델(Model)
  - 뷰가 필요한 데이터를 담아서 전달해줌
  - 뷰는 비즈니스 로직이나 데이터 접근을 몰라도 되기 때문에 화면 렌더링 일에만 집중 
- 뷰(View)
  - 모델에 담겨있는 데이터를 사용하여 화면을 그리는 작업 진행
  - HTML을 생성하는 부분
- 컨트롤러(Controller)
  - HTTP 요청을 받아서 파라미터를 검증하고 비즈니스 로직을 실행
  - 뷰에 전달할 결과 데이터를 조회해서 모델에 주입
- 서비스(Service)
  - 컨트롤러에 비즈니스 로직을 맡길 경우, 컨트롤러가 너무 많은 역할을 담당함. 
  - 일반적으로 비즈니스 로직은 서비스(Service)라는 계층을 별도로 만들어서 처리
  - 컨트롤러는 비즈니스 로직이 있는 서비스를 호출하는 역할을 담당

<br>

**MVC 패턴 한계**
- MVC 패턴 적용으로 컨트롤러의 역할과 뷰를 렌더링하는 역할을 명확히 구분
- 뷰는 화면을 그리는 역할만 담당하여 코드가 깔끔하고 직관적임. 단순히 모델에서 필요한 데이터를 꺼내고 화면을 만듦
- 컨트롤러는 여전히 중복이 많고 필요하지 않은 코드들이 있음
- MVC 컨트롤러 단점
  - 포워드 중복 - View로 이동하는 코드가 항상 중복 호출됨
  ```java
  RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
  dispatcher.forward(request, response);
  ```
  - ViewPath 중복 - prefix: /WEB-INF/views, suffix: .jsp
  ```java
  String viewPath = "/WEB-INF/views/new-form.jsp";
  ```
  - 사용하지 않는 코드
  ````java
  HttpServletRequest request, HttpServletResponse response
  ````
  - 공통 처리 어려움
    - 기능이 복잡해질수록 컨트롤러에서 공통으로 처리하는 부분 증가 -> 중복

<br>

**MVC 컨트롤러의 공통 처리가 어렵다는 문제 해결**
- 컨트롤러 호출 전에 먼저 공통 기능을 처리해야 함
- 프론트 컨트롤러(Front Controller)패턴을 도입하여 해결

<br>

---

### 4. MVC 프레임워크 만들기

<br>

**Front Controller(프론트 컨트롤러) 패턴**
- 도입 전에는 각 컨트롤러마다 공통 로직을 만들어야했었음.
- 도입 후에는 공통 로직은 프론트 컨트롤러에서 처리하고, 각자 처리해야하는 로직은 각 컨틀롤러에 처리하도록 함
- 공통의 관심사를 별도로 모으는 역할을 함
- 특징
  - 서블릿 하나로 클라이언트의 요청을 모두 받음
  - 프론트 컨트롤러가 요청에 맞는 컨트롤러를 찾아서 호출함
  - 공통 처리 가능
  - 프론트 컨트롤러를 제외한 나머지 컨트롤러는 서블릿을 사용하지 않아도 됨 -> 프론트 컨트롤러가 나머지 컨트롤러를 직접 호출해주기 때문
  - 스프링 웹 MCV의 핵심으로, DispatcherServlet이 FrontController 패턴으로 구현됨

<br/>

**Front Controller**
- 도입
  1. 클라이언트 - HTTP 요청
     - urlPatterns = "/front-controller/v1/*"
     - 하위 모든 요청은 Front Controller Servlet에서 처리
  2. Front Controller - URL 매핑 정보에서 컨트롤러 조회
     - controllerMap :  <매핑 URL, 호출될 컨트롤러>
  3. Front Controller - 컨트롤러 호출
    ````java
    String requestURI = request.getRequestURI();
    ControllerV1 controller = controllerMap.get(requestURI);
    controller.process(request, response);
     ````
  4. Controller - JSP forward
  5. JSP - 클라이언트에 HTML 응답
- View 분리
    1. 클라이언트 - HTTP 요청
    2. Front Controller - URL 매핑 정보에서 컨트롤러 조회, 컨트롤러 호출 
    3. Controller - MyView 반환
    ```java
    @Override
    public MyView process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        return new MyView("/WEB-INF/views/new-form.jsp");
    }
    ```
    4. Front Controller - render() 호출
    ```java
    MyView view = controller.process(request, response);
    view.render(request, response);
    ```
    5. MyView - JSP forward
    6. JSP - 클라이언트에 HTML 응답
- Model 추가
    1. 클라이언트 - HTTP 요청
    2. Front Controller - URL 매핑 정보에서 컨트롤러 조회, 컨트롤러 호출
       - `createParamMap` : 파라미터 정보를 Map으로 변환하여 컨트롤러에 전달
        ```java
        // paramMap
        Map<String, String> paramMap = createParamMap(request);
        ModelView mv = controller.process(paramMap);
        ```
    3. Controller - **ModelView** 반환
       - 기존
         - 서블릿에 종속적인 `HttpServletRequest` 사용
         - Model을 `request.setAttribute()`를 통해 데이터를 저장하고 뷰에 전달함
       - 개선
         - 서블릿의 종속성 제거 -> Model 생성
         ```java
         public class ModelView {
             private String viewName;
             private Map<String, Object> model = new HashMap<>(); // view에 필요한 데이터 저장
         }
         ```
         - 뷰 이름의 중복 제거 -> 논리 이름만 전달, 물리적인 이름은 프론트 컨트롤러에서 처리
         ```java
         @Override
         public ModelView process(Map<String, String> paramMap) {
             List<Member> members = memberRepository.findAll();
             ModelView mv = new ModelView("members"); // view의 논리 이름
             mv.getModel().put("members", members); // view에 필요한 데이터 저장
             return mv;
         }
         ```
    4. Front Controller - viewResolver 호출
        - View Resolver : 컨트롤러가 반환한 논리 뷰 이름을 물리 뷰 경로로 변경
        ```java
        String viewName = mv.getViewName();// 논리 이름 -> new-form
        MyView view = new MyView("/WEB-INF/views/" + viewName + ".jsp"); // view Resolver
        view.render(mv.getModel(), request, response);
        ```
    5. viewResolver - MyView 반환
    6. Front Controller - render(model) 호출
    7. MyView - JSP forward 
        ```java
        public void render(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            model.forEach((key, value) -> request.setAttribute(key, value)); // JSP는 getAttribute()로 데이터를 조회함
            RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
            dispatcher.forward(request, response);
        }
        ```
    8. JSP - 클라이언트에 HTML 응답
- Controller 개선
  - Model을 프론트 컨트롤러에서 생성하여 각 컨트롤러로 전달
  - 컨트롤러는 뷰의 논리 이름만 반환하고, 모델에 데이터를 저장함















