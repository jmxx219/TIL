package hello.hellospring.controller;

import hello.hellospring.domain.Member;
import hello.hellospring.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * 자바 코드로 직접 스프링 빈 등록하기
 * 1. 필드 주입
 * 2. setter 주입
 * 3. 생성자 주입 -> 의존 관계가 실행 중에 동적으로 변하는 경우는 거의 없으므로 생성자 주입 사용 권장
 */

@Controller
public class MemberController {

    private final MemberService memberService;

    //    @Autowired private final MemberService memberService; // 필드 주입

//    @Autowired
//    public void setMemberService(MemberService memberService){ // setter 주입
//        this.memberService = memberService;
//    }

    @Autowired
    public MemberController(MemberService memberService) { // 생성자 주입
        this.memberService = memberService;
    }

    @GetMapping("/members/new")
    public String createForm() {
        return "members/createMemberForm";
    }

    @PostMapping("/members/new")
    public String create(MemberForm form){
        Member member = new Member();
        member.setName(form.getName());

        memberService.join(member);

        return "redirect:/";
    }

    @GetMapping("/members")
    public String list(Model model) {
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);
        return "members/memberList";
    }
}
