package com.tbf.tcms;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Traditional Court Management API",
                version = "v1",
                description = "REST API for managing traditional court operations: users, organizations, land stands, levies, residents, dispute cases, and village events.",
                contact = @Contact(name = "The Bright Fusion", email = "support@thebrightfusion.com")
        )
)
public class TcmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TcmsApplication.class, args);
	}

}
