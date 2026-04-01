package com.challenge.couponapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Application Context Load Test")
class CouponApiApplicationTests {

	@Test
	@DisplayName("Should load the Spring Context successfully")
	void contextLoads() {
		// Act + Assert: context initialization succeeds if no exception is thrown
	}

	@Test
	@DisplayName("Should execute main method for coverage purposes")
	void mainMethodTest() {

		// Act: invoke application entry point
		CouponApiApplication.main(new String[] {});

		// Assert: no exception means successful execution
	}
}