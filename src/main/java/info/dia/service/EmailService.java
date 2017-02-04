package info.dia.service;

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
import org.springframework.stereotype.Component;


@Component
public class EmailService {
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	
	private static final String BACKGROUND_IMAGE = "email-templates/images/background.png";
    private static final String LOGO_BACKGROUND_IMAGE = "email-templates/images/logo-background.png";
    private static final String THYMELEAF_BANNER_IMAGE = "email-templates/images/thymeleaf-banner.png";
    private static final String THYMELEAF_LOGO_IMAGE = "email-templates/images/thymeleaf-logo.png";
    
    private static final String PNG_MIME = "image/png";
	
	 @Autowired
	 private JavaMailSender mailSender;
	 
	 @Autowired
	 private Environment env;
	 
	 
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
	            message.addInline("thymeleaf-banner", new ClassPathResource(THYMELEAF_BANNER_IMAGE), PNG_MIME);
	            message.addInline("thymeleaf-logo", new ClassPathResource(THYMELEAF_LOGO_IMAGE), PNG_MIME);
	            
	            
	            mailSender.send(message.getMimeMessage());
			} 
	        catch (MailException | MessagingException e)
			{
	        	LOGGER.error(e.toString());
			}
		}
	 
	
}
