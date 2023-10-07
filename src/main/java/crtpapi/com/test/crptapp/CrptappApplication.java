package crtpapi.com.test.crptapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class CrptappApplication {

	public static void main(String[] args) {

		SpringApplication.run(CrptappApplication.class, args);


		// ------------------------------------------------------------------------

	    CrptApi crptApi = null;
		try {
			crptApi = new CrptApi(TimeUnit.SECONDS, 5);
			crptApi.createDoc(crptApi.prepareProductJson());
		} catch (Exception e) {
			System.out.printf(e.getMessage());
		}
	}

}
