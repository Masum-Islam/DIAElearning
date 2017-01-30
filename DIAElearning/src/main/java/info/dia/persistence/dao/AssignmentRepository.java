package info.dia.persistence.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import info.dia.persistence.model.Assignment;
import info.dia.persistence.model.User;

public interface AssignmentRepository extends JpaRepository<Assignment, Long>{

	List<Assignment> findByUser(User user);
	List<Assignment> findByTitleLikeIgnoreCase(String title);
	List<Assignment> findBySessionLikeIgnoreCase(String session);

	
}
