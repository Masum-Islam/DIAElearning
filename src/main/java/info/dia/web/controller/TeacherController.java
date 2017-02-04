package info.dia.web.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import info.dia.authentication.IAuthenticationFacade;
import info.dia.persistence.dao.AssignmentRepository;
import info.dia.persistence.model.Assignment;
import info.dia.persistence.model.Group;
import info.dia.persistence.model.GroupDetails;
import info.dia.persistence.model.User;
import info.dia.service.IAssignmentService;
import info.dia.service.IGroupService;
import info.dia.service.IUserService;
import info.dia.web.dto.AssignmentDto;
import info.dia.web.dto.AssignmentInfoDto;
import info.dia.web.dto.GroupDto;
import info.dia.web.util.AssignmentMapper;
import info.dia.web.util.GenericResponse;

@Controller
@RequestMapping(value="/teacher")
public class TeacherController {
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	
	@Autowired
	private IUserService userService;
	
	@Autowired
	private MessageSource messages;
	
	@Autowired
	private IGroupService groupService;
	
	@Autowired
    private IAuthenticationFacade authenticationFacade;
	
	@Autowired
	private IAssignmentService assignmentService;
	
	@Autowired 
	private AssignmentRepository assignmentRepository;
	
	
	private static final int BUTTONS_TO_SHOW = 5;
	private static final int INITIAL_PAGE = 0;
	private static final int INITIAL_PAGE_SIZE = 5;
	private static final int[] PAGE_SIZES = { 5, 10, 20 };

	
	
	
	/* Start Assignment Related Methods*/
	
	/*Start Assignment Page Method*/
	@RequestMapping(value="/assignment",method=RequestMethod.GET)
	public String createAssignment(Model model){
		
		Authentication authentication = authenticationFacade.getAuthentication();
    	if (!(authentication instanceof AnonymousAuthenticationToken)) {
    		
    		User user = userService.findUserByEmail(authentication.getName());
    		
    		List<Group> groups = groupService.findByUser(user);
    		
    		
    		model.addAttribute("assignment",new AssignmentDto());
    		model.addAttribute("emails",groups);
    		
    		
    	}
		return "teacher/assignment";
	}
	/*End Assignment Page Method*/
	
