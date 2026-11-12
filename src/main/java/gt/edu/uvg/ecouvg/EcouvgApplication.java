package gt.edu.uvg.ecouvg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling 
public class EcouvgApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcouvgApplication.class, args);
	}

}
 