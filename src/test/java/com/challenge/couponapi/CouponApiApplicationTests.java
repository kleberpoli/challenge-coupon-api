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
	}

	@Test
	@DisplayName("Should execute main method for coverage purposes")
	void mainMethodTest() {
		CouponApiApplication.main(new String[] {});
	}
}