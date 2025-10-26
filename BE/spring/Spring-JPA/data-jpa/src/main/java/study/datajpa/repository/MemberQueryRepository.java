package study.datajpa.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import study.datajpa.entity.Member;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

	private final EntityManager em;

	List<Member> findAllMembers() {
		return em.createQuery("select m from Member m", Member.class)
				.getResultList();
	}
}
