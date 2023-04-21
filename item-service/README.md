# 상품 관리 서비스 


## 요구사항 분석

### 상품 도메인 모델
- 상품 ID
- 상품명
- 가격
- 수량

### 상품 관리 기능
- 상품 목록
- 상품 상세
- 상품 등록
- 상품 수정

### 서비스 제공 흐름
요구사항이 정리되면 `디자이너`, `웹 퍼블리셔`, `백엔드 개발자`가 업무를 분담하여 진행
> - `디자이너` : 요구사항에 맞게 디자인 후, `웹 퍼블리셔`에게 전달
> - `웹 퍼블리셔` : 디자인을 기반으로 HTML, CSS를 만들어 `개발자`에게 제공
> - `백엔드 개발자` : HTML 화면이 나오기 전까지는 시스템 설계 및 핵심 비즈니스 로직 구현, HTML이 나오면 뷰 템플릿으로 변환해서 웹 화면의 흐름을 제어

**참고**

웹 클라이언트 기술(`React`, `Vue.js`)을 사용할 경우, 웹 프론트엔드 개발자가 HTML을 동적으로 만드는 역할과 웹 화면의
흐름을 담당함. 이 경우에 백엔드 개발자는 HTML 뷰 템플릿을 직접 만지지 않고 HTTP API를 통해 웹 클라이언트가 필요로 하는 데이터와 기능을 제공함.

<br/>

## 상품 서비스 HTML

- `/resources/static`에 완성된 HTML 파일을 넣으면 스프링 부트가 정적 리소스를 제공함
- 정적 리소스이기 때문에 파일 탐색기로 직접 열어도 동작함
- `/resources/static` 폴더에 정적 리소스는 공개됨
  - 서비스를 운영할 때 공개할 필요가 없는 HTML을 넣지 않도록 주의


<br/>

## 상품 관리 기능

### `BasicItemController`

- `@Controller` : 스프링빈 등록(`@Component`)
  - `ItemRepository`
    - `@Autowired`: 의존성 주입(생성자, setter, 필드)
      - 생성자 주입에서 생성자가 하나일 경우 `@Autowired` 애노테이션 생략 가능
    - `@RequiredArgsConstructor`: `final`이 붙은 필드의 생성자를 자동으로 만들어서 의존관계 주입
  - 테스트용 데이터 추가
    - `@PostConstruct`: 빈의 의존관계가 모두 주입되고 나서 초기화 용도로 사용

<br/>  
  
- 상품 목록 : `GET` - `/basic/items`
- 상품 상세 : `GET` - `/basic/items/{itemId}`
- 상품 등록 폼 : `GET` - `/basic/items/add`
- 상품 등록 처리 : `POST` - `/basic/items/add`
  - POST - HTML Form
  - `@RequestParam`로 요청 파라미터 형식 처리
      ```java
      @PostMapping("/add")
      public String addItemV1(@RequestParam String itemName, @RequestParam int price, @RequestParam Integer quantity, Model model) {
          ...  
          model.addAttribute("item", item);
      }
      ```
  - `@ModelAttribute`
    ```java
    @PostMapping("/add")
    public String addItemV2(@ModelAttribute("item") Item item) { }
    
    @PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item) { }
    
    @PostMapping("/add")
    public String addItemV4(Item item, Model model) { }
    ```
    - 객체를 생성하고, 요청 파라미터의 값을 프로퍼티 접근법(setXxx)으로 입력함
    - 모델(Model)에 `@ModelAttribute`로 지정한 객체를 자동으로 넣어줌 (`model.addAttribute()`생략 가능)
      - 모델에 데이터를 담을 때 이름은 `@ModelAttribute`에 지정한 `name(value)` 속성을 사용
    - `@ModelAttribute`의 `name` 생략 가능
      - 모델에 저장될 때 클래스명의 첫글자만 소문자로 변경해서 등록함
    - `@ModelAttribute` 자체 생략 가능
- 상품 수정 폼 : `GET` - `/basic/items/{itemId}/edit`
- 상품 수정 처리 : `POST` - `/basic/items/{itemId}/edit`
  - `redirect:/...`
    - 처리 후에 뷰 템플릿을 호출하지 않고 상품 상세 화면으로 이동하도록 리다이렉트를 호출함
    - 컨트롤러에 매핑된 `@PathVariable`의 값은 `redirect`에도 사용 가능
      - `redirect:/basic/items/{itemId}"`

  > HTML Form 전송은 `PUT`, `PATCH`를 지원 X, `GET`, `POST`만 사용 가능  
  >`PUT`, `PATCH`는 API 전송시에 사용함

### PRG Post/Redirect/Get

**상품 등록 처리 컨트롤러에서 문제점**

- 상품 등록을 완료하고 웹 브라우저의 새로고침 버튼을 클릭하면 상품이 계속해서 중복 등록

**전체 흐름**
- 상품 목록 -> 상품 등록 폼 -> 상품 저장 -> 상품상세(내부 호출 - 뷰 템플릿)

**POST 등록 후 새로 고침**
- 웹 브라우저의 새로 고침은 마지막으로 서버에 전송한 데이터를 다시 전송함
  - 상품 등록 폼에서 데이터를 입력하고 저장하면 `POST /add` + 상품 데이터를 서버로 전송
  - 이 상태에서 새로 고침하면 마지막에 전송한 `POST /add` + 상품 데이터를 다시 서버로 전송함
    - 내용은 같고, ID만 다른 상품이 계속 쌓이게됨

