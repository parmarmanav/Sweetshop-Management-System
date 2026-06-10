package com.sweetshop.sweetshop_backend;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableConfigurationProperties
public class SweetshopBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SweetshopBackendApplication.class, args);
	}

	@RestController
	public static class HelloController {
		
		@GetMapping("/")
		public String hello() {
			return "Hello World!";
		}
	}
}
