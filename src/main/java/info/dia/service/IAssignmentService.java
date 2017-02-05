package info.dia.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import info.dia.persistence.model.Assignment;
import info.dia.persistence.model.User;
import info.dia.web.dto.AssignmentDto;
import info.dia.web.dto.AssignmentInfoDto;
import info.dia.web.dto.SearchDTO;

public interface IAssignmentService {
	
	void saveAssignment(AssignmentDto assignmentDto);
	
	List<AssignmentInfoDto> findByUser(User user, Pageable pageable);
	
	List<AssignmentInfoDto> findByStatus(User user,boolean status,Pageable pageable);
	
	Page<Assignment> searchRequests(User user,SearchDTO searchDTO, Pageable p);
}
