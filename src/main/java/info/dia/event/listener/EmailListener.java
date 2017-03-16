package info.dia.event.listener;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import info.dia.event.EmailEvent;
import info.dia.persistence.model.User;
import info.dia.service.EmailService;
import info.dia.web.dto.AssignmentDto;

@Component
public class EmailListener implements ApplicationListener<EmailEvent> {

	@Autowired 
	private EmailService emailService;
	
	
	@Override
	public void onApplicationEvent(final EmailEvent event) {
		this.emailSentEvent(event);
	}
	
	private void emailSentEvent(final EmailEvent event){
		final List<User> users = event.getUsers();
		final AssignmentDto assignmentDto = event.getAssignmentDto();
		final User assignmentUser = event.getAssignmentUser();
		
		emailService.sendAssignmentNotification(users, assignmentDto, assignmentUser);
		
	}
	
	
}
