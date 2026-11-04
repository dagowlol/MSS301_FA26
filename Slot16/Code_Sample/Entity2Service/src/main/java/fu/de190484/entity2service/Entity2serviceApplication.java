package fu.de190484.entity2service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class Entity2serviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(Entity2serviceApplication.class, args);
	}

}
