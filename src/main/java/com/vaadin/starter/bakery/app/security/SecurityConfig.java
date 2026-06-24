package com.vaadin.starter.bakery.app.security;

import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.vaadin.starter.bakery.app.Application;
import com.vaadin.starter.bakery.backend.data.Role;

@EnableWebSecurity
@Configuration
@NullMarked
public class SecurityConfig {

	private final RedirectAuthenticationSuccessHandler successHandler;

	@Autowired
	public SecurityConfig(UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder,
			RedirectAuthenticationSuccessHandler successHandler) {
		this.successHandler = successHandler;
	}

	@Bean
	@SuppressWarnings("java:S4502")
	public SecurityFilterChain configure(HttpSecurity http) throws Exception {

		http.authorizeHttpRequests(auth -> auth
				.requestMatchers(new AntPathRequestMatcher("/VAADIN/**"))
				.permitAll().requestMatchers(new AntPathRequestMatcher("/**"))
				.hasAnyAuthority(Role.getAllRoles()));

		// Not using Spring CSRF here to be able to use plain HTML for the login
		// page
		http.csrf(AbstractHttpConfigurer::disable);

		http.formLogin(config -> config.loginPage(Application.LOGIN_URL)
				.loginProcessingUrl(Application.LOGIN_PROCESSING_URL)
				.failureUrl(Application.LOGIN_FAILURE_URL)
				.successHandler(successHandler).permitAll());

		http.logout(config -> config.logoutSuccessUrl(Application.LOGOUT_URL));

		return http.build();
	}

}
