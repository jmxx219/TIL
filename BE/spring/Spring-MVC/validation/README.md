# Validation

> [상품 관리 시스템](https://github.com/jmxx219/Spring-Study/blob/main/item-service/README.md) 프로젝트에 검증 로직을 추가해보자

### 목차
- [검증 직접 처리](#검증-직접-처리)
- [BindingResult](#BindingResult)
- [FieldError와 ObjectError](#FieldError와-ObjectError)
- [오류 코드와 메시지 처리](#오류-코드와-메시지-처리)
- [Validator](#Validator)
- [Bean Validation](#Bean-Validation)
  - [소개](#소개)
  - [직접 사용](#직접-사용)
  - [스프링 적용](#스프링-적용)
  - [에러 코드](#에러-코드)
  - [오브젝트 오류](#오브젝트-오류)
  - [groups](#groups)
  - [Form 전송 객체 분리](#Form-전송-객체-분리)
  - [HTTP 메시지 컨버터](#HTTP-메시지-컨버터)

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


## FieldError와 ObjectError

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

<br/>

## 오류 코드와 메시지 처리

**1. `errors.properties`**
- 오류 메시지를 별도로 관리
- `FieldError`의 `codes`에 오류 메시지 코드를 지정하고, `arguments`로 메시지 매개변수 값을 전달함

**2. `BindingResult`**
- 컨트롤러에서 `BindingResult`는 검증해야 할 객체 `target` 바로 다음에 위치함
  - `BindingResult`는 검증해야 할 `target`에 대해 이미 알고 있음
  - `target`에 대한 정보 불필요
- `rejectValue()`, `reject()`
  - `FieldError`와 `ObjectError`를 직접 생성하지 않고 검증 에러를 다룰 수 있음
  - `rejectValue(@Nullable String field, String errorCode, @Nullable Object[] errorArgs, @Nullable String defaultMessage)`
    - `field` : 오류 필드명 
    - `errorCode` : 오류 코드(메시지에 등록된 코드 X, `messageResolver`를 위한 오류 코드)
    - `errorArgs` : 오류 메시지에서 매개변수를 치환하기 위한 값 
    - `defaultMessage` : 오류 메시지를 찾을 수 없을 때 사용하는 기본 메시지

**3. 오류 코드**
- 메시지 우선순위(단계)
  - 메시지를 단순하게 만들면 범용성이 좋아 여러 곳에서 사용 가능, but 세밀하게 작성하기 어려움
  - 너무 자세하게 만들면 범용성이 떨어짐
  - 따라서 범용성으로 사용하다가 세밀하게 작성해야 하는 경우에만 세밀하게 적용되도록 메시지에 단계를 둠
- `MessageCodesResolver`
  - 인퍼페이스로, 검증 오류 코드로 메시지 코드를 생성함
  - `DefaultMessageCodesResolver`가 기본 구현체
    - 기본 메시지 생성 규칙
      - 객체 오류: `code + "." + object name` ➔ `code`
      - 필드 오류: `code + "." + object name + "." + field` ➔ `code + "." + field` ➔ `code + "." + field type` ➔`code`

**4. 스프링이 직접 만든 오류 메시지 처리**
- 개발자가 직접 설정한 오류 코드 ➔ `rejectValue()` 직접 호출
- 스프링이 직접 검증 오류에 추가한 경우(주로 타입 정보 불일치)
  - `typeMismatch`: 스프링은 타입 오류 발생 시, 해당 오류 코드를 사용
  - 메시지에 `typeMismatch`를 이용하여 메시지 코드를 추가해서 사용
    - `typeMismatch.java.lang.Integer=숫자를 입력해주세요.`
  
**동작 방식**

1. `rejectValue()`, `reject()` 호출
2. `MessageCodesResolver`를 통해 검증 오류 코드로 메시지 코드들 생성
3. `FieldError`, `ObjectError`를 생성하면서 메시지 코드들을 보관
   - 생성자를 보면, 여러 개의 오류 코드를 가질 수 있음
4. `th:erros`에서 메시지 코드들로 메시지를 순서대로 찾고, 노출

<br/>


## Validator

- 컨트롤러에서 검증 로직이 차지하는 부분이 큼 ➔ 검증 로직 별도 분리
  - 별도의 클래스로 역할을 분리
  - 분리한 검증 로직 재사용 가능
- `Validator` 인터페이스
  - 스프링에서 검증 기능을 체계적으로 도입하기 위해 제공
  ```java
  public interface Validator {
      boolean supports(Class<?> clazz);
      void validate(Object target, Errors errors);
  }
  ```
    - `supports()`: 해당 검증기를 지원하는 여부 확인
    - `validate()`: 검증 대상 객체(`target`)와 `BindingResult`(`Errors`)

**Validator 이용**
- `Validator` 직접 호출
  - `ItemValidator`를 스프링 빈으로 주입받아서 직접 호출
    - `itemValidator.validate(item, bindingResult)`
- `WebDataBinder`
  - 스프링의 파라미터 바인딩의 역할 및 검증 기능을 내부에 포함함
  ```java
  @InitBinder
  public void init(WebDataBinder dataBinder) {
      dataBinder.addValidators(itemValidator);
  }
  ```
  - `WebDataBinder`에 검증기를 추가 ➔ 해당 컨트롤러에서 검증기를 자동 적용 가능
    - `@InitBinder`: 해당 컨트롤러에만 영향을 줌
    - 글로벌 설정으로 모든 컨트롤러에 적용할 수도 있음
  - `@Validated` 적용
    - 검증기를 실행하라는 애노테이션으로, `WebDataBinder`에 등록한 검증기를 찾아서 실행
      - `supports()` 메소드를 이용하여 여러 검증기 중에 실행되어야 할 검증기를 찾음 
    - `public String addItemV6(@Validated @ModelAttribute Item item, ...) { }`
      - `Validator`를 직접 호출하는 부분이 사라지고, 검증 대상 앞에 `@Validated`가 붙음
    - 검증 시, `@Validated`와 `@Valid` 둘 다 사용 가능
      - `@Validated`: 스프링 전용 검증 애노테이션
      - `@Valid`: 자바 표준 검증 애노테이션(`build.gradle` 의존관계 추가가 필요)

<br/>

## Bean Validation

### 소개
- 검증 로직을 모든 프로젝트에 적용할 수 있게 공통화하고, 표준화한 것
- 애노테이션 하나도 검증 로직을 매우 편리하게 적용할 수 있음
- 특정한 구현체가 아닌 검증 애노테이션과 여러 인터페이스의 모음
  - `Hibernate Validator`: Bean Validation을 구현하여 일반적으로 사용하는 구현체

### 직접 사용

- 의존관계 추가
  - `implementation 'org.springframework.boot:spring-boot-starter-validation'`
- Bean Validation 애노테이션 적용
  - `@NotBlank`: 빈값 + 공백만 있는 경우 허용하지 X
  - `@NotNull`: `null`을 허용하지 X
  - `@Range(min = 1000, max = 1000000)`: 해당 범위 값만 허용
  - `@Max(9999)`: 최대 해당 값까지만 허용
- 검증기 생성
  ```java
  ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
  Validator validator = factory.getValidator();
  ```
- 검증 실행
  - `Set<ConstraintViolation<Item>> validations = validator.validate(item)`
    - 검증 대상(`Item`)을 직접 검증기에 넣고 결과를 받음
    - `ConstraintViolation`이라는 검증 오류가 담김
    - 검증 오류가 발생한 객체, 필드, 메시지 정보 등 다양한 정보 확인 가능

### 스프링 적용

- 스프링 부트가 `spring-boot-starter-validation` 라이브러리를 넣으면 자동으로 Bean Validator를 인지하고 스프링에 통합됨
- 스프링 부트는 `LocalValidatorFactoryBean`을 자동으로 글로벌 Validator로 등록함
  - `@Valid` 또는 `@Validated`만 적용하면, 해당 Validator가 `@NotNull`과 같은 애노테이션을 보고 검증을 수행함
    - 검증 오류가 발생할 경우, `FieldError`, `ObjectError`를 생성해서 `BindingResult`에 넣어줌

**검증 순서**
1. `@ModelAttribute` 각각의 필드에 타입 변환 시도
   1. 성공하면 다음으로
   2. 실패하면 `typeMismatch`로 `FieldError` 추가
2. Validator 적용
   - 바인딩에 성공한 필드만 Bean Validation 적용

### 에러 코드

- Bean Validation을 적용하고 `bindingResult`에 등록된 검증 오류 코드를 보면 오류 코드가 애노테이션 이름으로 등록됨
  - `typeMismatch`와 유사
  - `NotBlank`와 같은 오류 코드를 기반으로 `MessageCodesResolver`를 통해 다양한 메시지 코드가 순서대로 생성됨
- Bean Validation이 기본으로 제공하는 오류 메시지 변경
  - 메시지 등록
    - `{0}`: 필드명
    - `{1}`, `{2}` 등 : 각 애노테이션마다 다름
- Bean Validation 메시지 찾는 순서
  - 생성된 메시지 코드 순서대로 `messageSource`에서 메시지 찾기
  - 애노테이션의 `message` 속성 사용 ➔ `@NotBlank(message = "공백! {0}")`
  - 라이브러리가 제공하는 기본 값 사용
  

### 오브젝트 오류

- `@ScriptAssert()`
  - 특정 필드(`FieldError`)가 아닌 해당 오브젝트 관련 오류(`ObjectError`) 처리
- 제약
  - 실제로 사용하면 제약이 많고 복잡함
  - 실무에서는 검증 기능이 해당 객체의 범위는 넘어서는 경우가 있어서 대응이 어려움
  - 오브젝트 오류(글로벌 오류)의 경우 `@ScriptAssert()`을 사용하는 것보다 관련 부분만 직접 자바 코드로 작성하는 것을 권장함

### groups

**Bean Validation 한계**
- 데이터를 등록할 때와 수정할 때의 요구사항이 다를 수 있음
  - 등록과 수정에서 검증 조건의 충돌이 발생
  - 등록과 수정이 같은 Bean Validation을 적용할 수 없는 문제가 발생할 수 있음

**동일한 모델 객체를 등록할 때와 수정할 때 각각 다르게 검증하는 방법**
  1. Bean Validation의 groups 기능을 사용
  2. `Item`을 직접 사용하지 않고, 폼 전송을 위한 별도의 모델 객체를 만들어 사용

**groups 적용**
- 검증할 기능을 각각 그룹으로 나누어 적용 가능
  - `SaveCheck`, `UpdateCheck` 인터페이스 생성
  - `@NotNull(groups = UpdateCheck.class)`로 수정 시에만 적용
  - `@NotBlank(groups = {SaveCheck.class, UpdateCheck.class})`로 등록과 수정시 모두 적용
- `@Valid`에는 groups를 적용할 수 있는 기능이 없음
- groups 기능 사용 시에는 `@Validated`만 사용

### Form 전송 객체 분리

**groups의 한계**
- 회원 등록 시, 회원과 관련된 데이터 뿐만아니라 객체와 관계없는 부가 데이터가 넘어올 수 있음
- 등록 시 폼에서 전달하는 데이터가 도메인 객체(`Item`)와 딱 맞지 않기 때문에 실무에서는 잘 사용하지 않음
- 따라서 복잡한 폼의 데이터를 컨트롤러까지 전달할 별도의 객체를 만들어서 전달함
  - `ItemSaveForm`과 같이 폼을 전달받는 전용 객체를 만들어서 `@ModelAttribute`로 사용
  - 컨트롤러에서 해당 폼 데이터를 전달받고, 이후 컨트롤러에서 필요한 데이터를 이용해 `Item`을 생성함

**폼 데이터 전달**
- **Item** 도메인 객체 사용
  - `HTML Form ➔ Item ➔ Controller ➔ Item ➔ Repository`
  - 장점
    - Item 도메인 객체를 컨트롤러, 레파지토리까지 직접 전달하기 때문에 중간에 Item을 만드는 과정이 없어 간단함
  - 단점
    - 간단한 경우에만 적용 가능
    - 수정 시에 검증이 중복될 수 있고, groups를 사용해야 함
- 별도의 객체 사용
  - `HTML Form ➔ ItemSaveForm ➔ Controller ➔ Item 생성 ➔ Repository`
  - 장점
    - 전송하는 폼 데이터가 복잡해도 거기에 맞춘 별도의 폼 객체를 사용해서 데이터를 전달 받을 수 있음
    - 보통 등록용과 수정용으로 별도의 폼 객체를 만들기 때문에 검증이 중복되지 않음
  - 단점
    - 폼 데이터를 기반으로 컨트롤러에서 Item 객체를 생성하는 변환 과정이 추가됨

**등록, 수정용 뷰 템플릿**
- 등록용과 수정용 뷰 템플릿이 비슷할 경우, 합치면 수많은 분기문때문에 나중에 유지보수하기 어려울 수 있음
- 어설픈 분기문들이 보이기 시작하면, 분리하는 것이 좋음

**적용**
- `Item`
  - 해당 도메인에서 검증은 사용하지 않음
- `ItemSaveForm`
  - `Item` 저장용 폼
  ```java
  @PostMapping("/add")
  public String addItem(@Validated @ModelAttribute("item") ItemSaveForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes){
      ...
  }
  ```
- `ItemUpdateForm`
  - `Item` 수정용 폼
  ```java
  @PostMapping("/{itemId}/edit")
  public String edit(@PathVariable Long itemId, @Validated @ModelAttribute("item") ItemUpdateForm form, BindingResult bindingResult){
      ...
  }
  ```

### HTTP 메시지 컨버터

- `@Valid`와 `@Validated`는 `HttpMessageConverter`(`@RequestBody`)에도 적용 가능
  - `@ModelAttribute`는 HTTP 요청 파라미터(URL 쿼리 스트링, POST Form)를 다룰 때 사용
  - `@RequestBody`는 HTTP Body의 데이터를 객체로 변환할 때 사용
- API 요청
  - 성공 요청: 성공
  - 실패 요청: JSON을 객체로 생성하는 것 자체를 실패
    - `HttpMessageConverter`에서 요청 JSON을 해당 객체로 생성하는데 실패함
    - 객체 자체를 만들지 못하기 때문에 컨트롤러 호출 X
    - 컨트롤러 호출 전에 예외가 발생함(Validator도 실행 X)
  - 검증 오류 요청: JSON을 객체로 생성하고, 검증에서 실패
    - `HttpMessageConverter`에서 요청 JSON을 해당 객체로 생성하는데 성공
    - 검증(Validator)에서 오류 발

**`@ModelAttribute` VS `@RequestBody`**
- `@ModelAttribute`
  - HTTP 요청 파라미터를 처리하는 `@ModelAttribute`는 각각의 필드 단위로 세밀하게 적용됨
    - 특정 필드에 타입이 맞지 않는 오류가 발생해도 나머지 필드는 정상 처리 가능
    - Validator를 사용한 검증 적용 가능
- `@RequestBody`
  - `HttpMessageConverter`는 `ModelAttribute`와 다르게 전체 객체 단위로 적용됨
    - 메시지 컨버터의 작용이 성공해서 객체를 만들어야 `@Valid`와 `@Validated`가 적용됨
  - `HttpMessageConverter` 단계에서 JSON 데이터 객체로 변경하지 못하면 예외가 발생함
    - Validator를 사용한 검증 불가능