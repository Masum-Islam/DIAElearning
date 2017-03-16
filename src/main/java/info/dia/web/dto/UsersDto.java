package info.dia.web.dto;

import java.io.Serializable;
import java.util.List;

import info.dia.persistence.model.Role;

public class UsersDto implements Serializable{

	private static final long serialVersionUID = 5945736079040320801L;
	
	private String emails;
	
	private List<Role> roles;

	public String getEmails() {
		return emails;
	}

	public void setEmails(String emails) {
		this.emails = emails;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
	
	

}
