package info.dia.web.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import info.dia.authentication.IAuthenticationFacade;
import info.dia.persistence.dao.UserRepository;
import info.dia.persistence.model.User;
import info.dia.response.JqgridResponse;
import info.dia.response.UserResoponseDto;
import info.dia.service.IRoleService;
import info.dia.service.IUserService;
import info.dia.web.dto.UserStatusDto;
import info.dia.web.dto.UsersDto;
import info.dia.web.util.JqgridFilter;
import info.dia.web.util.JqgridObjectMapper;
import info.dia.web.util.UserMapper;

@Controller
@RequestMapping(value="/admin")
public class AdminController {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	
	@Autowired
	private IRoleService roleService;
	
	@Autowired
    private IAuthenticationFacade authenticationFacade;
	
	@Autowired
	private UserRepository repository;
	
	
	@Autowired
	private IUserService userService;
	
	@RequestMapping(value="/allUsers",method=RequestMethod.GET)
	public String getAllUsers(){
		
		return "admin/users";
	}
	
	@RequestMapping(value="/createUser",method=RequestMethod.GET)
	public String createUser(Model model){
		
		UsersDto usersDto = new UsersDto();
		usersDto.setRoles(roleService.getAllRoles());
		
		model.addAttribute("usersDto",usersDto);
		
		return "admin/user";
	}
	
	@RequestMapping(value="/createUser",method=RequestMethod.POST)
	public @ResponseBody List<UserStatusDto> createUser(@Valid UsersDto usersDto){
		
		List<UserStatusDto> users = userService.registerNewUserAccounts(usersDto);
		LOGGER.info("Users size :"+users.size());
    	
		return users;
	}
	
	
	@RequestMapping(value="/records", produces="application/json")
	public @ResponseBody JqgridResponse<UserResoponseDto> records(
    		@RequestParam("_search") Boolean search,
    		@RequestParam(value="filters", required=false) String filters,
    		@RequestParam(value="page", required=false) Integer page,
    		@RequestParam(value="rows", required=false) Integer rows,
    		@RequestParam(value="sidx", required=false) String sidx,
    		@RequestParam(value="sord", required=false) String sord) {

		Pageable pageRequest = new PageRequest(page-1, rows);
		
		if (search == true) {
			return getFilteredRecords(filters, pageRequest);
			
		} 
			
		Page<User> users = userService.getAllUser(pageRequest);
		LOGGER.info("users :"+users.getTotalElements());
		List<UserResoponseDto> userDtos = UserMapper.map(users);
		
		JqgridResponse<UserResoponseDto> response = new JqgridResponse<UserResoponseDto>();
		response.setRows(userDtos);
		response.setRecords(Long.valueOf(users.getTotalElements()).toString());
		response.setTotal(Integer.valueOf(users.getTotalPages()).toString());
		response.setPage(Integer.valueOf(users.getNumber()+1).toString());
		
		return response;
	}
	
	/**
	 * Helper method for returning filtered records
	 */
	public JqgridResponse<UserResoponseDto> getFilteredRecords(String filters, Pageable pageRequest) {
		String qUsername = null;
		String qFirstName = null;
		String qLastName = null;
		
		
		JqgridFilter jqgridFilter = JqgridObjectMapper.map(filters);
		for (JqgridFilter.Rule rule: jqgridFilter.getRules()) {
			if (rule.getField().equals("username"))
				qUsername = rule.getData();
			else if (rule.getField().equals("firstName"))
				qFirstName = rule.getData();
			else if (rule.getField().equals("lastName"))
				qLastName = rule.getData();
		}
		
		Page<User> users = null;
		if (qUsername != null) 
			users = repository.findByEmailLike("%"+qUsername+"%", pageRequest);
		if (qFirstName != null && qLastName != null) 
			users = repository.findByFirstNameLikeAndLastNameLike("%"+qFirstName+"%", "%"+qLastName+"%", pageRequest);
		if (qFirstName != null) 
			users = repository.findByFirstNameLike("%"+qFirstName+"%", pageRequest);
		if (qLastName != null) 
			users = repository.findByLastNameLike("%"+qLastName+"%", pageRequest);
		
		
		List<UserResoponseDto> userDtos = UserMapper.map(users);
		JqgridResponse<UserResoponseDto> response = new JqgridResponse<UserResoponseDto>();
		response.setRows(userDtos);
		response.setRecords(Long.valueOf(users.getTotalElements()).toString());
		response.setTotal(Integer.valueOf(users.getTotalPages()).toString());
		response.setPage(Integer.valueOf(users.getNumber()+1).toString());
		return response;
	}
	
	@RequestMapping(value="/get", produces="application/json")
	public @ResponseBody UserResoponseDto get(@RequestBody UserResoponseDto user) {
		return UserMapper.map(repository.findByEmail(user.getEmail()));
	}
	
}
