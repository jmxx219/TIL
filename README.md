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
















