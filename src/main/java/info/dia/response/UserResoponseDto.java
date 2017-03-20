package info.dia.response;

import java.io.Serializable;
import java.util.Collection;

import info.dia.persistence.model.Role;

public class UserResoponseDto implements Serializable {
	
	private static final long serialVersionUID = -1520585255658892288L;
	
	private Long id;
	private String email;
	private String firstName;
	private String lastName;
	private Collection<Role> roles;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public Collection<Role> getRoles() {
		return roles;
	}
	public void setRoles(Collection<Role> roles) {
		this.roles = roles;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
