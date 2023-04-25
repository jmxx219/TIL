# Thymeleaf

### 타임리프 특징

- **서버 사이드 HTML 렌더링(SSR)**
  - 백엔드 서버에서 HTML을 동적으로 렌더링하는 용도로 사용
- **네츄럴 템플릿**
  - 순수 HTML을 그대로 유지하면서 뷰 템플릿도 사용할 수 있는 타임리프의 특징
  - [부가 설명](https://github.com/jmxx219/Spring-Study/blob/main/item-service/README.md#thymeleaf)
- **스프링 통합 지원**
  - 스프링과 자연스럽게 통합되고, 스프링의 다양한 기능을 편리하게 사용할 수 있도록 지원함

<br/>

## 타임 리프 기본 기능

**사용 선언** : `<html xmlns:th="http://www.thymeleaf.org">`

### 기본 표현식
- 간단한 표현 - 변수: `${...}`, 선택 변수: `*{...}`, 메시지: `#{...}`, 링크 URL: `@{...}`, 조각: `~{...}`
- 리터럴 - 텍스트, 숫자, 불린(true, false), 널(null), 리터럴 토큰
- 문자 연산 - 문자 합치기(`+`), 리터럴 대체(`|The name is ${name}|`)
- 산술 연산 - `+`, `-`, `*`, `/`, `%`
- 불린 연산 - `and`, `or`, `!`, `not`
- 비교: `>`,`<`,`>=`,`<=` (gt,lt,ge,le)
- 동등:  `==`, `!=` (eq, ne)
- 조건 연산 - If-then: `(if) ? (then)`, If-then-else: `(if) ? (then) : (else)`, Default: `(value) ?: (defaultvalue)`
- 특별한 토큰 -  No-Operation: `_`


### 텍스트

- `th:text`, `th:utext`
  - HTML 태그의 속성에 기능을 정의해서 콘텐츠(content)에 데이터 출력
    - `<li>th:text = <span th:text="${data}"></span></li>`
    - `<li>th:utext = <span th:utext="${data}"></span></li>`
- `[[...]]`, `[(...)]`
  - 태그의 속성이 아닌 HTML 콘텐츠 영역 안에서 직접 데이터 출력
    - `<li><span th:inline="none">[[...]] = </span>[[${data}]]</li>`
    - `<li><span th:inline="none">[(...)] = </span>[(${data})]</li>`
- `th:inline="none"`
  - 타임리프는 `[[...]]`를 해석
  - 화면에 그대로 보여주기 위해서 태그 안에서는 타임리프가 해석하지 말라는 옵션

```
th:text = Hello <b>Spring!</b>
th:utext = Hello Spring! // Unescape

[[...]] = Hello <b>Spring!</b>
[(...)] = Hello Spring! // Unescape
```

**Escape**

- HTML 문서는 `<`, `>` 같은 특수 문자를 기반으로 정의됨
    - 뷰 템플릿으로 HTML 화면을 생성할 때 출력하는 데이터에 이러한 특수 문자가 있을 경우 주의해서 사용
- **HTML 엔티티**
    - 웹 브라우저는 `<`를 HTML 태그의 시작으로 인식하는데, 이때 `<`를 태그의 시작이 아닌 문자로 표현하는 방법
    - `<` ➔ `&lt;`
    - `>` ➔ `&gt;`
- HTML에서 사용하는 특수 문자를 **HTML 엔티티**로 변경하는 것을 `Escape`라 함
    - 타임리프가 제공하는 `th:text`와 `[[...]]`는 기본적으로 이스케이프를 제공
        - **Unescape**: `th:utext`와 `[(...])]`


### SpringEL

`${...}` : 변수 표현식
  - 해당 표현식에 **SpringEL**이라는 스프링이 제공하는 표현식을 사용할 수 있음
    - **Object**
      - `user.username`: user의 username 프로퍼티 접근 ➔ `user.getUsername()`
      - `user['username']`: 위와 동일
      - `user.getUsername()`: user의 `user.getUsername()`을 직접 호출
    - **List**
      - `users[0].username`: List에서 첫 번째 회원을 찾고 username 프로퍼티 접근 ➔ `list.get(0).getUsername()`
      - `users[0]['username']`: 위와 동일
      - `users[0].getUsername()` : 메소드 직접 호출
    - **Map**
      - `userMap['userA'].username`: Map에서 userA를 찾고, username 프로퍼티 접근 ➔ `map.get("userA").getUsername()`
      - `userMap['userA']['username']`: 위와 동일
      - `userMap['userA'].getUsername()`: 메소드 직접 호출

**지역 변수 선언**
- `th:with`: 선언한 태그 안에서만 사용 가능
    ```html
    <div th:with="first=${users[0]}">
        <p>처음 사람의 이름은 <span th:text="${first.username}"></span></p>
    </div>
    ```