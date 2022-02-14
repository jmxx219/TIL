package hello.core;

import hello.core.member.MemberRepository;
import hello.core.member.MemoryMemberRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
        basePackages = "hello.core.member",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Configuration.class)
)
public class AutoAppConfig {

    // 컴포넌트 스캔에 의해 자동으로 스프링 빈이 등록되어있는 경우, 이름이 같으면 스프링은 오류를 발생 시킴
    // 수동 빈 등록과 자동 빈 등록이 충돌되면 수동 빈 등록이 우선권을 가짐. (수동 빈이 자동 빈을 오버라이딩 함)
    @Bean(name = "memoryMemberRepository")
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }
}
