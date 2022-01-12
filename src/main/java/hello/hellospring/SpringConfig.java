package hello.hellospring;

/**
 * 자바 코드로 직접 스프링 빈 등록하기
 */

import hello.hellospring.aop.TimeTraceAop;
import hello.hellospring.repository.JpaMemberRepository;
import hello.hellospring.repository.MemberRepository;
import hello.hellospring.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

//     DataSource는 데이터베이스 커넥션을 획득할 때 사용하는 객체다.
//     스프링 부트는 데이터베이스 커넥션 정보를 바탕으로 DataSource를 생성하고 스프링 빈으로 만들어둔다.
//    private final DataSource dataSource;

//    @Autowired
//    public SpringConfig(DataSource dataSource) { this.dataSource = dataSource; }

//    private EntityManager em;
//
//    public SpringConfig(EntityManager em) {
//        this.em = em;
//    }

    private final MemberRepository memberRepository;

    @Autowired
    public SpringConfig(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Bean
    public MemberService memberService() {
        return new MemberService(memberRepository);
    }

//    @Bean // -> TimeTraceAop 클래스에서 @Component 이용
//    public TimeTraceAop timeTraceAop() {
//        return new TimeTraceAop();
//    }

//    @Bean
//    public MemberRepository memberRepository() {
        // 스프링의 DI (Dependencies Injection)을 사용하면
        // 기존 코드를 전혀 손대지 않고, 설정만으로 구현 클래스를 변경할 수 있다.

//        return new MemoryMemberRepository();
//        return new JdbcMemberRepository(dataSource);
//        return new JdbcTemplateMemberRepository(dataSource);
//        return new JpaMemberRepository(em);
//    }

}
