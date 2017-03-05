package info.dia.web.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import info.dia.authentication.IAuthenticationFacade;
import info.dia.persistence.dao.AssignmentRepository;
import info.dia.persistence.dao.AssignmentStudentRepository;
import info.dia.persistence.model.Assignment;
import info.dia.persistence.model.AssignmentStudent;
import info.dia.persistence.model.Document;
import info.dia.persistence.model.Group;
import info.dia.persistence.model.GroupDetails;
import info.dia.persistence.model.User;
import info.dia.service.DocumentService;
import info.dia.service.IAssignmentService;
import info.dia.service.IAssignmentStudentService;
import info.dia.service.IDocumentService;
import info.dia.service.IGroupDetailsService;
import info.dia.service.IGroupService;
import info.dia.service.IUserService;
import info.dia.web.dto.AssignmentDto;
import info.dia.web.dto.AssignmentInfoDto;
import info.dia.web.dto.AssignmentStudentInfo;
import info.dia.web.dto.AssignmentStudentListDto;
import info.dia.web.dto.DocumentDto;
import info.dia.web.dto.EmailsDto;
import info.dia.web.dto.GroupDto;
import info.dia.web.dto.SearchDTO;
import info.dia.web.util.AssignmentMapper;
import info.dia.web.util.AssignmentStudentMapper;
import info.dia.web.util.DocumentMapper;
import info.dia.web.util.GenericResponse;
import info.dia.web.util.HelperUtils;

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
	private IGroupDetailsService groupDetailsService;
		
	@Autowired
    private IAuthenticationFacade authenticationFacade;
	
	@Autowired
	private IAssignmentService assignmentService;
	
	@Autowired 
	private AssignmentRepository assignmentRepository;
	
	
	@Autowired
	private IAssignmentStudentService assignmentStudentService;
	
	
	@Autowired
	private AssignmentStudentRepository assignmentStudentRepository; 
	
	
	@Autowired
	private IDocumentService uploadService;
	
	
	private static final int BUTTONS_TO_SHOW = 5;
	private static final int INITIAL_PAGE = 0;
	private static final int INITIAL_PAGE_SIZE = 5;
	private static final int[] PAGE_SIZES = { 5, 10, 20 };
	private static final String DEFAULT_SORT_STRING = "submitStartDate";
	
	
	
	/* Start Assignment Related Methods*/
	
	/*Start Assignment Page Method*/
	@RequestMapping(value="/assignment",method=RequestMethod.GET)
	public String createAssignment(Model model){
		
		Authentication authentication = authenticationFacade.getAuthentication();
    	if (!(authentication instanceof AnonymousAuthenticationToken)) {
    		String roleName = "ROLE_USER";
    		model.addAttribute("assignmentDto",new AssignmentDto());
    		model.addAttribute("emailsFrom",userService.findByRoles(roleName));
    	}
		return "teacher/assignment";
	}
	/*End Assignment Page Method*/
	
	
	
	/*Start Assignment Save/Update Method*/
	@RequestMapping(value="/assignment",method=RequestMethod.POST)
	@ResponseBody
	public GenericResponse saveAssignment(final Locale locale,@Valid AssignmentDto assignmentDto,@RequestParam Map<String,String> reqPar){
		
 		String submitStartDate = reqPar.get("submitStartDate");
 		String submitEndDate = reqPar.get("submitEndDate");
 		
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
			
			assignmentDto.setSubmitStartDate(c1.getTime());
			assignmentDto.setSubmitEndDate(c2.getTime());
			
			assignmentService.saveAssignment(assignmentDto);
		
		
    	return new GenericResponse(messages.getMessage("message.assignmentSaveSuc", null, locale));
	}
	/*End Assignment Save/Update Method*/
	
	
	@RequestMapping(value="/assignments", method=RequestMethod.GET)
	public String allAssignmentInformationByUser(Model model,@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "sortString", required = false) String sortString,
			@RequestParam(value = "oldSortString", required = false) String oldSortString,
			@RequestParam(value = "oldDirection", required = false) Direction oldDirection){

				Authentication authentication = authenticationFacade.getAuthentication();
		    	if (!(authentication instanceof AnonymousAuthenticationToken)) {
		    		
		    		User user = userService.findUserByEmail(authentication.getName());
		    		
		    		PageRequest pageRequest = HelperUtils.createPageRequest(model,page,sortString,oldSortString,oldDirection,INITIAL_PAGE,INITIAL_PAGE_SIZE,DEFAULT_SORT_STRING);
		    		
		    		Page<Assignment> assignments = assignmentRepository.findByUser(user,pageRequest);
		    		
		    		/*LOGGER.info("User email 1:"+user.getEmail()+" Search Find :"+assignments.getTotalElements());*/
		    		
		    		List<AssignmentInfoDto> assignmentInfoDtos = AssignmentMapper.map(assignments);
		    		
		    		info.dia.web.util.Pager pager = new info.dia.web.util.Pager(assignments.getTotalPages(),assignments.getNumber(),BUTTONS_TO_SHOW);
		    		
		    		model.addAttribute("assignmentInfoDtos", assignmentInfoDtos);
		    		model.addAttribute("assignments", assignments);
		    		model.addAttribute("pager", pager);
		    		model.addAttribute("searchDTO", new SearchDTO());
		    		
		     }
		return "/teacher/assignmentList";
	}
	
	//Assignment Document
	@RequestMapping(value="/addDocument/{assignmentId}",method=RequestMethod.GET)
	public String addDocumentsToAssignment(Model model,@PathVariable Long assignmentId){
		
		DocumentDto documentDto = new DocumentDto();
		documentDto.setAssignmentId(assignmentId);
		
		
		List<DocumentDto> assignmentsDocuments = new ArrayList<>();
		Authentication authentication = authenticationFacade.getAuthentication();
    	if (!(authentication instanceof AnonymousAuthenticationToken)) {
    		User assignmentUser = userService.findUserByEmail(authentication.getName());
    		Assignment assignment = assignmentService.getAssignmentByIdAndUser(assignmentId, assignmentUser.getId());
    		if(assignment!=null){
    			List<Document> documents = uploadService.getAllDocumentsByAssignmenmt(assignment);
    			if (documents.size()>0) {
    				assignmentsDocuments = DocumentMapper.map(documents);
    				/*LOGGER.info("assignmentsDocuments size :"+assignmentsDocuments.size());*/
				}
    		}
    	}
    	
		model.addAttribute("assignmentDocument", documentDto);
		model.addAttribute("assignmentsDocuments", assignmentsDocuments);
		
		return "/teacher/addDocument";
	}
	
	
	@RequestMapping(value = "/uploadAssignmentDocument", method = RequestMethod.POST)
	  public @ResponseBody List<DocumentDto> upload(MultipartHttpServletRequest request,HttpServletResponse response,@RequestParam(value="assignmentId") Long assignmentId) throws IOException {

		Authentication authentication = authenticationFacade.getAuthentication();
		List<DocumentDto> uploadedFiles = null;
    	if (!(authentication instanceof AnonymousAuthenticationToken)) {
    		
    		User user = userService.findUserByEmail(authentication.getName());
    		
    		// Getting uploaded files from the request object
    	    Map<String, MultipartFile> fileMap = request.getFileMap();

    	    // Maintain a list to send back the files info. to the client side
    	    uploadedFiles = new ArrayList<DocumentDto>();

    	    // Iterate through the map
    	    for (MultipartFile multipartFile : fileMap.values()) {

    	      // Save the file to local disk
    	      saveFileToLocalDisk(multipartFile,user);

    	      LOGGER.info("Assignment Id :"+assignmentId);
    	      
    	      DocumentDto fileInfo = getUploadedFileInfo(multipartFile,user,assignmentId);
    	      
    	      // Save the file info to database
    	      Document document = saveFileToDatabase(fileInfo);

    	      // adding the file info to the list
    	      uploadedFiles.add(fileInfo);
    	    }
    	    
    	}
	    return uploadedFiles;
	  }
	
	
	@RequestMapping(value = "/assignments/search",method=RequestMethod.GET)
	public String search(
			Model model,
			HttpSession session,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "sortString", required = false) String sortString,
			@RequestParam(value = "oldSortString", required = false) String oldSortString,
			@RequestParam(value = "oldDirection", required = false) Direction oldDirection,
			@ModelAttribute("searchDTO") SearchDTO searchDTO) {
		
		
		/*LOGGER.info("Search Method Called!"+" with search string :"+searchDTO.getSearchString()+" and boolean value:"+searchDTO.getAssignmentStatus());*/
		
		SearchDTO sessionSearchDTO = (SearchDTO) session.getAttribute("searchDTO");
		
		if (sessionSearchDTO != null && page != null) {
			searchDTO = sessionSearchDTO;
		}
		
		PageRequest pageRequest = HelperUtils.createPageRequest(model, page,sortString, oldSortString, oldDirection,INITIAL_PAGE,INITIAL_PAGE_SIZE,DEFAULT_SORT_STRING);
		
		
		
		return searchAssignment(model, session, searchDTO, pageRequest);
	}
	
	
	public String searchAssignment(Model model, HttpSession session, SearchDTO searchDTO,PageRequest pageRequest){
		
		Authentication authentication = authenticationFacade.getAuthentication();
		
    	if (!(authentication instanceof AnonymousAuthenticationToken)) {
    		
    		User user = userService.findUserByEmail(authentication.getName());

    		Page<Assignment> assignments = assignmentService.searchRequests(user,searchDTO,pageRequest);
    		
    		List<AssignmentInfoDto> assignmentInfoDtos = AssignmentMapper.map(assignments);
    		info.dia.web.util.Pager pager = new info.dia.web.util.Pager(assignments.getTotalPages(),assignments.getNumber(),BUTTONS_TO_SHOW);
    		
    		model.addAttribute("assignmentInfoDtos", assignmentInfoDtos);
    		model.addAttribute("assignments", assignments);
    		model.addAttribute("pager", pager);
    		model.addAttribute("searchDTO", searchDTO != null ? searchDTO: new Assignment());
    		model.addAttribute("isSearch", "true");
    		session.setAttribute("searchDTO", searchDTO);
    		
    		/*LOGGER.info("Search String:"+searchDTO.getSearchString()+" and boolean value: "+searchDTO.getAssignmentStatus());*/
    		
    	}
    	
    	return "/teacher/assignmentList";
	}
	
	@RequestMapping(value="/assignment/{id}",method=RequestMethod.GET)
	public String editAssignment(Model model,@PathVariable("id") long id){
		
		Authentication authentication = authenticationFacade.getAuthentication();
		String viewPage = "";
		
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			
    		User currentUser = userService.findUserByEmail(authentication.getName());
    		
    		Assignment assignment = assignmentService.getAssignmentByIdAndUser(id, currentUser.getId());
    		
    		LOGGER.info("Assignment Edit User:"+currentUser.getEmail()+"--->Assignment Id"+id+"---->Assignment :"+assignment);
    		
    		if (assignment!=null) {
    			
    			AssignmentDto assignmentDto = new AssignmentDto();
				
    			assignmentDto.setId(assignment.getId());
    			assignmentDto.setTitle(assignment.getTitle());
    			assignmentDto.setSession(assignment.getSession());
    			assignment.setCreateDate(assignment.getCreateDate());
    			assignmentDto.setSubmitStartDate(assignment.getSubmitStartDate());
    			assignmentDto.setSubmitEndDate(assignment.getSubmitEndDate());
    			assignmentDto.setInstructions(assignment.getInstructions());
    			assignmentDto.setStatus(assignment.getStatus());
    			StringJoiner joiner = new StringJoiner(",");
    			
    			for (AssignmentStudent assignmentStudent : assignment.getAssignmentStudents()) {
    				joiner.add(assignmentStudent.getEmail());
				}
    			assignmentDto.setEmailsTo(joiner.toString());
				
				model.addAttribute("assignmentDto", assignmentDto);
				
				viewPage = "teacher/assignment";
			}else{
				
				viewPage = "error/404";
				
			}
    		
		}
		
		return viewPage;
	}
	
	@RequestMapping(value="/viewAssignment/")
	public String viewAssignment(Model model,
			@RequestParam(value = "assignmentId", required = true) Long assignmentId,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "sortString", required = false) String sortString,
			@RequestParam(value = "oldSortString", required = false) String oldSortString,
			@RequestParam(value = "oldDirection", required = false) Direction oldDirection){
		
		Authentication authentication = authenticationFacade.getAuthentication();
		
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			
    		User currentUser = userService.findUserByEmail(authentication.getName());
    		
    		/*LOGGER.info("Assignment Id:"+assignmentId);*/
    		
    		Assignment assignment = assignmentService.getAssignmentByIdAndUser(assignmentId,currentUser.getId());
    		
    		PageRequest pageRequest = HelperUtils.createPageRequest(model,page,sortString,oldSortString,oldDirection,INITIAL_PAGE,INITIAL_PAGE_SIZE,DEFAULT_SORT_STRING);
    		
    		Page<AssignmentStudent> assignmentStudents = assignmentStudentService.findAllByAssignment(assignment,pageRequest);
    		List<AssignmentStudentInfo> assignmentStudentInfos = AssignmentStudentMapper.map(assignmentStudents);
    		
    		info.dia.web.util.Pager pager = new info.dia.web.util.Pager(assignmentStudents.getTotalPages(),assignmentStudents.getNumber(),BUTTONS_TO_SHOW);
    		
    		int totalAssignmentSubmittedStudents = assignmentSubmittedCount(assignmentStudentInfos);
    		
    		model.addAttribute("assignment", assignment);
    		model.addAttribute("assignmentStudents", assignmentStudents);
    		model.addAttribute("totalAssignmentStudents", assignmentStudents.getTotalElements());
    		model.addAttribute("totalAssignmentSubmittedStudents",totalAssignmentSubmittedStudents);
    		model.addAttribute("assignmentStudentInfos", assignmentStudentInfos);
    		model.addAttribute("pager", pager);
    		model.addAttribute("searchDTO", new SearchDTO());
    		
		}
		
		
		return "/teacher/viewAssignment";
	}
	
	
	@RequestMapping(value = "/assignmentStudents/search/{assignmentId}",method=RequestMethod.GET)
	public String searchAssignmentStudent(
			Model model,
			HttpSession session,
			/*@RequestParam(value = "assignmentId", required = true) Long assignmentId,*/
			@PathVariable("assignmentId") long assignmentId,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "sortString", required = false) String sortString,
			@RequestParam(value = "oldSortString", required = false) String oldSortString,
			@RequestParam(value = "oldDirection", required = false) Direction oldDirection,
			@ModelAttribute("searchDTO") SearchDTO searchDTO) {
		
		
		LOGGER.info("Assignment Id :"+assignmentId);
		
		/*LOGGER.info("Search Method Called!"+" with search string :"+searchDTO.getSearchString()+" and boolean value:"+searchDTO.getAssignmentStatus());*/
		
		SearchDTO sessionSearchDTO = (SearchDTO) session.getAttribute("searchDTO");
		
		if (sessionSearchDTO != null && page != null) {
			searchDTO = sessionSearchDTO;
		}
		
		PageRequest pageRequest = HelperUtils.createPageRequest(model, page,sortString, oldSortString, oldDirection,INITIAL_PAGE,INITIAL_PAGE_SIZE,DEFAULT_SORT_STRING);
		
		
		
		return searchAssignmentStudent(model, session,assignmentId,searchDTO, pageRequest);
	}
	
	
	public String searchAssignmentStudent(Model model, HttpSession session,Long assignmentId,SearchDTO searchDTO,PageRequest pageRequest){
		
		Authentication authentication = authenticationFacade.getAuthentication();
		
    	if (!(authentication instanceof AnonymousAuthenticationToken)) {
    		
    		User user = userService.findUserByEmail(authentication.getName());
    		
    		Assignment assignment = assignmentService.getAssignmentByIdAndUser(assignmentId,user.getId());
    		
    		Page<AssignmentStudent> assignmentStudents = assignmentStudentService.searchAssignmentStudent(assignment,searchDTO,pageRequest);
    		
    		List<AssignmentStudentInfo> assignmentStudentInfos = AssignmentStudentMapper.map(assignmentStudents);
    		info.dia.web.util.Pager pager = new info.dia.web.util.Pager(assignmentStudents.getTotalPages(),assignmentStudents.getNumber(),BUTTONS_TO_SHOW);
    		
    		int totalAssignmentSubmittedStudents = assignmentSubmittedCount(assignmentStudentInfos);
    		
    		
    		model.addAttribute("assignment", assignment);
    		model.addAttribute("assignmentStudents", assignmentStudents);
    		model.addAttribute("totalAssignmentStudents", assignmentStudents.getTotalElements());
    		model.addAttribute("totalAssignmentSubmittedStudents",totalAssignmentSubmittedStudents);
    		model.addAttribute("assignmentStudentInfos", assignmentStudentInfos);
    		model.addAttribute("pager", pager);
    		model.addAttribute("searchDTO", searchDTO != null ? searchDTO: new AssignmentStudent());
    		model.addAttribute("isSearch", "true");
    		session.setAttribute("searchDTO", searchDTO);
    		
    		/*LOGGER.info("Assignment student search string:"+searchDTO.getSearchString()+" and boolean value: "+searchDTO.getAssignmentStatus());*/
    		
    	}
    	
    	return "/teacher/viewAssignment";
	}
		
	@RequestMapping(value="/editAssignmentStudent/{assignmentStudentIds}",method=RequestMethod.GET)
	public String editAssignmentStudent(Model model,@RequestParam(value="assignmentId") Long assignmentId,@PathVariable String[] assignmentStudentIds){
		
		Authentication authentication = authenticationFacade.getAuthentication();
		
    	if (!(authentication instanceof AnonymousAuthenticationToken)) {
    		
    		User currentUser = userService.findUserByEmail(authentication.getName());
    		
    		Assignment assignment = assignmentService.getAssignmentById(assignmentId);
    		
    		AssignmentStudentListDto assignmentStudentListDto = new AssignmentStudentListDto();
    		assignmentStudentListDto.setAssignmentId(assignment.getId());
    		
    		
    		List<AssignmentStudentInfo> assignmentStudentInfos = new ArrayList<>();
    		
    		if (assignmentStudentIds!=null) {
    			for (String string : assignmentStudentIds) {
    				if (!StringUtils.isEmpty(string)) {
    					AssignmentStudent assignmentStudent = assignmentStudentService.findByAssignmentStudentId(Long.parseLong(string));
    					AssignmentStudentInfo assignmentStudentInfo = new AssignmentStudentInfo();
    					boolean result = isAssignmentStudentExistsUserAssignment(assignmentStudent, currentUser);
    					if (result) {
    						assignmentStudentInfo.setAssignmentId(assignment.getId());
    						assignmentStudentInfo.setAssignmentStudentId(assignmentStudent.getId());
    						assignmentStudentInfo.setEmail(assignmentStudent.getEmail());
    						assignmentStudentInfo.setStatus(assignmentStudent.isStatus());
    						assignmentStudentInfo.setSubmitedDate(assignmentStudent.getSubmitDate());
    						assignmentStudentInfo.setLastDateOfSubmission(assignmentStudent.getSubmitEndDate());
						}
    					assignmentStudentInfos.add(assignmentStudentInfo);
    				}
    			}
    			assignmentStudentListDto.setAssignmentStudents(assignmentStudentInfos);
    		}
    		model.addAttribute("assignment", assignment);
    		model.addAttribute("assignmentStudentListDto", assignmentStudentListDto);
    	}
    	
    	
		return "/teacher/editAssignmentStudent";
	}
	
	
	@RequestMapping(value="/updateAssignmentStudentDate",method=RequestMethod.POST)
	@ResponseBody
	public GenericResponse updateAssignmentStudentDate(final Locale locale,@ModelAttribute AssignmentStudentListDto dto){
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		for (AssignmentStudentInfo assignmentStudentInfo : dto.getAssignmentStudents()) {
			
			AssignmentStudent assignmentStudent = assignmentStudentService.findByAssignmentStudentId(assignmentStudentInfo.getAssignmentStudentId());
			if (assignmentStudent!=null) {
				try {
					if (dto.getSubmitEndDate()!=null && !StringUtils.isEmpty(dto.getSubmitEndDate())) {
						assignmentStudent.setSubmitEndDate(dateFormat.parse(dto.getSubmitEndDate()));
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			
			assignmentStudentRepository.saveAndFlush(assignmentStudent);
			
		}
		
		
    	return new GenericResponse(messages.getMessage("message.assignmentStudentLastDateOfSubmissionUpdateSuc", null, locale));
	}

	
	
	@RequestMapping(value="/assignmentEmail.json/{query}",method=RequestMethod.GET)
	@ResponseBody
	public List<EmailsDto> getAssignmentEmails(@PathVariable("query") String query){
		
		List<EmailsDto> emails = new ArrayList<>();
		LOGGER.info("query String :"+query);
		
		Iterable<User> iterable = userService.searchEmail(query);
		List<User> users = new ArrayList<>();
		iterable.iterator().forEachRemaining(users::add);
		
		for (User user : users) {
			EmailsDto dto = new EmailsDto();
			dto.setEmail(user.getEmail());
			dto.setName(user.getFirstName()+" "+user.getLastName());
			emails.add(dto);
		}
		
		return emails;
	}
	
	
	
	@RequestMapping(value="/assignmentDocuments/{assignmentId}",method=RequestMethod.GET)
	public String getAssignmentDocuments(@PathVariable("assignmentId") long assignmentId,Model model){
		LOGGER.info("Inside Metod :"+assignmentId);
		List<DocumentDto> assignmentsDocuments = new ArrayList<>();
		Authentication authentication = authenticationFacade.getAuthentication();
    	if (!(authentication instanceof AnonymousAuthenticationToken)) {
    		User assignmentUser = userService.findUserByEmail(authentication.getName());
    		Assignment assignment = assignmentService.getAssignmentByIdAndUser(assignmentId, assignmentUser.getId());
    		if(assignment!=null){
    			List<Document> documents = uploadService.getAllDocumentsByAssignmenmt(assignment);
    			if (documents.size()>0) {
    				assignmentsDocuments = DocumentMapper.map(documents);
    				/*LOGGER.info("assignmentsDocuments size :"+assignmentsDocuments.size());*/
    				model.addAttribute("assignmentsDocuments", assignmentsDocuments);
				}
    		}
    	}
    	return "teacher/assignmentDocuments :: assignmentDocuments";
	}
	
	//Assignment Document Download
	@PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @RequestMapping(value = "/downloadDocument/{documentId}/{assignmentId}", method = RequestMethod.GET)
    public void getFile(HttpServletResponse response,@PathVariable Long documentId,@PathVariable Long assignmentId) {

		Authentication authentication = authenticationFacade.getAuthentication();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			
			User assignmentUser = userService.findUserByEmail(authentication.getName());

			Document dataFile = uploadService.getDocumentByIdAndAssignmentIdAndUser(documentId, assignmentId, assignmentUser.getId());
			if (dataFile!=null) {
				LOGGER.info("dataFile :"+dataFile.getLocation()+"--->"+dataFile.getName());
				File file = new File(dataFile.getLocation(), dataFile.getName());
				try {
					response.setContentType(dataFile.getType());
					response.setHeader("Content-disposition", "attachment; filename=\"" + dataFile.getName() + "\"");
					FileCopyUtils.copy(FileUtils.readFileToByteArray(file), response.getOutputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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
    
    
    public int assignmentSubmittedCount(List<AssignmentStudentInfo> assignmentStudents){
    	
    	int count=0;
    	
    	for (AssignmentStudentInfo assignmentStudentInfo : assignmentStudents) {
			if (assignmentStudentInfo.getStatus()==true) {
				count +=count;
			}
		}
    	 return count;
    }
    
   public boolean isAssignmentStudentExistsUserAssignment(AssignmentStudent assignmentStudent,User user){
	   
	   boolean flag = false;
	   
	   Assignment assignment = assignmentService.getAssignmentByIdAndUser(assignmentStudent.getAssignment().getId(), user.getId());
	   if (assignment!=null) {
		   flag = true;
	   }
	   
	   return flag;
   }
	
	private void saveFileToLocalDisk(MultipartFile multipartFile,User user) throws IOException, FileNotFoundException {

		String outputFileName = getOutputFilename(multipartFile,user);

		// Get the filename and build the local file path
		String filename = multipartFile.getOriginalFilename();
		String filepath = Paths.get(outputFileName, filename).toString();

		// Save the file locally
		BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(filepath)));
		stream.write(multipartFile.getBytes());
		stream.close();
	}

	private String getOutputFilename(MultipartFile multipartFile,User user) {

		return getDestinationLocation(user);
	}
	
	private Document saveFileToDatabase(DocumentDto uploadedFile) {

	    return uploadService.saveOrUpdate(uploadedFile);

	}
	
	private DocumentDto getUploadedFileInfo(MultipartFile multipartFile,User user,Long assignmentId) throws IOException {

		 	DocumentDto fileInfo = new DocumentDto();
		 	
		    fileInfo.setName(multipartFile.getOriginalFilename());
		    fileInfo.setSize(multipartFile.getSize());
		    fileInfo.setType(multipartFile.getContentType());
		    fileInfo.setLocation(getDestinationLocation(user));
		    fileInfo.setUserId(user.getId());
		    fileInfo.setAssignmentId(assignmentId);
		    
		    return fileInfo;
	}
	 
	
	private String getDestinationLocation(User user) {

		String parrentDirectory = "D:/file_to_save/";
		boolean flag = false;

		File file = new File(parrentDirectory + user.getEmail());
		if (!file.exists()) {
			flag = file.mkdirs();
		}

		return file.getAbsolutePath();
	}
   

}
