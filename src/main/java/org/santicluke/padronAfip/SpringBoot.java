package org.santicluke.padronAfip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringBoot {
	public static void main(String[] args) {
		SpringApplication.run(SpringBoot.class, args);
	}

}
