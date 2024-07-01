package study.datajpa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

	/** 메소드 이름으로 쿼리 생성 **/
	List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

	/** JPA NamedQuery**/
	// @Query(name = "Member.findByUsername")
	List<Member> findByUsername(@Param("username") String username);

	/** @Query, 리포지토리 메소드에 쿼리 정의하기 **/
	@Query("select m from Member m where m.username= :username and m.age = :age")
	List<Member> findUser(@Param("username") String username, @Param("age") int age);

	/** @Query, 단순히 값 하나를 조회하기 **/
	@Query("select m.username from Member m")
	List<String> findUsernameList();

	/** @Query, 값, DTO 조회하기 **/
	@Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) " +
		"from Member m join m.team t")
	List<MemberDto> findMemberDto();

	/** **컬렉션 파라미터 바인딩 **/
	@Query("select m from Member m where m.username in :names")
	List<Member> findByNames(@Param("names") List<String> names);

	/** 반환 타입 **/
	List<Member> findListByUsername(String username); //컬렉션
	Member findMemberByUsername(String username); //단건
	Optional<Member> findOptionalByUsername(String username); //단건 Optional

	/** 페이징과 정렬 **/
	@Query(value = "select m from Member m", countQuery = "select count(m) from Member m") // count 쿼리 분리
	Page<Member> findByAge(int age, Pageable pageable); //count 쿼리 사용
	// Slice<Member> findByAge(int age, Pageable pageable); //count 쿼리 사용 안함
	// List<Member> findByAge(int age, Pageable pageable); //count 쿼리 사용 안함
	// List<Member> findByAge(int age, Sort sort);

	/** 벌크성 수정 쿼리** **/
	@Modifying(clearAutomatically = true) // 벌크성 쿼리를 실행 후, 영속성 컨텍스트 초기화
	@Query("update Member m set m.age = m.age + 1 where m.age >= :age")
	int bulkAgePlus(@Param("age") int age);

	/** JPQL 페치 조인 **/
	@Query("select m from Member m left join fetch m.team")
	List<Member> findMemberFetchJoin();

	/** EntityGraph - 공통 메서드 오버라이드 **/
	@Override
	@EntityGraph(attributePaths = {"team"})
	List<Member> findAll();

	/** EntityGraph - JPQL + 엔티티 그래프 **/
	@EntityGraph(attributePaths = {"team"})
	@Query("select m from Member m")
	List<Member> findMemberEntityGraph();

	/** EntityGraph - 메서드 이름으로 쿼리에서 특히 편리하다. **/
	// @EntityGraph(attributePaths = {"team"})
	List<Member> findEntityGraphByUsername(String username);

	/** EntityGraph - 네임드 쿼리(NamedEntityGraph) **/
	@EntityGraph("Member.all")
	@Query("select m from Member m")
	List<Member> findNamedEntityGraphByUsername(String username);

	/** JPA Query Hint **/
	@QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
	Member findReadOnlyByUsername(String username);

	@QueryHints(value = { @QueryHint(name = "org.hibernate.readOnly", value = "true")}, forCounting = true)
	Page<Member> findReadOnlyByUsername(String name, Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	List<Member> findLockByUsername(String name);
}