	/*Start Assignment Save/Update Method*/
	@RequestMapping(value="/assignment",params="save",method=RequestMethod.POST)
	@ResponseBody
	public GenericResponse saveAssignment(final Locale locale,@Valid AssignmentDto assignmentDto,@RequestParam Map<String,String> reqPar){
		
 		String submitStartDate = reqPar.get("submitStartDate");
 		String submitEndDate = reqPar.get("submitEndDate");
 		
 		LOGGER.info("Date Value :"+submitStartDate+"--->"+submitEndDate);
 		
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 		
 		
			Date pStartDate = null;
			Date pEndDate = null;
			
			try {
				pStartDate = format.parse(submitStartDate);
				pEndDate = format.parse(submitEndDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			
			Calendar c1 = Calendar.getInstance();
			c1.setTime(pStartDate);
			
			Calendar c2 = Calendar.getInstance();
			c2.setTime(pEndDate);
			
			LOGGER.info("After Parse :"+"Start Date :"+pStartDate+ " End Date :"+pEndDate);
			
			assignmentDto.setSubmitStartDate(c1.getTime());
			assignmentDto.setSubmitEndDate(c2.getTime());
			
		    // user clicked "Save Button"
			LOGGER.info("Save Button Clicked");
			
			//Assignment status--->0(save) and status--->1(save and published)
			
			assignmentService.saveAssignment(assignmentDto);
		
		
		
    	return new GenericResponse(messages.getMessage("message.assignmentSaveSuc", null, locale));
	}
	/*End Assignment Save/Update Method*/
	
	
	
	/*Start Assignment Save/Update with published Method*/
	@RequestMapping(value="/assignment",params="published",method=RequestMethod.POST)
	@ResponseBody
	public GenericResponse saveAndPublishedAssignment(final Locale locale,@Valid AssignmentDto assignmentDto,@RequestParam Map<String,String> reqPar){
	
		    // user clicked "Save And Published Button"
			LOGGER.info("Save And Published Button Clicked :");
			
			//Assignment status--->0(save) and status--->1(save and published)
		
    	return new GenericResponse(messages.getMessage("message.assignmentPublishedSuc", null, locale));
	}
	/*End Assignment Save/Update Method*/
	
	
	
	@RequestMapping(value="/assignments", method=RequestMethod.GET)
	public ModelAndView allAssignmentInformation(@RequestParam(value = "pageSize", required = false) Integer pageSize,
			 									 @RequestParam(value = "page", required = false) Integer page){
		
				ModelAndView modelAndView = new ModelAndView("/teacher/assignmentList");
		
				// Evaluate page size. If requested parameter is null, return initial
				// page size
				int evalPageSize = pageSize == null ? INITIAL_PAGE_SIZE : pageSize;
				
				// Evaluate page. If requested parameter is null or less than 0 (to
				// prevent exception), return initial size. Otherwise, return value of
				// param. decreased by 1.
				int evalPage = (page == null || page < 1) ? INITIAL_PAGE : page - 1;
				
				Authentication authentication = authenticationFacade.getAuthentication();
		    	if (!(authentication instanceof AnonymousAuthenticationToken)) {
		    		
		    		User user = userService.findUserByEmail(authentication.getName());
		    		
		    		Page<Assignment> assignments = assignmentRepository.findByUser(user,new PageRequest(evalPage, evalPageSize,Sort.Direction.DESC,"submitStartDate","submitEndDate"));
		    		List<AssignmentInfoDto> assignmentInfoDtos = AssignmentMapper.map(assignments);
		    		
		    		info.dia.web.util.Pager pager = new info.dia.web.util.Pager(assignments.getTotalPages(),assignments.getNumber(),BUTTONS_TO_SHOW);
		    		
		    		modelAndView.addObject("assignmentInfoDtos", assignmentInfoDtos);
		    		modelAndView.addObject("assignments", assignments);
		    		modelAndView.addObject("selectedPageSize", evalPageSize);
		    		modelAndView.addObject("pageSizes", PAGE_SIZES);
		    		modelAndView.addObject("pager", pager);
		    		
		    		
		     }
		return modelAndView;
	}

	/**
	 * Handles all requests
	 * 
	 * @param pageSize
	 * @param page
	 * @return model and view
	 */
	
	@RequestMapping(value="/getAssignmentList", method=RequestMethod.GET)
	public String retriveAssignments(Model model,@RequestParam(value = "pageSize", required = false) Integer pageSize,
												 @RequestParam(value = "page", required = false) Integer page){
		
		// Evaluate page size. If requested parameter is null, return initial
		// page size
		int evalPageSize = pageSize == null ? INITIAL_PAGE_SIZE : pageSize;
		
		// Evaluate page. If requested parameter is null or less than 0 (to
		// prevent exception), return initial size. Otherwise, return value of
		// param. decreased by 1.
		int evalPage = (page == null || page < 1) ? INITIAL_PAGE : page - 1;
		
		Authentication authentication = authenticationFacade.getAuthentication();
    	if (!(authentication instanceof AnonymousAuthenticationToken)) {
    		
    		User user = userService.findUserByEmail(authentication.getName());
    		
    		Page<Assignment> assignments = assignmentRepository.findByUser(user,new PageRequest(evalPage, evalPageSize,Sort.Direction.DESC,"submitStartDate"));
    		List<AssignmentInfoDto> assignmentInfoDtos = AssignmentMapper.map(assignments);
    		
    		info.dia.web.util.Pager pager = new info.dia.web.util.Pager(assignments.getTotalPages(),assignments.getNumber(),BUTTONS_TO_SHOW);
    		
    		model.addAttribute("assignmentInfoDtos", assignmentInfoDtos);
    		model.addAttribute("assignments", assignments);
    		model.addAttribute("selectedPageSize", evalPageSize);
    		model.addAttribute("pageSizes", PAGE_SIZES);
    		model.addAttribute("pager", pager);
    	}

    	return "/teacher/assignments :: assignments";
	}
	
	
	
	/* End Assignment Related Methods*/
	
	
	
	
	
	@RequestMapping(value="/group",method=RequestMethod.GET)
	public String createGroup(Model model){
		
		String roleName = "ROLE_USER";
		
		LOGGER.info("User List size:"+userService.findByRoles(roleName));
		
		model.addAttribute("groupDto",new GroupDto());
		model.addAttribute("groupDetailsForm",userService.findByRoles(roleName));
		
		
		return "teacher/group";
	}
	
	
	 // Save Group User
    @RequestMapping(value = "/group/save", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse saveGroup(final Locale locale, @Valid GroupDto groupDto) {
    	
    	LOGGER.info("Inside Group Method------>"+groupDto.getGroupDetailsTo());
    	/*if (groupDto.getGroupDetailsTo()!=null) {
			
		}
    	for (int v : groupDto.getGroupDetailsTo()) {
    		LOGGER.info("V :"+v);
		}*/
    	
    	groupService.saveGroup(groupDto);
        
        return new GenericResponse(messages.getMessage("message.saveGroupSuc", null, locale));
    }
    
    
    @RequestMapping(value="/groups",method=RequestMethod.GET)
	public String viewGroups(Model model){
		
    	Authentication authentication = authenticationFacade.getAuthentication();
    	if (!(authentication instanceof AnonymousAuthenticationToken)) {
    		
    		User user = userService.findUserByEmail(authentication.getName());
    		
    		List<Group> groups = groupService.findByUser(user);
    		if (groups.size()>0) {
    			model.addAttribute("groups",groups);
			}else {
				model.addAttribute("groups",groups);
			}
    		
    	}		
		
		return "teacher/groupList";
	}
    
    
    @RequestMapping(value="/groups/{groupId}",method=RequestMethod.GET)
   	public String viewGroup(Model model,@PathVariable("groupId") String groupId){
   		
       	long id = Long.parseLong(groupId);
       	
       	Group group = groupService.getByGroupId(id);
       	
       	LOGGER.info("Group :"+group);
       	
       	List<GroupDetails> groupDetails = new ArrayList<>();
       	
       	for (GroupDetails gd : group.getGroupDetails()) {
       		groupDetails.add(gd);
		}
       	
       	LOGGER.info("groupDetails Size :"+groupDetails.size());
       	
       	model.addAttribute("groupUsers",groupDetails);
       	
   		
   		return "teacher/groupUserList :: groupUserList";
   	}
    
    
    @RequestMapping(value="/editGroups/{groupId}",method=RequestMethod.GET)
   	public String editGroup(Model model,@PathVariable("groupId") String groupId){
   		
       	long id = Long.parseLong(groupId);
       	
       	Group group = groupService.getByGroupId(id);
       	String roleName = "ROLE_USER";
       	       	
       	GroupDto groupDto = new GroupDto();
       	
       	groupDto.setId(group.getId());
       	groupDto.setName(group.getName());
       	groupDto.setGroupDetailsForm(userService.findByRoles(roleName));
       	
       	List<User> groupDetailsTo = new ArrayList<>();
       	
       	for (GroupDetails gp : group.getGroupDetails()) {
			User user = userService.findUserByEmail(gp.getEmail());
			groupDetailsTo.add(user);
		}
       	
       	groupDto.setGroupDetailsTo(groupDetailsTo);
		
		
		model.addAttribute("groupDto",groupDto);
		model.addAttribute("groupDetailsForm",userService.findByRoles(roleName));
		model.addAttribute("groupDetailsTo", groupDto.getGroupDetailsTo());
		
       	return "teacher/group";
   	}
    
    
	

}
