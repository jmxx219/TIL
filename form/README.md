# Thymeleaf - Spring 통합과 폼

> 타임리프는 스프링과 통합을 위한 다양한 기능을 제공한다. ([스프링 통합 메뉴얼](https://www.thymeleaf.org/doc/tutorials/3.0/thymeleafspring.html))  
> 해당 기능을 이용하여 [상품 관리 서비스 개발](https://github.com/jmxx219/Spring-Study/blob/main/item-service/README.md) 프로젝트를 개선해보자

### 목차
- [입력 폼 처리](#입력-폼-처리) <br/>


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
  
