package info.dia.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import info.dia.authentication.IAuthenticationFacade;
import info.dia.persistence.model.AssignmentStudent;
import info.dia.persistence.model.User;
import info.dia.service.IAssignmentStudentService;
import info.dia.service.IUserService;
import info.dia.web.util.HelperUtils;

@Controller
@RequestMapping(value="/student")
public class StudentController {
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	private static final int BUTTONS_TO_SHOW = 5;
	private static final int INITIAL_PAGE = 0;
	private static final int INITIAL_PAGE_SIZE = 5;
	private static final int[] PAGE_SIZES = { 5, 10, 20 };
	private static final String DEFAULT_SORT_STRING = "submitStartDate";
	
	@Autowired
    private IAuthenticationFacade authenticationFacade;
	
	@Autowired
	private IUserService userService;
	
	@Autowired
	private IAssignmentStudentService assignmentStudentService;
	
	@RequestMapping(value="/assignment",method=RequestMethod.GET)
	public String assignment(Model model,@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "sortString", required = false) String sortString,
			@RequestParam(value = "oldSortString", required = false) String oldSortString,
			@RequestParam(value = "oldDirection", required = false) Direction oldDirection){
		
		Authentication authentication = authenticationFacade.getAuthentication();
		if(!(authentication instanceof AnonymousAuthenticationToken)){
			
			User currentUser = userService.findUserByEmail(authentication.getName());
			PageRequest pageRequest = HelperUtils.createPageRequest(model,page,sortString,oldSortString,oldDirection,INITIAL_PAGE,INITIAL_PAGE_SIZE,DEFAULT_SORT_STRING);
			
			Page<AssignmentStudent> studentAssignments = assignmentStudentService.getAllStudentAssignmentByEmailAndAssignmentStatus(currentUser.getEmail(),true,pageRequest);
			
			LOGGER.info("Student Assignment Size:"+studentAssignments.getTotalElements());
			
			info.dia.web.util.Pager pager = new info.dia.web.util.Pager(studentAssignments.getTotalPages(),studentAssignments.getNumber(),BUTTONS_TO_SHOW);
			
			model.addAttribute("studentAssignments", studentAssignments);
			model.addAttribute("pager", pager);
			
		}
		return "student/assignment";
	}

}
