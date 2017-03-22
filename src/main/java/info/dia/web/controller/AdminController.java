package info.dia.web.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import info.dia.authentication.IAuthenticationFacade;
import info.dia.persistence.dao.UserRepository;
import info.dia.persistence.model.User;
import info.dia.service.IRoleService;
import info.dia.service.IUserService;
import info.dia.web.dto.SearchDTO;
import info.dia.web.dto.UserStatusDto;
import info.dia.web.dto.UsersDto;
import info.dia.web.util.HelperUtils;

@Controller
@PreAuthorize("hasAuthority('ADMIN_PRIVILEGE')")
@RequestMapping(value="/admin")
public class AdminController {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	private static final int BUTTONS_TO_SHOW = 5;
	private static final int INITIAL_PAGE = 0;
	private static final int INITIAL_PAGE_SIZE = 5;
	private static final int[] PAGE_SIZES = { 5, 10, 20 };
	private static final String DEFAULT_SORT_STRING = "id";
	
	
	@Autowired
	private IRoleService roleService;
	
	@Autowired
    private IAuthenticationFacade authenticationFacade;
	
	@Autowired
	private UserRepository repository;
	
	
	@Autowired
	private IUserService userService;
	
	@RequestMapping(value="/allUsers",method=RequestMethod.GET)
	public String getAllUsers(Model model,
			@RequestParam(value = "status", required = false) Boolean status,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "sortString", required = false) String sortString,
			@RequestParam(value = "oldSortString", required = false) String oldSortString,
			@RequestParam(value = "oldDirection", required = false) Direction oldDirection){
		
		PageRequest pageRequest = HelperUtils.createPageRequest(model,page,sortString,oldSortString,oldDirection,INITIAL_PAGE,INITIAL_PAGE_SIZE,DEFAULT_SORT_STRING);
		Page<User> users = userService.getAllUser(pageRequest);
		info.dia.web.util.Pager pager = new info.dia.web.util.Pager(users.getTotalPages(),users.getNumber(),BUTTONS_TO_SHOW);
		
		model.addAttribute("users", users);
		model.addAttribute("pager", pager);
		
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
	
	
	
	
}
