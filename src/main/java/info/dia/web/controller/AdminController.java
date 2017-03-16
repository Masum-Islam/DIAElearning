package info.dia.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import info.dia.service.IRoleService;
import info.dia.web.dto.UsersDto;

@Controller
@RequestMapping(value="/admin")
public class AdminController {

	
	@Autowired
	private IRoleService roleService;

	
	@RequestMapping(value="/allUsers",method=RequestMethod.GET)
	public String getAllUsers(){
		
		return "admin/users";
	}
	
	@RequestMapping(value="/createUser",method=RequestMethod.GET)
	public String createUser(Model model){
		
		UsersDto users = new UsersDto();
		users.setRoles(roleService.getAllRoles());
		
		
		model.addAttribute("users",users);
		
		return "admin/user";
	}
	
	
}
