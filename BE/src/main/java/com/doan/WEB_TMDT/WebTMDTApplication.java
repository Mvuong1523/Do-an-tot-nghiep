package com.doan.WEB_TMDT;

import com.doan.WEB_TMDT.module.auth.entity.Role;
import com.doan.WEB_TMDT.module.auth.entity.Status;
import com.doan.WEB_TMDT.module.auth.entity.User;
import com.doan.WEB_TMDT.module.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class WebTMDTApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebTMDTApplication.class, args);
	}

	@Bean
	CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder encoder) {
		return args -> {
			if (userRepository.findByEmail("admin@webtmdt.com").isEmpty()) {
				User admin = User.builder()
						.email("admin@webtmdt.com")
						.password(encoder.encode("admin123"))
						.role(Role.ADMIN)
						.status(Status.ACTIVE)
						.build();
				userRepository.save(admin);
				System.out.println("✅ Admin mặc định đã được tạo: admin@webtmdt.com / admin123");
			} else {
				System.out.println("⚠️ Admin đã tồn tại, bỏ qua tạo mới.");
			}
		};
	}
}
