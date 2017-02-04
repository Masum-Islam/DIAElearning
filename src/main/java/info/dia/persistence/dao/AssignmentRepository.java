package info.dia.persistence.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import info.dia.persistence.model.Assignment;
import info.dia.persistence.model.User;

public interface AssignmentRepository extends JpaRepository<Assignment, Long>{

	Page<Assignment> findByUser(User user,Pageable pageable);
	List<Assignment> findByTitleLikeIgnoreCase(String title);
	List<Assignment> findBySessionLikeIgnoreCase(String session);

	
}
