package info.dia.web.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import info.dia.authentication.IAuthenticationFacade;
import info.dia.persistence.model.Assignment;
import info.dia.persistence.model.AssignmentStudent;
import info.dia.persistence.model.Document;
import info.dia.persistence.model.User;
import info.dia.service.IAssignmentService;
import info.dia.service.IAssignmentStudentService;
import info.dia.service.IDocumentService;
import info.dia.service.IUserService;
import info.dia.web.dto.DocumentDto;
import info.dia.web.dto.StudentDocumentDto;
import info.dia.web.util.DocumentMapper;
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
	
	@Autowired
	private IAssignmentService assignmentService;
	
	
	@Autowired
	private IDocumentService uploadService;
	
	
	
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
	
	
		//Student Assignment Document
	@RequestMapping(value="/addDocument",method=RequestMethod.GET)
	public String addDocumentsToAssignment(Model model,@RequestParam(value="assignmentStudentId") Long assignmentStudentId,@RequestParam(value="assignmentId") Long assignmentId){
			
			LOGGER.info("AssignmentId :"+assignmentId+" Assignment Student Id :"+assignmentStudentId);
		
			Authentication authentication = authenticationFacade.getAuthentication();
			StudentDocumentDto studentDocumentDto = null;
			Document document = null;
			if (!(authentication instanceof AnonymousAuthenticationToken)) {
				
				User user = userService.findUserByEmail(authentication.getName());
				
				studentDocumentDto = new StudentDocumentDto();
				studentDocumentDto.setAssignmentId(assignmentId);
				studentDocumentDto.setAssignmentStudentId(assignmentStudentId);
				
				document = uploadService.getDocumentByAssignmentIdAndUserId(assignmentId, user.getId());
				if (document!=null) {
					studentDocumentDto.setId(document.getId());
				}
				
			}
		model.addAttribute("studentDocumentDto",studentDocumentDto);
		model.addAttribute("document",document);
			
		return "/student/addDocument";
	}
	
	
	@RequestMapping(value="/assignmentDocument/{assignmentId}",method=RequestMethod.GET)
	public String getAssignmentDocument(@PathVariable("assignmentId") long assignmentId,Model model){
		
		LOGGER.info("getAssignmentDocument :"+assignmentId);
		
		Authentication authentication = authenticationFacade.getAuthentication();
    	if (!(authentication instanceof AnonymousAuthenticationToken)) {
    		User assignmentUser = userService.findUserByEmail(authentication.getName());
    		Document  document = uploadService.getDocumentByAssignmentIdAndUserId(assignmentId,assignmentUser.getId());
    		LOGGER.info("document :"+document);
    		if(document!=null){
    			model.addAttribute("document", document);
    		}
    	}
    	return "student/studentAssignmentDocument :: studentAssignmentDocument";
	}
	
	
	@RequestMapping(value = "/uploadStudentAssignmentDocument", method = RequestMethod.POST)
	public @ResponseBody List<StudentDocumentDto> upload(MultipartHttpServletRequest request, HttpServletResponse response,
		   @RequestParam(value = "assignmentId") Long assignmentId,@RequestParam(value="assignmentStudentId") Long assignmentStudentId,@RequestParam(value = "id",required=false) Long id) throws IOException {

		Authentication authentication = authenticationFacade.getAuthentication();
		List<StudentDocumentDto> uploadedFiles = null;
		
		LOGGER.info("Student DocumentId :"+id);
		
		if (!(authentication instanceof AnonymousAuthenticationToken)) {

			User user = userService.findUserByEmail(authentication.getName());
			
			// Getting uploaded files from the request object
			Map<String, MultipartFile> fileMap = request.getFileMap();

			// Maintain a list to send back the files info. to the client side
			uploadedFiles = new ArrayList<StudentDocumentDto>();

			// Iterate through the map
			for (MultipartFile multipartFile : fileMap.values()) {

				// Save the file to local disk
				saveFileToLocalDisk(multipartFile, user);

				StudentDocumentDto fileInfo = getUploadedFileInfo(multipartFile,user,assignmentStudentId,assignmentId);

				// Save the file info to database
				Document document = saveFileToDatabase(fileInfo);
				
				
				// adding the file info to the list
				uploadedFiles.add(fileInfo);
			}

		}
		return uploadedFiles;
	}
	
	
	//Document upload related method
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
	
	private Document saveFileToDatabase(StudentDocumentDto uploadedFile) {

	    return uploadService.saveStudentDocument(uploadedFile);

	}
	
	private StudentDocumentDto getUploadedFileInfo(MultipartFile multipartFile,User user,Long assignmentStudentId,Long assignmentId) throws IOException {

		 	StudentDocumentDto fileInfo = new StudentDocumentDto();
		 	
		    fileInfo.setName(multipartFile.getOriginalFilename());
		    fileInfo.setSize(multipartFile.getSize());
		    fileInfo.setType(multipartFile.getContentType());
		    fileInfo.setLocation(getDestinationLocation(user));
		    fileInfo.setUserId(user.getId());
		    fileInfo.setAssignmentId(assignmentId);
		    fileInfo.setAssignmentStudentId(assignmentStudentId);
		    fileInfo.setStatus(2);  // Status = 2, student assignment document
		    
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
