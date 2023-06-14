# 파일 업로드

### 목차
- [HTML Form 전송 방식](#HTML-Form-전송-방식)
- [서블릿과 파일 업로드](#서블릿과-파일-업로드)
  - [파일 전송](#파일-전송)
  - [파일 업로드](#파일-업로드)
- [스프링과 파일 업로드](#스프링과-파일-업로드)
- [상품 관리 개발](#상품-관리-개발)


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

### 파일 전송

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

### 파일 업로드

`application.properties`
- `file.dir=파일 업로드 경로 설정`
  - `file.dir=/Users/sonjimin/study/file/`
- 파일 업로드를 위한 실제 파일이 저장되는 경로 설정
- 설정할 때 마지막에 `/`(슬래시) 포함

`ServletUploadControllerV2`
- `application.properties`에서 설정한 `file.dir` 값 주입
  ```java
    @Value("${file.dir}")
    private String fileDir;
  ```
- `Part`
  - 서블릿이 제공하는 `Part`는 멀티파트 형식을 편리하게 읽을 수 있는 다양한 메서드를 제공함
    ```java
    for (Part part : parts) {
        log.info("==== PART ====");
        log.info("name={}", part.getName());
        Collection<String> headerNames = part.getHeaderNames();
        for (String headerName : headerNames) {
            log.info("header {}: {}", headerName, part.getHeader(headerName));
        }
        // 편의 메서드
        // Content-Disposition; filename
        log.info("submittedFileName={}", part.getSubmittedFileName());
        log.info("size={}", part.getSize()); // part body size
  
        // 데이터 읽기
        InputStream inputStream = part.getInputStream();
        String body = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        log.info("body={}", body);
  
        // 파일에 저장하기
        if(StringUtils.hasText(part.getSubmittedFileName())) {
            String fullPath = fileDir + part.getSubmittedFileName();
            log.info("파일 저장 fullPath={}", fullPath);
            part.write(fullPath);
        }
    }
    ```
    - `part.getSubmittedFileName()`: 클라이언트가 전달한 파일명 
    - `part.getInputStream()`: `Part`의 전송 데이터를 읽음
    - `part.write(...)`: `Part`를 통해 전송된 데이터를 저장
      - 설정한 파일 경로에 실제 파일이 저장됨
  - `Part`는 편리하지만 `HttpServletRequest`를 사용해야 하고, 추가로 파일 부분만 구분하려면 여러가지 코드를 넣어야 함


<br/>

## 스프링과 파일 업로드

`MultipartFile`
- 스프링은 `MultipartFile`이라는 인터페이스로 멀티파트 파일을 매우 편리하게 지원
  ```java
  @PostMapping("/upload")
  public String saveFile(@RequestParam String itemName,
                         @RequestParam MultipartFile file, HttpServletRequest request) throws IOException {
      log.info("reqeust={}", request);
      log.info("itemName={}", itemName);
      log.info("MultipartFile={}", file);
  
      if(!file.isEmpty()) {
          String fullPath = fileDir + file.getOriginalFilename();
          log.info("파일 저장 fullPath={}", fullPath);
          file.transferTo(new File(fullPath));
      }
  
      return "upload-form";
  }
  ```
  - `@RequestParam MultipartFile file`
    - 업로드하는 HTML Form의 name에 맞추어 `@RequestParam`을 적용
    - `@ModelAttribute`에서도 `MultipartFile`을 동일하게 사용 가능
  - `file.getOriginalFilename()`: 업로드 파일명
  - `file.transferTo(...)`: 파일 저장


<br/>

## 상품 관리 개발

### 요구사항
- 상품 관리
  - 상품 이름
  - 첨부파일 하나
  - 이미지 파일 여러 개
- 첨부 파일을 업로드, 다운로드 기능
- 업로드한 이미지를 웹 브라우저에서 확인

### 기능

- `Item`
  - 상품 도메인
- `ItemRepository`
  - 상품 리포지토리
- `UploadFile`
  - 업로드 파일 정보 보관
  - 고객이 업로드한 파일명으로 서버 내부에 파일을 저장할 경우, 다른 고객이 같은 파일 이름으로 업로드하면 충돌 발생
  - 저장할 파일명과 겹치지 않도록 내부에서 관리하는 별도의 파일명 필요 
    - `uploadFileName`: 고객이 업로드한 파일명
    - `storeFileName`: 서버 내부에서 관리하는 파일명
- `FileStore`
  - 멀티파트 파일을 서버에 저장하는 역할 담당
  - `createStoreFileName()`: 서버 내부에서 관리하는 파일명은 유일한 이름을 생성하는 UUID를 사용함
  - `extractExt()`: 확장자를 별도 추출해서 서버 내부에서 관리하는 파일명에 붙여줌
- `ItemController`
  - `@GetMapping("/items/new")`: 상품 등록 폼
  - `@PostMapping("/items/new")`: 폼의 데이터 저장 및 상품 조회 폼으로 리다이렉트
  - `@GetMapping("/items/{id}")`: 상품 조회 폼
  - `@GetMapping("/images/{filename}")`
    - `<img>` 태그로 이미지를 조회할 때 사용
    - `UrlResource`로 이미지 파일을 읽어서 `@ResponseBody`로 이미지 바이너리 반환