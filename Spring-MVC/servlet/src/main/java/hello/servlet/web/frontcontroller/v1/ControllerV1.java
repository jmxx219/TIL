package hello.servlet.web.frontcontroller.v1;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ControllerV1 {

    // 서블릿과 비슷한 모양의 컨트롤러 인터페이스 도입
    // 각 컨트롤러들은 이 인터페이스를 구현
    // 프론트 컨트롤러는 이 인터페이스를 호출해서 구현과 관계없이 로직의 일관성을 가져갈 수 있음
    void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

}
