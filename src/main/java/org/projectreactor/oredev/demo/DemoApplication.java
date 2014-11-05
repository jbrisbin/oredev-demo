package org.projectreactor.oredev.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.spring.annotation.EnableRatpack;
import reactor.spring.context.config.EnableReactor;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableMongoRepositories
@EnableRatpack
@EnableReactor
public class DemoApplication {

	@Bean
	public Action<Chain> handlers(ObjectMapper mapper,
	                              ModelMapper beanMapper) {
		return (chain) -> {

		};
	}

	@Bean
	public ModelMapper beanMapper() {
		return new ModelMapper();
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
