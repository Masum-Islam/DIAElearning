package info.dia.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import info.dia.persistence.model.Assignment;
import info.dia.persistence.model.AssignmentStudent;
import info.dia.web.dto.SearchDTO;

public interface IAssignmentStudentService {
	
	Page<AssignmentStudent> findAllByAssignment(Assignment assignment,Pageable pageable);
	
	Page<AssignmentStudent> searchAssignmentStudent(Assignment assignment,SearchDTO dto,Pageable pageable);

}
