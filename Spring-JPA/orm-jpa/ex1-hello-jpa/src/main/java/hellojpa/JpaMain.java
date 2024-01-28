package hellojpa;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            Member member = new Member();
            member.setUserName("member1");
            member.setHomeAddress(new Address("homeCity", "street1", "1000"));

            member.getFavoriteFoods().add("치킨");
            member.getFavoriteFoods().add("피자");
            member.getFavoriteFoods().add("족발");

//            member.getAddressHistory().add(new Address("old1", "street1", "1000"));
//            member.getAddressHistory().add(new Address("old2", "street1", "1000"));
            member.getAddressHistory().add(new AddressEntity("old1", "street1", "1000"));
            member.getAddressHistory().add(new AddressEntity("old2", "street1", "1000"));


            em.persist(member);

            em.flush();
            em.clear();

            System.out.println("======== SEARCH START ========");
            Member findMember = em.find(Member.class, member.getId());

//            List<Address> addressHistory = findMember.getAddressHistory();
//            for (Address address : addressHistory) {
//                System.out.println("address.city = " + address.getCity());
//            }

            /*
            Member member = new Member();
            member.setUserName("hello");

            em.persist(member);

            em.flush();
            em.clear();
//            Member findMember = em.find(Member.class, member.getId());
            Member findMember = em.getReference(Member.class, member.getId());
            System.out.println("findMember = " + findMember.getClass());
            System.out.println("findMember.id = " + findMember.getId());
            System.out.println("findMember.userName = " + findMember.getUserName());
            */

//            printMember(findMember);
//            printMemberAndTeam(findMember);

            /*
            Movie movie = new Movie();
            movie.setDirector("A");
            movie.setActor("BB");
            movie.setName("엘리멘탈");
            movie.setPrice(10000);

            em.persist(movie);

            em.flush();
            em.clear();

            Movie findMovie = em.find(Movie.class, movie.getId());
            System.out.println("findMovie = " + findMovie);
            */

            /*
            Team team = new Team();
            team.setName("TeamA");
//            team.getMembers().add(member); // 역방향(연관관계의 주인이 아닌 방향)만 연관관계 설정하면 값이 제대로 안들어감
            em.persist(team);


            Member member = new Member();
            member.setUserName("member1");
//            member.setTeam(team);
//            member.setTeamId(team.getId());
//            member.changeTeam(team);
            em.persist(member);

            team.addMember(member);

            em.flush();
            em.clear();
            */

            /*
            Member findMember = em.find(Member.class, member.getId());
            List<Member> members = findMember.getTeam().getMembers();

            Long teamId = findMember.getTeamId();
            Team findTeam = em.find(Team.class, teamId);
            Team findTeam = member.getTeam();
            */

            /*
            // 비영속
            Member member = new Member();
            member.setId(100L);
            member.setName("helloJPA");

            // 영속
            System.out.println("==== BEFORE ====");
            em.persist(member); // 1차 캐시에서 저장
//            em.detach(member); // 영속성 컨텍스트에서 분리
            System.out.println("==== AFTER ====");

//            em.flush(); // 영속성 컨텍스트의 쓰기 지연 SQL 저장소의 쿼리를 DB에 전송(플러시 발생) - INSERT 쿼리가 날아감

            System.out.println("=====================");

            Member findMember = em.find(Member.class, 100L); // 1차 캐시에서 조회
            member.setName("hello!"); // JPA는 값이 변경되면 변경을 감지해서 커밋되는 시점에 update 쿼리문을 자동으로 날림

//            em.detach(member); // 영속 상태의 엔티티를 영속성 컨텍스트에서 분리(준영속 상태)

//            System.out.println("findMember.id " + findMember.getId());
//            System.out.println("findMember.name " + findMember.getName());
            */

            tx.commit(); // 커밋하는 시점에 쿼리가 날아감
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }

        emf.close();
    }

    // 멤버만 출력하는데, 멤머를 조회할 때 팀도 함께 조회되면 낭비
    private static void printMember(Member member) {
        String userName = member.getUserName();
        System.out.println("userName = " + userName);
    }

    // 멤버와 팀을 함께 출력
    private static void printMemberAndTeam(Member member) {
        String userName = member.getUserName();
        System.out.println("userName = " + userName);

        Team team = member.getTeam();
        System.out.println("team = " + team.getName());
    }
}
