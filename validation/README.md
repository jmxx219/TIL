# Validation

> [상품 관리 시스템](https://github.com/jmxx219/Spring-Study/blob/main/item-service/README.md) 프로젝트에 검증 로직을 추가해보자

### 목차
- [검증 직접 처리](#검증-직접-처리)
- [BindingResult](#BindingResult)
- [FieldError, ObjectError](#FieldError,-ObjectError)

<br/>

**검증 요구사항 추가**

- 타입 검증
  - 가격, 수량에 문자가 들어가면 검증 오류 처리
- 필드 검증
  - 상품명: 필수, 공백 X
  - 가격: 1,000원 이상, 1백만원 이하
  - 수량: 최대 999
- 특정 필드의 범위를 넘어서는 검증
  - 가격 * 수량의 합은 10,000원 이상
  
> 컨트롤러의 중요한 역할 중 하나는 **HTTP 요청이 정상인지 검증하는 것**

**클라이언트 검증, 서버 검증**
- 클라이언트 검증은 조작할 수 있어 보안에 취약함
- 서버만 검증할 경우, 즉각적인 고객 사용성 부족
- 둘을 적절히 섞어 사용하고 최종적으로 서버 검증은 필수
- API 방식을 사용할 경우, API 스펙을 잘 정의해서 `검증 오류`를 `API 응답 결과`에 잘 남겨주어야 함

---

<br/>


## 검증 직접 처리

검증 로직
- **검증 오류 보관**
  - `Map<String, String> errors = new HashMap<>()`
  - 검증 시, 오류가 발생하면 어떤 검즈에서 오류가 발생했는지 정보를 담아둠

- **필드 검증 로직**
  ```java
  if(!StringUtils.hasText(item.getItemName())) {
      errors.put("itemName", "상품 이름은 필수입니다.");
  }
  ```
- **특정 필드의 범위를 넘어선(복합) 검증 로직**
  ```java
  if(item.getPrice() != null && item.getQuantity() != null) {
      int resultPrice = item.getPrice() * item.getQuantity();
      if(resultPrice < 10000) {
          errors.put("globalError", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice);
      }
  }
  ```
  - 특정 필드를 넘어서는 오류 처리할 때, 필드 이름 대신 `globalError`라는 `key`를 사용
  
- **검증 실패 시, 다시 입력 폼으로 이동**
  ```java
  if(!errors.isEmpty()) {
      log.info("errors = {}", errors);
      model.addAttribute("errors", errors);
      return "validation/v1/addForm";
  }
  ```
  - 검증에서 오류 메시지가 하나라도 있을 경우
    - 오류 메시지를 출력하기 위해 `model`에 `eroors`를 넣고, 입력 폼이 있는 뷰 템플릿으로 이동함

오류 메시지 출력
- 타임리프의 `th:if`를 사용하여 errors`에 내용이 있을 때만 해당 HTML 태그 출력
  ```html
  <div th:if="${errors?.containsKey('globalError')}">
      <p class="field-error" th:text="${errors['globalError']}">전체 오류 메시지</p>
  </div>
  ```
  - `errors`가 `null`일 경우, `containsKey`를 호출하면 `NullPointerException` 예외 발생
  - `errors?.`를 사용하면 예외가 발생하는 대신, `null`을 반환하여 오류 메시지가 출력되지 않음

**🚨 문제점**
- 뷰 템플릿의 중복 처리가 많음
- 타입 오류 처리 안됨
  - 숫자 필드는 타입이 `Integer`이기 때문에 문자 타입으로 설정하는 것이 불가능
  - 숫자 타입에 문자가 들어오면 오류 발생
    - 해당 오류는 스프링 MVC에서 컨트롤러에 진입하기 전에 예외가 발생함
    - 컨트롤러 호출 X, 400 예외가 발생하면서 오류 페이지를 띄움
- 타입 오류가 발생해도 고객이 입력한 문자를 화면에 남겨야 함
  - `Integer`인 `price`에 문자를 입력할 경우, 타입이 달라 문자를 보관할 수 없음
  - 문자는 바인딩이 불가능 ➔ 고객이 입력한 문자는 사라짐 ➔ 본인이 어떤 내용을 입력해서 오류가 발생했는지 알기 어려움
- 고객이 입력한 값도 어딘가에 별도로 관리 되어야함


<br/>

## BindingResult

- 스프링이 제공하는 검증 오류를 보관하는 객체
  - `BindingResult`의 파라미터의 위치는 검증할 대상(`@ModelAttribute` 객체) 바로 뒤에 와야함
  - `@ModelAttribute`에 바인딩 시 타입 오류가 발생할 경우
    - `BindingResult`가 없으면 ➔ 400 오류가 발생, 컨트롤러 호출 X, 오류 페이지로 이동
    - `BindingResult`가 있으면 ➔ 오류 정보(`FieldError`)를 `BindingResult`에 보관, 컨트롤러 정상 호출
  - `BindingResult`는 `Model`에 자동 포함됨
  - `addError()`로 오류를 보관함
    - `bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."))`
- **FieldError**
  - 필드에 오류가 있을 때, 해당 객체를 생성해서 `BindingResult`에 담아둠
  - `FieldError(String objectName, String field, String defaultMessage)`
    - `objectName`: @ModelAttribute 이름
    - `field`: 오류가 발생한 필드 이름
    - `defaultMessage`: 오류 기본 메시지
- **ObjectError**
  - 특정 필드를 넘어서는 오류가 있을 때, 해당 객체를 생성해서 `BindingResult`에 담아둠
    - `ObjectError(String objectName, String defaultMessage)`

**BindingResult에 검증 오류를 적용하는 방법**
1. `@ModelAttribute`의 객체에 타입 오류 등으로 바인딩이 실패하는 경우, 스프링이 `FieldError`를 생성해서 `BindingResult`에 넣어줌
2. 개발자가 직접 적용
3. `Validator` 사용

**타임리프 스프링 검증 오류 통합 기능**
- 타임리프는 스프링의 `BindingResult`를 활용하여 편리하게 검증 오류를 표현하도록 기능을 제공함
  - `#fields`: `BindingResult`가 제공하는 검증 오류에 접근 
  - `th:errors`: 해당 필드에 오류가 있는 경우에 태그를 출력(`th:if`의 편의 버전)
  - `th:errorclass`: `th:field`에서 지정한 필드에 오류가 있으면 `class` 정보를 추가함
- 글로벌 오류 처리
  - `th:if="${#fields.hasGlobalErrors()}"`
  - `th:each="err : ${#fields.globalErrors()}"`
- 필드 오류 처리
  - `th:errorclass="field-error"`
  - `th:errors="*{itemName}"`

**🚨 문제점**
- 오류가 발생하는 경우, 고객이 입력한 내용이 모두 사라짐

<br/>


## FieldError, ObjectError

- `FieldError`와 `ObjectError` 두 가지 생성자를 제공함
  - `FieldError(String objectName, String field, @Nullable Object rejectedValue, boolean bindingFailure, @Nullable String[] codes, @Nullable Object[] arguments, @Nullable String defaultMessage)`
    - `rejectedValue`: 사용자가 입력한 값(거절된 값)
    - `bindingFailure`: 타입 오류 같은 바인딩 실패인지, 검증 실패인지 구분 값 
    - `codes`: 메시지 코드 
    - `arguments`: 메시지에서 사용하는 인자 

**오류 발생 시, 사용자 입력 값을 유지**
- 사용자의 입력 데이터가 컨트톨러의 `@ModelAttribute`에 바인딩되는 시점에 오류 발생 시, 모델 객체에 사용자 입력 값을 유지하기 어려움
- 오류가 발생한 경우, 사용자 입력 값을 보관하는 별도의 방법 필요
  - 보관한 사용자 입력 값을 검증 오류 발생 시 화면에 다시 출력
- `FieldError`의 `rejectedValue`: 오류 발생 시, 사용자 입력 값을 저장하는 필드
- 타임리프의 사용자 입력 값 유지
  - `th:field`는 보통 모델 객체의 값을 사용하지만, 오류가 발생하면 `FieldError`에서 보관한 값을 사용함
- 스프링의 바인딩 오류 처리
  - 타입 오류로 바인딩에 실패하면 스프링은 `FieldError`를 생성하여 사용자가 입력한 값을 넣어둠
  - 해당 오류를 `BindingResult`에 담아서 컨트롤러를 호출함
  - 타입 오류 같은 바인딩 실패에도 사용자의 오류 메시지를 정상 출력할 수 있음