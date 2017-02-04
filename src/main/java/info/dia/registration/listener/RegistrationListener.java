package info.dia.registration.listener;

import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import info.dia.persistence.model.User;
import info.dia.registration.OnRegistrationCompleteEvent;
import info.dia.service.EmailService;
import info.dia.service.IUserService;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {
	
	 private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	 
	 
    @Autowired
    private IUserService service;

    @Autowired
    private MessageSource messages;

    /*@Autowired
    private JavaMailSender mailSender;*/

    @Autowired
    private Environment env;
    
    
    @Autowired 
    protected EmailService emailService;
    
	@Autowired 
	private TemplateEngine templateEngine;
	
   
    

    // API

    public void onApplicationEvent(final OnRegistrationCompleteEvent event) {
        
		
	this.confirmRegistration(event);
		
		
    }

    private void confirmRegistration(final OnRegistrationCompleteEvent event)  {
    	
        final User user = event.getUser();
        final String token = UUID.randomUUID().toString();
        
        service.createVerificationTokenForUser(user, token);
        
        String recipientAddress = user.getEmail();
        final String subject = "Registration Confirmation";
        final String confirmationUrl = event.getAppUrl() + "/registrationConfirm.html?token=" + token;
        /*final String message = messages.getMessage("message.regSucc", null, event.getLocale());*/

        final Context ctx = new Context(event.getLocale());
        
        ctx.setVariable("name", user.getFirstName()+" "+user.getLastName());
        ctx.setVariable("subscriptionDate", new Date());
        ctx.setVariable("link",confirmationUrl);
        
        final String htmlContent = templateEngine.process("registrationTemplete",ctx);

        emailService.sendEmail(recipientAddress,subject,htmlContent);
        
        LOGGER.info("ctx variable :"+ctx.getVariables());
        
        /*final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
        
        message.setFrom("<" + env.getProperty("support.emaile") + ">");
        message.setTo("<" + recipientAddress + ">");
        message.setSubject(subject);
        message.setText(htmlContent,true);*/
       
        /*final SimpleMailMessage email = constructEmailMessage(event, user, token);
        mailSender.send(email);*/
        
        /*mailSender.send(mimeMessage);*/
    }

    //

    /*private final SimpleMailMessage constructEmailMessage(final OnRegistrationCompleteEvent event, final User user, final String token) {
        final String recipientAddress = user.getEmail();
        final String subject = "Registration Confirmation";
        final String confirmationUrl = event.getAppUrl() + "/registrationConfirm.html?token=" + token;
        final String message = messages.getMessage("message.regSucc", null, event.getLocale());
        
        final SimpleMailMessage email = new SimpleMailMessage();
        
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(message + " \r\n" + confirmationUrl);
        email.setFrom(env.getProperty("support.email"));
        
        return email;
    }*/

}
