package info.dia.service;

import java.util.List;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import info.dia.persistence.model.User;
import info.dia.web.dto.AssignmentDto;


@Component
public class EmailService {
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	
	private static final String BACKGROUND_IMAGE = "email-templates/images/background.png";
    private static final String LOGO_BACKGROUND_IMAGE = "email-templates/images/logo-background.png";
    private static final String ELEARNING_BANNER_IMAGE = "email-templates/images/eLearning_Banner.png";
    private static final String ELEARNING_LOGO_IMAGE = "email-templates/images/eLearning_Logo.png";
    
    private static final String PNG_MIME = "image/png";
	
	@Autowired
	private JavaMailSender mailSender;
	 
	@Autowired
	private Environment env;
	 
	@Autowired 
	private TemplateEngine templateEngine;
	 
	 
	 public void sendEmail(String to, String subject, String content)
		{
	        try
			{
	        	// Prepare message using a Spring helper
	        	
	        	LOGGER.info("Sending email....."+env.getProperty("support.email"));
	        	
	            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
	            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true /* multipart */, "UTF-8");
	            message.setSubject(subject);
	            message.setFrom(env.getProperty("support.email"));
	            message.setTo(to);
	            message.setText(content, true /* isHtml */);
	            
	            message.addInline("background", new ClassPathResource(BACKGROUND_IMAGE), PNG_MIME);
	            message.addInline("logo-background", new ClassPathResource(LOGO_BACKGROUND_IMAGE), PNG_MIME);
	            message.addInline("eLearning_Banner", new ClassPathResource(ELEARNING_BANNER_IMAGE), PNG_MIME);
	            message.addInline("eLearning_Logo", new ClassPathResource(ELEARNING_LOGO_IMAGE), PNG_MIME);
	            
	            
	            mailSender.send(message.getMimeMessage());
			} 
	        catch (MailException | MessagingException e)
			{
	        	LOGGER.error(e.toString());
			}
		}
	 
	 
	 public void sendAssignmentNotification(final List<User> users, final AssignmentDto assignmentDto,User assignmentUser) 
     {
         MimeMessagePreparator[] preparators = new MimeMessagePreparator[users.size()];
         int i = 0;
         for (final User user: users) 
         {
             preparators[i++] =  new MimeMessagePreparator() 
             {
                 public void prepare(MimeMessage mimeMessage) throws Exception 
                 {
                     final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                     message.setSubject("New assignment has been created:#"+assignmentDto.getTitle());
                     message.setTo(user.getEmail());
                     message.setFrom(env.getProperty("support.email"));
                     
                     
                     final Locale locale = new Locale(Locale.ENGLISH.toString());
                     
                     final Context ctx = new Context(locale);
                     
                     ctx.setVariable("assignmentCreator", assignmentUser.getFirstName()+" "+assignmentUser.getLastName());
                     ctx.setVariable("name", user.getFirstName()+" "+user.getLastName());
                     ctx.setVariable("title", assignmentDto.getTitle());
                     ctx.setVariable("session", assignmentDto.getSession());
                     ctx.setVariable("startDate", assignmentDto.getSubmitStartDate());
                     ctx.setVariable("endDate", assignmentDto.getSubmitEndDate());
                     
                     
                    final String htmlContent = templateEngine.process("assignmentInformationEmail",ctx);
                     
                    message.setText(htmlContent, true /* isHtml */);
                     
                    message.addInline("background", new ClassPathResource(BACKGROUND_IMAGE), PNG_MIME);
     	            message.addInline("logo-background", new ClassPathResource(LOGO_BACKGROUND_IMAGE), PNG_MIME);
     	            message.addInline("eLearning_Banner", new ClassPathResource(ELEARNING_BANNER_IMAGE), PNG_MIME);
     	            message.addInline("eLearning_Logo", new ClassPathResource(ELEARNING_LOGO_IMAGE), PNG_MIME);
     	            
                 }
             };
         }
         mailSender.send(preparators);
     }
}
