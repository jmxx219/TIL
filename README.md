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

---

### 6. 템플릿 엔진

<br>

- 서블릿을 이용하여 동적으로 HTML을 만들수 있음
- 자바 코드로 HTML을 만드는 것은 복잡하고 비효율적
- 템플릿 엔진은 HTMl 문서에 동적으로 변경해야하는 부분만(필요한 부분만) 코드를 적용해서 동적으로 변경할 수 있게 해줌
- JSP, Thymeleaf, Freemarker, Velocity 등
- JSP는 성능과 기능면에서 다른 템플릿 엔진에 밀리고 있음. Thymeleaf는 스프링과 잘 통합됨

<br>

---












