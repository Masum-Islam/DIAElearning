package info.dia.service;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mysema.query.BooleanBuilder;

import info.dia.authentication.IAuthenticationFacade;
import info.dia.persistence.dao.AssignmentRepository;
import info.dia.persistence.dao.GroupRepository;
import info.dia.persistence.model.Assignment;
import info.dia.persistence.model.AssignmentStudent;
import info.dia.persistence.model.Group;
import info.dia.persistence.model.GroupDetails;
import info.dia.persistence.model.QAssignment;
import info.dia.persistence.model.User;
import info.dia.web.dto.AssignmentDto;
import info.dia.web.dto.AssignmentInfoDto;
import info.dia.web.dto.SearchDTO;
import info.dia.web.util.AssignmentMapper;

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
	public List<AssignmentInfoDto> findByUser(User user, Pageable pageable) {

		Authentication authentication = authenticationFacade.getAuthentication();

		List<AssignmentInfoDto> assignmentInfoDtos = null;

    	if (!(authentication instanceof AnonymousAuthenticationToken)) {
    		
    		User assignmentUser = userService.findUserByEmail(authentication.getName());
    		
    		Page<Assignment> assignments = assignmentRepository.findByUser(assignmentUser,pageable);

			assignmentInfoDtos = AssignmentMapper.map(assignments);

    	}

		return assignmentInfoDtos;
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


	@Override
	public List<AssignmentInfoDto> findByStatus(User user, boolean status, Pageable pageable) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Page<Assignment> searchRequests(User user, SearchDTO searchDTO, Pageable p) {
		
		BooleanBuilder b = new BooleanBuilder();
		
		
		
		QAssignment qAssignment = QAssignment.assignment;
		if (searchDTO != null) {
			if (!StringUtils.isEmpty(searchDTO.getSearchString())) {
				b = b.and(qAssignment.id.like(searchDTO.getSearchString())
						.or(qAssignment.title.containsIgnoreCase(searchDTO.getSearchString()))
						.or(qAssignment.session.containsIgnoreCase(searchDTO.getSearchString()))
						.and(qAssignment.user.id.eq(user.getId())));
			}
		}
		
		LOGGER.info("user :"+user.toString()+" Search Find :"+assignmentRepository.findAll(b, p).getTotalElements());
		
		return assignmentRepository.findAll(b, p);
	}
}
