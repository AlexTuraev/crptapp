package crtpapi.com.test.crptapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CrptappApplication {

	public static void main(String[] args) {

		SpringApplication.run(CrptappApplication.class, args);


		// ------------------------------------------------------------------------

	    CrptApi crptApi = null;
		try {
			crptApi = new CrptApi(5);
			crptApi.createDoc(crptApi.prepareProductJson());
		} catch (Exception e) {
			System.out.printf(e.getMessage());
		}
	}

}
