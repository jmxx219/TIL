package hello.core.lifecycle;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//public class NetworkClient implements InitializingBean, DisposableBean {
public class NetworkClient {

    private String url;

    public NetworkClient() {
        System.out.println("생성자 호출, url = " + url);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    //서비스 시작시 호출
    public void connect() {
        System.out.println("connect: " + url);
    }

    public void call(String message) {
        System.out.println("call: " + url + " message = " + message);
    }

    //서비스 종료시 호출
    public void disconnect() {
        System.out.println("close: " + url);
    }

    /**
     * 빈 생명주기 콜백 함수 지원
     * 1. 인터페이스(InitializingBean, DisposableBean) -> 초창기 방법
     * 2. 설정 정보에 초기화 메서드, 종료 메서드 지정 - @Bean(initMethod = "init", destroyMethod = "close")
     * 3. @PostConstruct, @PreDestroy 애노테이션 지원
     */
//    @Override
//    public void afterPropertiesSet() throws Exception {
    @PostConstruct
    public void init() throws Exception {
        System.out.println("NetworkClient.init");
        connect();
        call("초기화 연결 메시지");
    }

//    @Override
//    public void destroy() throws Exception {
    @PreDestroy
    public void close() throws Exception {
        System.out.println("NetworkClient.close");
        disconnect();
    }
}
