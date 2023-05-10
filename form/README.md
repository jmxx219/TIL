# Thymeleaf - Spring 통합과 폼

> 타임리프는 스프링과 통합을 위한 다양한 기능을 제공한다. ([스프링 통합 메뉴얼](https://www.thymeleaf.org/doc/tutorials/3.0/thymeleafspring.html))  
> 해당 기능을 이용하여 [상품 관리 서비스 개발](https://github.com/jmxx219/Spring-Study/blob/main/item-service/README.md) 프로젝트를 개선해보자  


**요구사항 추가**
- 판매여부
  - 판매 오픈 여부
  - 체크 박스로 선택
- 등록 지역
  - 서울, 부산, 제주
  - 체크 박스로 다중 선택
- 상품 종류
  - 도서, 식품, 기타
  - 라디오 버튼 하나 선택
- 배송 방식
  - 빠른 배송, 일반 배송, 느린 배송
  - 셀렉트 박스로 하나 선택



### 목차
- [입력 폼 처리](#입력-폼-처리)
- [체크 박스](#체크-박스)


---

<br/>

## 입력 폼 처리

```html
 <form action="item.html" th:action th:object="${item}" method="post">
    <div>
        <label for="itemName">상품명</label>
        <input type="text" id="itemName" th:field="*{itemName}" class="form-control" placeholder="이름을 입력하세요">
    </div>
</form>
```

- `th:object`
    - 커맨트 객체를 지정
    - `<form>`에서 사용할 객체를 지정
    - 선택 변수 식(`*{...}`) 적용 가능
- `*{...}`
    - 선택 변수 식으로, `th:object`에서 선택한 객체에 접근
- `th:field`
    - HTML 태그의 `id`, `name`, `value` 속성을 자동으로 처리함
        - 렌더링 전: `<input type="text" th:field="*{itemName}" />`
        - 렌더링 후: `<input type="text" id="itemName" name="itemName" th:value="" />`
    - `*{itemName}` `==` `${item.itemName}`

<br/>

## 체크 박스

**단일**
- HTML Form
  - checkbox 선택이 안되면 클라이언트에서 서버로 값 자체를 보내지 않음
    - 수정의 경우에 체크되어 있던 값을 해제해도 아무런 값이 넘어가지 않아 문제가 될 수 있음
  - 히든 필드를 만들어서 해결
    - 히든 필드는 항상 전송
    - 기존 체크 박스 이름 앞에 언더스코어(`_`)를 붙여서 전송하면 체크를 해제했다고 인식
      - `<input type="hidden" name="_open" value="on"/>`
      - 체크를 해제한 경우 기존 체크 박스 `open`은 전송되지 않고, `_open`만 전송됨 
      - 이때 Spring MVC에서는 체크를 해제했다고 판단
- 타임리프 체크 박스 HTML
  - `<input type="checkbox" id="open" th:field="*{open}" class="form-check-input">`
  - 타임리프를 사용하면 체크 박스의 히든 필드와 관련된 부분을 해결함
    - HTML 생성 결과를 보면 히든 필드 부분이 자동으로 생성됨
  - `checked="checked"` 체크 확인
    - 체크 박스를 체크하면 조회 시, `checked` 속성이 추가됨
    - 타임리프의 `th:field`를 사용하면 값이 `true`인 경우 체크를 자동으로 처리함

**멀티**

- `@ModelAttribute`
  ```java
  @ModelAttribute("regions")
  public Map<String, String> regions() {
      Map<String, String> regions = new LinkedHashMap<>();
      regions.put("SEOUL", "서울");
      regions.put("BUSAN", "부산");
      regions.put("JEJU", "제주");
      return regions;
  }
  ```
  - 등록, 상세화면, 수정 폼에서 모두 동일한 체크 박스를 반복해서 보여줄 경우
    - 각각의 컨트롤러에서 `model`에 데이터를 반복해서 넣어주어야 함
    - `model.addAttribute("regions", regions)`
  - `@ModelAttribute`는 컨트롤러에 있는 별도의 메서드 적용 가능
    - 컨트롤러에 요청할 때 `regions()`에서 반환된 값이 자동으로 `model`에 들어감
- `th:each` 멀티 박스
  ```html
  <div th:each="region : ${regions}" class="form-check form-check-inline">
      <input type="checkbox" th:field="*{regions}" th:value="${region.key}" class="form-check-input">
      <label th:for="${#ids.prev('regions')}" th:text="${region.value}" class="form-check-label">서울</label>
  </div>
  ```
  - 멀티 박스는 같은 이름의 여러 체크 박스 만들 수 있기 때문에 HTML 태그 속성에서 `name`은 같아도, `id`는 달라야 함
    - 타임리프는 체크박스를 `th:each` 루프 안에서 반복해서 만들 때 `id`에 임의로 숫자를 뒤에 붙여줌
  - `th:for="${#ids.prev('regions')}"`
    - `id`가 타임리프에 의해 동적으로 만들어지기 때문에 `<label for="id 값">`으로 `label`의 대상이 되는 `id` 값을 지정하는 것은 안됨
    - 타임리프는 `ids.prev(...)`, `ids.next(...)`을 제공하여 동적으로 생성되는 id 값을 사용할 수 있게 함