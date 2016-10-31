package com.xiongyingqi;

import com.xiongyingqi.filter.RedirectFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import javax.servlet.Filter;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public FilterRegistrationBean someFilterRegistration() {

		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(redirectFilter());
		registration.addUrlPatterns("/*");
		registration.addInitParameter("paramName", "paramValue");
		registration.setName("redirectFilter");
		registration.setOrder(1);
		return registration;
	}

	@Bean(name = "redirectFilter")
	public Filter redirectFilter() {
		return new RedirectFilter();
	}

}
