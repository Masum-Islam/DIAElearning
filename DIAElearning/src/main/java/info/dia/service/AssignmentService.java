package info.dia.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import info.dia.authentication.IAuthenticationFacade;
import info.dia.persistence.dao.AssignmentRepository;
import info.dia.persistence.dao.GroupRepository;
import info.dia.persistence.model.Assignment;
import info.dia.persistence.model.AssignmentStudent;
import info.dia.persistence.model.Group;
import info.dia.persistence.model.GroupDetails;
import info.dia.persistence.model.User;
import info.dia.web.dto.AssignmentDto;

@Service
@Transactional
public class AssignmentService implements IAssignmentService{
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private AssignmentRepository assignmentRepository;
	
	@Autowired
	private IUserService userService;
	
	@Autowired
    private IAuthenticationFacade authenticationFacade;
	
	@Autowired
	private GroupRepository groupRepository;

	@Override
	public void saveAssignment(AssignmentDto assignmentDto) {
		
		Authentication authentication = authenticationFacade.getAuthentication();
		
		
    	if (!(authentication instanceof AnonymousAuthenticationToken)) {
    		
    		User assignmentUser = userService.findUserByEmail(authentication.getName());
    		
    		if (assignmentDto.getId()!=0) {
				
			}else {
				
				Assignment assignment = new Assignment();
				
				
				assignment.setTitle(assignmentDto.getTitle());
				assignment.setSession(assignmentDto.getSession());
				assignment.setSubmitStartDate(assignmentDto.getSubmitStartDate());
				assignment.setSubmitEndDate(assignmentDto.getSubmitEndDate());
				assignment.setInstructions(assignmentDto.getInstructions());
				assignment.setCreateDate(new Date());
				assignment.setUser(assignmentUser);
				
				Set<AssignmentStudent> assignmentEmails = new HashSet<AssignmentStudent>();
				
				//For duplicate email
				HashMap<String,Long> emails = new HashMap<String,Long>();
				
				/*if (assignmentDto.getEmails()!=null) {
					
					for (Group group : assignmentDto.getEmails()) {
						
						if (group.getGroupDetails()!=null) {
							for (GroupDetails groupDetails : group.getGroupDetails()) {
								emails.put(groupDetails.getEmail(),group.getId());
							}
						}
					}
				}*/
				
				if (assignmentDto.getEmails()!=null) {
					for (GroupDetails groupDetails : assignmentDto.getEmails()) {
						LOGGER.info("Before Email :"+groupDetails.getEmail()+"--->Group Id :"+groupDetails.getGroup().getId());
						emails.put(groupDetails.getEmail(),groupDetails.getGroup().getId());
					}
				}
				
				LOGGER.info("Total Email Size:"+emails.size());
				
				if (emails.size()>0) {
					for (Map.Entry<String, Long> entry : emails.entrySet()) {
						
						AssignmentStudent assignmentStudent = new AssignmentStudent();
						
						LOGGER.info("After Email :"+entry.getKey()+"--->Group Id :"+entry.getValue());
						
						Group group = groupRepository.findOne(entry.getValue());
						
						assignmentStudent.setAssignment(assignment);
						assignmentStudent.setGroup(group);
						assignmentStudent.setEmail(entry.getKey());
						assignmentStudent.setSubmitStartDate(assignmentDto.getSubmitStartDate());
						assignmentStudent.setSubmitEndDate(assignmentDto.getSubmitEndDate());
						
						assignmentEmails.add(assignmentStudent);
						
					}
				}
				
				
				assignment.setAssignmentStudents(assignmentEmails);
				
				assignmentRepository.save(Arrays.asList(assignment));
			}
    	}
		
		
	}
	

	@Override
	public List<AssignmentDto> findByUser(User user) {

		List<AssignmentDto> assignmentList = new ArrayList<AssignmentDto>();
		
		Authentication authentication = authenticationFacade.getAuthentication();
		
    	if (!(authentication instanceof AnonymousAuthenticationToken)) {
    		
    		User assignmentUser = userService.findUserByEmail(authentication.getName());
    		
    		List<Assignment> assignments = assignmentRepository.findByUser(assignmentUser);
    		
    		for (Assignment assignment : assignments) {
    			
    			AssignmentDto dto = new AssignmentDto();
    			
    			dto.setId(assignment.getId());
    			dto.setTitle(assignment.getTitle());
    			dto.setSession(assignment.getSession());
    			dto.setInstructions(assignment.getInstructions());
				dto.setSubmitStartDate(assignment.getSubmitStartDate());
				dto.setSubmitEndDate(assignment.getSubmitEndDate());

    			if (assignment.getAssignmentStudents().size()>0) {
					
    				for (AssignmentStudent assignmentStudent : assignment.getAssignmentStudents()) {
						
					}
    				
				}
    			
			}    		
    	}
		
		
		return null;
	}






	@Override
	public List<AssignmentDto> findByTitleLikeIgnoreCase(String title) {

		
		return null;
	}






	@Override
	public List<AssignmentDto> findBySessionLikeIgnoreCase(String session) {

		return null;
	}
	
		
	boolean isObjectInSet(GroupDetails object, Set<GroupDetails> set) {
		
		   boolean result = false;

		   for(GroupDetails o : set) {
		     if(o.getEmail().equalsIgnoreCase(object.getEmail())) {
		       result = true;
		       break;
		     }
		   }

		   return result;
	}

}