**POST, Redirect GET**
- 상품 저장 후에 뷰 템플릿으로 이동하지 않고, 상품 상세 화면으로 리다이렉트를 호출하여 해결
- 웹 브라우저는 리다이렉트의 영향으로 상품 저장 후에 실제 상품 상세 화면으로 이동(URL 변경)
- 마지막에 호출한 내용이 상품 상세 화면인 `GET /items/{id}`가 되기 때문에 새로고침 문제를 해결할 수 있음
  - 이러한 문제 해결 방식을 `PRG Post/Redirect/Get`이라고 함
```java
@PostMapping("/add")
public String addItemV5(@ModelAttribute Item item) {
    itemRepository.save(item);
    return "redirect:/basic/items/" + item.getId();
}
```
  - `redirect`에서 `+ item.getId()`처럼 URL에 변수를 더해서 사용하는 것은 URL 인코딩이 되지 않아 위험함

### RedirectAttributes

URL 인코딩 및 `pathVarible`과 쿼리 파라미터까지 처리해줌

```java
@PostMapping("/add")
public String addItemV6(@ModelAttribute Item item, RedirectAttributes redirectAttributes) {
    Item savedItem = itemRepository.save(item);
    redirectAttributes.addAttribute("itemId", savedItem.getId());
    redirectAttributes.addAttribute("status", true);
    return "redirect:/basic/items/{itemId}";
}
```
- `redirect:/basic/items/{itemId}`
  - pathVariable 바인딩: `{itemId}`
  - 나머지는 쿼리 파라미터로 처리: `?status=true`

<br/>

---

<br/>

# Thymeleaf

`/resources/templates` 동적 리소스

JSP
- JSP 파일을 웹 브라우저에서 열면 JSP 소스 코드와 HTML이 섞여 확인이 불가능함
- 오직 서버를 통해 열어야함

타임리프
- 순수 HTML 파일을 웹 브라우저에서 열어 내용을 확인할 수 있음
- 서버를 통해 뷰 템플릿을 거치면 동적으로 변경된 결과를 확인할 수 있음

### **natural templates**

순수 HTML을 그대로 유지하면서 뷰 템플릿을 사용할 수 있는 타임리프의 특징
- `th:xxx`가 붙은 부분은 서버사이드에서 렌더링되며 기존 것을 대체, 없으면 기존 html 속성 `xxx`을 그대로 사용
- HTML 파일을 직접 열면 `th:xxx`가 있어도 웹 브라우저는 무시함
  - HTML 파일 보기를 유지하면서 템플릿 기능도 함

## 사용

**선언** 
- `<html xmlns:th="http://www.thymeleaf.org">`

**속성 변경**
- `th:href`
  - HTML을 그대로 볼 때는 `href` 속성이 사용되고, 뷰 템플릿을 거치면서 `th:href``의 값이 `href`로 대체되면서 동적으로 변경됨
    - 원래의 값 `href="value1"`에서 `th:href="value2"`으로 변경, 없다면 새로 생성
- `th:onclick`
  - `th:onclick="|location.href='@{/basic/items/add}'|"` : 클릭 시, 상품 등록 폼으로 이동
- `th:value`
  - 모델에 있는 정보를 가져와 출력
    - `th:value="${item.id}"`
  - `th:action`
    - HTML Form에서 `action`에 값이 없으면 현재 URL에 데이터를 전송

**내용 변경**
- `th:text`
  - 내용의 값을 `th:text`의 값으로 변경
    - `td th:text="${item.price}">10000</td>`

**변수 표현식**
- `${...}`
  - 모델에 포함된 값이나, 타임리프 변수로 선언한 값을 조회할 수 있음
    - `<td th:text="${item.price}">10000</td>`
  - 프로퍼티 접근법을 사용(`item.getPrice()`)

**리터럴 대체**
- `|...|`
  - 타임리프에서 문자와 표현식 등은 분리되어 있기 때문에 더해서 사용해야 함
    - `<span th:text="'Welcome to our application, ' + ${user.name} + '!'">`
  - 리터럴 대체 문법을 사용하면 더하기 없이 편리하게 사용 가능
    - `<span th:text="|Welcome to our application, ${user.name}!|">`

**URL 링크 표현식**
- `@{...}`
  - 타임리프는 URL 링크를 사용하는 경우 `@{...}`를 사용
    - `th:href="@{/css/bootstrap.min.css}"`
  - 경로를 템플릿처럼 사용 가능
    - 경로 변수 사용(`itemId`)
      - `th:href="@{/basic/items/{itemId}(itemId=${item.id})}"`
    - 쿼리 파라미터 생성
      - `th:href="@{/basic/items/{itemId}(itemId=${item.id}, query='test')}"`
        - 생성 링크: `http://localhost:8080/basic/items/1?query=test`
    - 리터럴 사용
      - `th:href="@{|/basic/items/${item.id}|}"`
  

**반복 출력**
- `th:each`
  - `<tr th:each="item : ${items}">`
    - `model.addAttribute("items", items);`
    - 모델에 포함된 `item` 컬렉션 데이터에서 `item` 변수에 하나씩 포함되어 반복문 안에서 사용 가능함

**조건**
- `th:if`
  - 해당 조건이 참이면 실행

**Etc**
- `${param.status}`
  - 타임리프에서 쿼리 파라미터를 편리하게 조회하는 기능