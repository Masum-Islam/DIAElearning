package info.dia.persistence.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import info.dia.persistence.model.Assignment;
import info.dia.persistence.model.AssignmentStudent;

public interface AssignmentStudentRepository extends JpaRepository<AssignmentStudent, Long>,QueryDslPredicateExecutor<AssignmentStudent> {

	AssignmentStudent findByEmailAndAssignment(String email,Assignment assignment);
	
	Page<AssignmentStudent> findAllByAssignment(Assignment assignment,Pageable pageable);
	
	Page<AssignmentStudent> findAllByEmailAndAssignmentStatus(String email,Boolean status,Pageable pageable);
	
	AssignmentStudent findByEmailAndAssignmentId(String email,Long assignmentId);
	
	AssignmentStudent findByIdAndEmail(Long assignmentStudentId,String email);
	
}
