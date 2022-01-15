package hello.core.member;

public class MemberServiceImpl implements MemberService {

    // DIP 위반 - 의존관계가 인터페이스 뿐만 아니라 구현까지 모두 의존 (문제)
    private final MemberRepository memberRepository = new MemoryMemberRepository();

    @Override
    public void join(Member member) {
        memberRepository.save(member);
    }

    @Override
    public Member findMember(Long memberId) {
        return memberRepository.findById(memberId);
    }
}
