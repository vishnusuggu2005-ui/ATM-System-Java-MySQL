package com.example.atm;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AtmApplication {
    public static void main(String[] args) {
        SpringApplication.run(AtmApplication.class, args);
    }

    @Bean
    CommandLineRunner seedDemoAccount(JdbcTemplate jdbc) {
        return args -> {
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM accounts WHERE account_number = ?",
                    Integer.class, "1001001");
            if (count != null && count == 0) {
                String pinHash = new BCryptPasswordEncoder().encode("1234");
                jdbc.update("INSERT INTO accounts(account_number, holder_name, pin_hash, balance) VALUES (?, ?, ?, ?)",
                        "1001001", "Demo User", pinHash, new java.math.BigDecimal("5000.00"));
                System.out.println("Demo account created: account 1001001, PIN 1234");
            }
        };
    }
}
