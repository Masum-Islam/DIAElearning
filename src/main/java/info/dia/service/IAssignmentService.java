package info.dia.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import info.dia.persistence.model.User;
import info.dia.web.dto.AssignmentDto;
import info.dia.web.dto.AssignmentInfoDto;

public interface IAssignmentService {
	
	void saveAssignment(AssignmentDto assignmentDto);
	
	List<AssignmentInfoDto> findByUser(User user, Pageable pageable);
	
	List<AssignmentDto> findByTitleLikeIgnoreCase(String title);
	
	List<AssignmentDto> findBySessionLikeIgnoreCase(String session);

}
