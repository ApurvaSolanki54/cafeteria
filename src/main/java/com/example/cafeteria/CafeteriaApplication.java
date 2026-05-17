package com.example.cafeteria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@EnableScheduling: Activates the @Scheduled annotation in CoinRefillService.
//  Without this, the cron job would never run!
@EnableScheduling
public class CafeteriaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CafeteriaApplication.class, args);
	}

}
