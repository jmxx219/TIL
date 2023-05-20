# 로그인 처리

> [상품 관리 시스템](https://github.com/jmxx219/Spring-Study/blob/main/item-service/README.md) 프로젝트에 로그인 기능을 구현해보자

### 목차


<br/>

### 패키지 구조 설계

**package 구조**
- hello.login 
  - domain 
    - item 
    - member 
    - login
  - web 
    - item
    - member
    - login

**도메인**

- 화면, UI, 기술 인프라 등등의 영역을 제외한 시스템이 구현해야 하는 핵심 비즈니스 업무 영역
- 향후 web을 다른 기술로 바꾸어도 도메인은 그대로 유지할 수 있어야 함
  - web은 domain을 알고있지만(의존), domain은 web을 모르도록(의존 X) 설계