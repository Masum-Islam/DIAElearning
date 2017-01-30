package info.dia.web.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import info.dia.persistence.model.Group;

public class AssignmentDto implements Serializable{

	private static final long serialVersionUID = -3440398920608049592L;

	private long id;
	
	@NotNull(message="{NotNull.assignmentDto.title}")
    @Size(min = 1,max=30,message="{Size.assignmentDto.title}")
	private String title;
	
	@NotNull(message="{NotNull.assignmentDto.session}")
	private String session;
	
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat ( pattern="yyyy-MM-dd HH:mm")
	private Date submitStartDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat ( pattern="yyyy-MM-dd HH:mm")
	private Date submitEndDate;
	
	
	private String instructions;
	
	private HashSet<Group> groups;
	


	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getSession() {
		return session;
	}


	public void setSession(String session) {
		this.session = session;
	}


	public Date getSubmitStartDate() {
		return submitStartDate;
	}


	public void setSubmitStartDate(Date submitStartDate) {
		this.submitStartDate = submitStartDate;
	}


	public Date getSubmitEndDate() {
		return submitEndDate;
	}


	public void setSubmitEndDate(Date submitEndDate) {
		this.submitEndDate = submitEndDate;
	}


	public String getInstructions() {
		return instructions;
	}


	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}


	public HashSet<Group> getGroups() {
		return groups;
	}


	public void setGroups(HashSet<Group> groups) {
		this.groups = groups;
	}

	
}
