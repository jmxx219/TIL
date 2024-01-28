package hello.core.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
//@Component("memberService2") // 이름 직접 지정
public class MemberServiceImpl implements MemberService {

    // DIP 위반 - 의존관계가 인터페이스 뿐만 아니라 구현까지 모두 의존 (문제)
//    private final MemberRepository memberRepository = new MemoryMemberRepository();

    private final MemberRepository memberRepository; // 추상화에만 의존

    @Autowired // ac.getBean(MemberRepository.class)
    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public void join(Member member) {
        memberRepository.save(member);
    }

    @Override
    public Member findMember(Long memberId) {
        return memberRepository.findById(memberId);
    }

    //테스트 용도
    public MemberRepository getMemberRepository() {
        return memberRepository;
    }
}
