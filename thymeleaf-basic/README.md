# Thymeleaf

### 목차
- [타임리프 특징](#타임리프-특징) <br/>
- [타임 리프 기본 기능](#타임-리프-기본-기능)
  * [텍스트](#텍스트)
  * [기본 표현식](#기본-표현식)
    * [SpringEL](#SpringEL)
    * [기본 객체](#기본-객체)
    * [유틸리티 객체와 날짜](#유틸리티-객체와-날짜)
    * [URL 링크](#URL-링크)
    * [리터럴(Literals)](#리터럴(Literals))
    * [연산](#연산)
  * [속성 값 설정](#속성-값-설정)
  * [반복](#반복)
  * [조건부 평가](#조건부-평가)
  * [주석](#주석)
  * [블럭](#블럭)
  * [자바스크립트 인라인](#자바스크립트-인라인)
  * [템플릿 조각](#템플릿-조각)
  * [템플릿 레이아웃](#템플릿-레잉아웃)


---

## 타임리프 특징


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

<br/>


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

<br/>

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

<br/>
  
### 기본 객체

`${#request}` , `${#response}` , `${#session}` , `${#servletContext}`, `${#locale}`

- 스프링 부트 3.0부터는 `${#request}` , `${#response}` , `${#session}` , `${#servletContext}`를 지원하지 않음
  - 직접 `Model`에 해당 객체를 추가해서 사용
- 편의 객체 제공
  - `param`: HTTP 요청 파라미터 접근
    - `${param.paramData}`
  - `session`: HTTP 세션 접근
    - `${session.sessionData}`
  - `@`: 스프링 빈 접근
    - `${@helloBean.hello('Spring!')}`


### 유틸리티 객체와 날짜

문자, 숫자, 날짜, URI 등을 편리하게 다루는 다양한 유틸리티 객체를 제공함
- `#message`: 메시지, 국제화 처리
- `#uris` : URI 이스케이프 지원
- `#dates` : java.util.Date 서식 지원 
- `#calendars` : java.util.Calendar 서식 지원 
- `#temporals` : 자바8 날짜 서식 지원
- `#numbers` : 숫자 서식 지원
- `#strings` : 문자 관련 편의 기능
- `#objects` : 객체 관련 기능 제공
- `#bools` : boolean 관련 기능 제공
- `#arrays` : 배열 관련 기능 제공
- `#lists` , #sets , #maps : 컬렉션 관련 기능 제공 
- `#ids` : 아이디 처리 관련 기능 제공

참고
- [타임리프 유틸리티 객체](https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html#expression-utility-objects)
- [예시](https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html#appendix-b-expression-utility-objects)

<br/>

### URL 링크

`@{...}`: URL 생성
- 단순 URL
  - `@{/hello}` ➔ `/hello`
- 쿼리 파라미터
  - `@{/hello(param1=${param1}, param2=${param2})}` ➔ `/hello?param1=data1&param2=data2`
  - `()` 부분은 쿼리 파라미터로 처리
- 경로 변수
  - `@{/hello/{param1}/{param2}(param1=${param1}, param2=${param2})}` ➔ `/hello/data1/data2`
  - URL 경로상에 변수가 있으면 `()` 부분은 경로 변수로 처리
- 경로 변수 + 쿼리 파라미터
  - `@{/hello/{param1}(param1=${param1}, param2=${param2})}` ➔ `/hello/data1?param2=data2`
  - 경로 변수와 일치하지 않으면 쿼리 파라미터로 자동 처리

<br/>

### 리터럴(Literals)

- 소스 코드 상에서 고정된 값
  - 문자: `'hello'`, 숫자: `10`, 불린: `true` `false`, null: `null`
- 타임리프에서 문자 리터럴은 항상 `'`(작은 따옴표)로 감싸야 함
  - `<span th:text="'hello'">`
  - 공백없이 쭉 이어진다면 하나의 의미있는 토근으로 인지 ➔ 작은 따옴표 생략 가능
    - `<span th:text="hello world!"></span>` ➔ 오류
    - `<span th:text="'hello world!'"></span>` ➔ `'`로 감싸면 정상 동작
- `|...|` 
  - 리터럴 대체를 이용하면 편리하게 사용 가능
    - `<span th:text="|hello ${data}|">`

<br/>

### 연산

자바와 크게 다르지 않지만 HTML 엔티를 사용하는 부분만 주의

- 산술 연산
  - `<span th:text="10 + 2"></span>` ➔ `12`
  - `<span th:text="10 % 2 == 0"></span>` ➔ `true`
- 비교 연산
  - HTML 엔티티 사용 주의
  - `>` (gt), `<` (lt), `>=` (ge), `<=` (le), `!` (not), `==` (eq), `!=` (neq, ne)
    - `1 &gt; 10` ➔ `1 > 10` ➔ `false`
    - `1 gt 10` ➔ `1 gt 10` ➔ `false`
    - `1 >= 10` ➔ `1 >= 10` ➔ `false`
    - `1 ge 10` ➔ `1 ge 10` ➔ `false`
    - `1 == 10` ➔ `1 == 10` ➔ `false`
    - `1 != 10` ➔ `1 != 10` ➔ `true`
- 조건식
  - `(10 % 2 == 0)? '짝수':'홀수'` ➔ `짝수`
- Elvis 연산자
  - `data` 변수가 참이면 `data`를, 거짓이라면 `데이터가 없습니다.` 출력
    - `${data}?: '데이터가 없습니다.'` ➔ `Spring!`
    - `${nullData}?: '데이터가 없습니다.'` ➔ `데이터가 없습니다.`
- No-Operation
  - `_` 인 경우 마치 타임리프가 실행되지 않는 것처럼 동작
    - HTML 내용을 그대로 사용
        - `<span th:text="${data}?: _">데이터가 없습니다.</span>` = Spring!
        - `<span th:text="${nullData}?: _">데이터가 없습니다.</span>` = 데이터가 없습니다.

<br/>

### 속성 값 설정

타임리프 태그 속성(Attribute)

- `th:*`
  - HTML 태그에 `th:*` 속성을 지정하여 기존 속성을 대체하고, 기존 속성이 없으면 새로 생성함
    - 타임리프 렌더링 전 : `<input type="text" name="mock" th:name="userA" />`
    - 타임리프 렌더링 후 : `<input type="text" name="userA" />`
- 속성 추가
  - `th:attrappend`: 속성 값의 뒤에 값을 추가
    - 타임리프 렌더링 전 : `<input type="text" class="text" th:attrappend="class=' large'" />`
    - 타임리프 렌더링 후 : `<input type="text" class="text large" />`
  - `th:attrprepend`: 속성 값의 앞에 값을 추가
  - `th:classappend`: class 속성에 추가
- checked 처리
  - HTML에서는 `checked` 속성의 값(`checked="false"`)과 상관없이 `checked` 속성이 있으면 체크 처리됨
  - 타임리프의 `th:checked`은 값이 `false`인 경우 `checked` 속성 자체를 제거함

<br/>

### 반복

`th:each`

- 반복 기능
  - `<tr th:each="user : ${users}">`
    - 오른쪽 컬렉션 ${users}에서 값을 하나씩 꺼내 왼쪽 변수 `user`에 담아서 태그를 반복 실행함
    - `java.util.Iterable`, `java.util.Enumeration`을 구현한 모든 객체를 반복에 사용 가능
- 반복 상태 유지
  - `<tr th:each="user, userStat : ${users}">`
    - 반복의 두번째 파라미터를 설정해서 반복의 상태 확인 가능
      - 두번째 파라미터를 생략해도 사용 가능. 지정한 변수명(`user`) + `Stat` ➔ `userStat`
  - 기능
    - `index` : 0부터 시작하는 값
    - `count` : 1부터 시작하는 값
    - `size` : 전체 사이즈
    - `even`, `odd` : 홀수, 짝수 여부(`boolean`) 
    - `first`, `last`: 처음, 마지막 여부(`boolean`) 
    - `current` : 현재 객체
    
<br/>

### 조건부 평가

- `th:if`, `th:unless`(`if`의 반대)
  - 타임리프는 해당 조건이 맞지 않으면 태그 자체를 렌더링하지 않음
- `th:switch`, `th:case`
  - `<td th:switch="${user.age}">`
    - `<span th:case="10">10살</span>`
    - `<span th:case="*">기타</span>`
      - `*`은 만족하는 조건이 없을 때 사용하는 디폴트

<br/>

### 주석


- 표준 HTML 주석
  - `<!--  ...  -->`
  - 자바스크립트의 표준 HTML 주석은 타임리프가 렌더링하지 않고 그대로 남겨둠
- 타임리프 파서 주석
  - `<!--/*  ...  */-->`
  - `<!--/*-->  ... <!--*/-->` ➔ 여러줄 주석 처리
  - 타임리프 렌더링에서 주석부분을 제거
  - HTML 파일을 웹 브라우저에서 열었을 때는 남겨짐
- 타임리프 프로토타입 주석
  - `<!--/*/ ... /*/-->`
  - HTML 파일을 웹 브라우저에서 그대로 열어보면 HTML 주석이기 때문에 이 부분을 웹 브라우저가 렌더링하지 않음 ➔ 주석 처리되어 남겨짐
  - 타임리프 렌더링을 거치면 이 부분이 정상 렌더링됨
    - 타임리프로 렌더링을 한 경우에만 보이는 기능
```html
<h1>1. 표준 HTML 주석</h1>
<!-- 
<span th:text="${data}">html data</span> 
-->

<h1>2. 타임리프 파서 주석</h1>
<!--/* [[${data}]] */-->
<!--/*-->
<span th:text="${data}">html data</span>
<!--*/-->

<h1>3. 타임리프 프로토타입 주석</h1>
<!--/*/
<span th:text="${data}">html data</span>
/*/-->
```
- 타임리프 렌더링 결과  
  ```
  1. 표준 HTML 주석
  2. 타임리프 파서 주석
  3. 타임리프 프로토타입 주석
  Spring!
  ```
- HTML 파일을 웹 브라우저로 열었을 때 결과
  ```
  1. 표준 HTML 주석
  2. 타임리프 파서 주석
  html data
  3. 타임리프 프로토타입 주석
  ```

<br/>

### 블록

`<th:block>`

- HTML 태그가 아닌 타임리프의 유일한 자체 태그
  - 렌더링시에는 제거됨
  - 웹 브라우저에서는 태그의 반복 결과만 남음
- 타임리프의 특성상 HTML 태그안에 속성으로 기능을 정의해서 사용
  - 여러 개의 태그를 반복문으로 돌려야 하는 상황에서 사용

```html
<th:block th:each="user : ${users}">
  <div>
    사용자 이름1 <span th:text="${user.username}"></span>
    사용자 나이1 <span th:text="${user.age}"></span> </div>
  <div>
    요약 <span th:text="${user.username} + ' / ' + ${user.age}"></span>
  </div>
</th:block>
```

<br/>

### 자바스크립트 인라인

`<script th:inline="javascript">` : 자바스크립트에서 타임리프를 편리하게 사용할 수 있는 기능 제공

- 텍스트 렌더링
  - `var username = [[${user.username}]];`
    - 인라인 사용 전 ➔ `var username = userA;`
      - 변수 이름이 그대로 남아있기 때문에 변수명으로 인식되어서 오류가 발생함
      - 숫자의 경우에는 `"`가 필요 없기 때문에 정상 렌더링됨
    - 인라인 사용 후 ➔ `var username = "userA";`
      - 문자 타입인 경우 `"`를 포함해줌
      - 자바스크립트 상에서 문제가 될 수 있는 문자가 포함되어 있으면 이스케이프 처리를 해줌
        - `"` ➔ `\"`
- 자바스크립트 내추럴 템플릿
  - 타임리프는 HTML 파일을 직접 열어도 동작하는 내추럴 템플릿 기능을 제공함
  - 자바스크립트 인라인을 이용하면 주석을 활용해서 해당 기능 사용 가능
  - `var username2 = /*[[${user.username}]]*/ "test username";` 
    - 인라인 사용 전 ➔ `var username2 = /*userA*/ "test username";`
      - 순수하게 그대로 해석되어 내추럴 템플릿 기능이 동작하지 않고, 렌더링 내용이 주석처리됨
    - 인라인 사용 후 ➔ `var username2 = "userA";`
      - 주석 부분이 제거되고 기능이 동작함
- 객체
  - 객체를 JSON으로 자동 변환함
  - `var user = [[${user}]];`
    - 인라인 사용 전 ➔ `var user = BasicController.User(username=userA, age=10);`
      - 객체의 `toString()` 함수가 호출된 값이 들어감
    - 인라인 사용 후 ➔ `var user = {"username":"userA","age":10};`

`th:each`
- 특정 요소를 for문처럼 반복처리하는 기능으로, 자바스크립트 인라인은 each를 지원함
  ```html
  <script th:inline="javascript">
    [# th:each="user, stat : ${users}"]
    var user[[${stat.count}]] = [[${user}]];
    [/]
  </script>
  ```
  결과
  ```
  <script>
      var user1 = {"username":"userA","age":10};
      var user2 = {"username":"userB","age":20};
      var user3 = {"username":"userC","age":30};
  </script>
  ```

<br/>

### 템플릿 조각

> 웹 페이지를 개발할 때 공통 영역들을 변경해야 할 경우, 모든 페이지를 다 수정해야 하기 때문에 비효율적임
> 해당 문제를 해결하기 위해 타임리프는 템플릿 조각과 레이아웃 기능을 지원함

`th:fragment`
- 해당 태그는 다른 곳에 포함되는 코드 조각으로 이해
  ```html
  <footer th:fragment="copy"> 
    푸터 자리 입니다.
  </footer>
  
  <footer th:fragment="copyParam (param1, param2)">
    <p>파라미터 자리 입니다.</p>
    <p th:text="${param1}"></p> 
    <p th:text="${param2}"></p>
  </footer>
  ```

`th:insert`: 현재 태그 내부에 추가
- 부분 포함
  - `<div th:insert="~{template/fragment/footer :: copy}"></div>`
    - `template/fragment/footer :: copy`
      - `template/fragment/footer.html` 템플릿에 있는 `th:fragment="copy"`라는 부분을 템플릿 조각으로 가져와서 사용한다는 의미
    ````html
    <h2>부분 포함 insert</h2>
    <div th:insert="~{template/fragment/footer :: copy}"></div>
    ````
    ```html
    <h2>부분 포함 insert</h2>
    <div>
      <footer>
        푸터 자리 입니다.
      </footer>
    </div>
    ```

`th:replace`: 현재 태그를 대체
- 부분 포함
  - `<div th:replace="~{template/fragment/footer :: copy}"></div>`
    ````html
    <h2>부분 포함 replace</h2>
    <div th:replace="~{template/fragment/footer :: copy}"></div>
    ````
    ```html
    <h2>부분 포함 replace</h2>
    <footer>
      푸터 자리 입니다.
    </footer>
    ```
- 부분 포함 단순식
  - `div th:replace="template/fragment/footer :: copy"></div>`
    - `~{...} `를 사용하는 것이 원칙이지만, 템플릿 조각을 사용하는 코드가 단순하면 생략 가능
- 파라미터 사용
  - `<div th:replace="~{template/fragment/footer :: copyParam ('데이터1', '데이터2')}"></
div>`
    - 파라미터를 전달해서 동적으로 조각을 렌더링할 수 있음
    ````html
    <h2>파라미터 사용</h2>
    <div th:replace="~{template/fragment/footer :: copyParam ('데이터1', '데이터 2')}"></div>
    ````
    ```html
    <h2>파라미터 사용</h2>
    <footer>
        <p>파라미터 자리 입니다.</p> 
        <p>데이터1</p>
        <p>데이터2</p>
    </footer>
    ```
  
<br/>

### 템플릿 레이아웃

코드 조각을 레이아웃에 넘겨서 사용
- 공통 정보들은 한 곳에 모아두고 사용하지만, 각 페이지마다 필요한 정보를 더 추가해서 이용하고 싶을 때 사용
- 레이아웃 개념을 두고, 레이아웃에 필요한 코드 조각을 전달해서 완성
- `base.html`
  ```html
  <html xmlns:th="http://www.thymeleaf.org">
  <head th:fragment="common_header(title,links)">
  
    <title th:replace="${title}">레이아웃 타이틀</title>
  
    <!-- 공통 -->
    <link rel="stylesheet" type="text/css" media="all" th:href="@{/css/awesomeapp.css}">
    <link rel="shortcut icon" th:href="@{/images/favicon.ico}">
    <script type="text/javascript" th:src="@{/sh/scripts/codebase.js}"></script>
  
    <!-- 추가 -->
    <th:block th:replace="${links}" />
  </head>
  ```
    - 타이틀이 전달한 `<title>` 태그 부분으로 교체
    - 공통 부분은 유지되고, 추가 부분에 전달한 `<links>`태그들이 포함됨

- `layoutMain.html`
  ````html
  <!DOCTYPE html>
  <html xmlns:th="http://www.thymeleaf.org">
  <head th:replace="template/layout/base :: common_header(~{::title},~{::link})">
      <title>메인 타이틀</title>
      <link rel="stylesheet" th:href="@{/css/bootstrap.min.css}">
      <link rel="stylesheet" th:href="@{/themes/smoothness/jquery-ui.css}">
  </head>
  <body>
  메인 컨텐츠
  </body>
  </html>
  ````
    - `common_header(~{::title},~{::link})`
      - `::title`은 현재 페이지의 title 태그들을 전달
      - `::link`는 현재 페이지의 link 태그들을 전달
- `<head>`뿐만 아니라 `<html>`전체에도 적용 가능

