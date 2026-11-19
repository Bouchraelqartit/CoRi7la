package ma.youcode.CoRi7la.repository;

import ma.youcode.CoRi7la.entity.Passager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassagerRepository extends JpaRepository<Passager, Long> {

    List<Passager> findByEmailIgnoreCase(String email);
}