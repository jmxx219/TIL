# 파일 업로드

### 목차
- [HTML Form 전송 방식](#HTML-Form-전송-방식)


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

