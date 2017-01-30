package info.dia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextListener;

@SpringBootApplication
public class DiaElearningApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiaElearningApplication.class, args);
	}
	
	 @Bean
	 public RequestContextListener requestContextListener() {
	        return new RequestContextListener();
	 }
	 
}
