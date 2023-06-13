# 파일 업로드

### 목차
- [HTML Form 전송 방식](#HTML-Form-전송-방식)
- [서블릿과 파일 업로드](#서블릿과-파일-업로드)


<br/>

## HTML Form 전송 방식

#### `application/x-www-form-urlencoded`
- HTML 폼 데이터를 서버로 전송하는 가장 기본적인 방법
- Form 태그에 별도의 `enctype` 옵션이 없으면 웹 브라우저는 요청 HTTP 메시지 헤더에 다음 내용을 추가함
  - `Content-Type: application/x-www-form-urlencoded`
- 폼에 입력한 전송할 항목을 HTTP Body에 문자로 `username=kim&age=20`과 같이 `&`로 구분해서 전송
- 파일 전송
  - 파일을 업로드하려면 문자가 아니라 바이너리 데이터를 전송해야 함
  - 문자를 전송하는 이 방식은 파일을 전송하기 어려움
  - 보통 폼을 전송할 때, 파일만 전송하는 것이 아니라 이름이나 나이 등과 같은 데이터도 함께 전송함
    - 이름과 나이 같은 데이터는 `문자`로 전송하고, 첨부 파일은 `바이너리`로 전송해야 함
    - 문자와 바이너리를 동시에 전송해야 하는 문제가 발

#### `multipart/form-data`

- Form 태그에 별도의 `enctype="multipart/form-data"`를 지정해서 사용
- 다른 종류의 여러 파일과 폼의 내용을 함게 전송할 수 있음(`multipart`)
- 폼의 입력 결과로 생성된 HTTP 메시지를 보면 각각의 전송 항목이 구분되어 있음
  - `Content- Disposition` 항목에 헤더가 추가되어 있고, 부가 정보가 존재
    - 폼의 일반 데이터는 각 항목별로 문자로 전송
    - 파일의 경우 파일 이름과 `Content-Type`이 추가되고 바이너리 데이터로 전송
  - 각각의 항목을 구분해서, 한 번에 전송함
- **Part**
  - `multipart/form-data`는 `application/x-www-form-urlencoded`와 비교해서 매우 복잡하고 각각의 부분(`Part`)로 나누어져 있음

<br/>

## 서블릿과 파일 업로드

- `ServletUploadControllerV1`
  ````java
  @GetMapping("/upload")
  public String newFile() {
      return "upload-form";
  }
  
  @PostMapping("/upload")
  public String saveFileV1(HttpServletRequest request) throws ServletException, IOException {
      log.info("request={}", request);
  
      String itemName = request.getParameter("itemName");
      log.info("itemName={}", itemName);
  
      Collection<Part> parts = request.getParts();
      log.info("parts={}", parts);
  
      return "upload-form";
  }
  ````
  - `request.getParts()`: `multipart/form-data` 전송 방식에서 각각 나누어진 부분을 받아서 확인할 수 있음
- `upload-form.html`
  ```html
  <form th:action method="post" enctype="multipart/form-data">
    <ul>
      <li>상품명 <input type="text" name="itemName"></li>
      <li>파일<input type="file" name="file" ></li>
    </ul>
    <input type="submit"/>
  </form>
  ```
  - `enctype="multipart/form-data"` 지정
- `application.properties`
  - `logging.level.org.apache.coyote.http11=debug`: HTTP 요청 메시지 확인 가능
- 요청 결과 로그
  ````html
  Content-Type: multipart/form-data; boundary=----xxxx
  
  ------xxxx
  Content-Disposition: form-data; name="itemName"
  
  Spring
  ------xxxx
  Content-Disposition: form-data; name="file"; filename="test.data"
  Content-Type: application/octet-stream
  
  sdklajkljdf...
  ````
     
     
**멀티파트 사용 옵션**
- 업로드 사이즈 제한
  - 큰 파일을 무제한 업로드하게 둬서는 안되기 때문에 업로드 사이즈를 제한할 수 있음
  - 사이즈를 넘으면 예외(`SizeLimitExceededException`)가 발생함
  - `spring.servlet.multipart.max-file-size=1MB` `spring.servlet.multipart.max-request-size=10MB`
    - `max-file-size`: 파일 하나의 최대 사이즈, 기본 1MB
    - `max-request-size`: 멀티파트 요청 하나에 여러 파일을 업로드 할 수 있는데, 그 전체의 합(기본 10MB)
- `spring.servlet.multipart.enabled`
  - `false`: 옵션을 끄면 서블릿 컨테이너가 멀티파트 관련 처리를 하지 않음
    - `request.getParameter("itemName")`와 `request.getParts()`의 값이 비어있음
  - `true`: 기본 값으로, 옵션을 키면 서블릿 컨테이너에게 데이터를 처리하라고 설정함
    - `DispatcherServlet`에서 멀티파트 리졸버(`MultipartResolver`)를 실행함
    - `MultipartResolver`는 멀티파트 요청인 경우, 서블릿 컨테이너가 전달하는 일반적인 `HttpServletRequest`를 `MultipartHttpServletRequest`로 변환해서 반환함
      - `MultipartHttpServletRequest`는 `HttpServletRequest`의 자식 인터페이스로, 멀티파트와 관련된 추가 기능을 제공함
- 스프링이 기본으로 제공하는 멀티 파트 리졸버 
  - `MultipartHttpServletRequest` 인터페이스를 구현한 `StandardMultipartHttpServletRequest`를 반환함
    - 컨트롤러에서 `HttpServletRequest` 대신에 `MultipartHttpServletRequest`를 주입받을 수 있음
    - 이것을 이용하여 멀티파트와 관련된 여러가지 처리를 편리하게 할 수 있음
    - 하지만 `MultipartFile`가 더 편리하기 때문에 `MultipartHttpServletRequest`는 잘 사용하지 않음




<br/>



