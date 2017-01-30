package info.dia.service;

import java.util.List;

import info.dia.persistence.model.User;
import info.dia.web.dto.AssignmentDto;

public interface IAssignmentService {
	
	void saveAssignment(AssignmentDto assignmentDto);
	
	List<AssignmentDto> findByUser(User user);
	
	List<AssignmentDto> findByTitleLikeIgnoreCase(String title);
	
	List<AssignmentDto> findBySessionLikeIgnoreCase(String session);

}
