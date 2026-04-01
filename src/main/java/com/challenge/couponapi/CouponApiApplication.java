package com.challenge.couponapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CouponApiApplication {

	/**
	 * The entry point of the Coupon API application. This class initializes the
	 * Spring Boot framework, performs component scanning, and starts the embedded
	 * web server.
	 * 
	 * @param args Command line arguments passed during application startup.
	 */
	public static void main(String[] args) {
		SpringApplication.run(CouponApiApplication.class, args);
	}
}