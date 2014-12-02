package org.projectreactor.oredev.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.projectreactor.oredev.demo.domain.Person;
import org.projectreactor.oredev.demo.domain.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ratpack.error.ClientErrorHandler;
import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.spring.annotation.EnableRatpack;
import reactor.core.Environment;
import reactor.rx.Streams;
import reactor.rx.stream.HotStream;
import reactor.tuple.Tuple;

import static ratpack.jackson.Jackson.fromJson;
import static ratpack.websocket.WebSockets.websocketBroadcast;
import static reactor.core.Environment.cachedDispatcher;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableRatpack
public class DemoApplication {

	private static final Logger LOG = LoggerFactory.getLogger(DemoApplication.class);

	private static final Environment ENV;

	static {
		ENV = Environment.initialize();
	}

	@Bean
	public HotStream<Person> personStream() {
		return Streams.defer(ENV);
	}

	@Bean
	public Action<Chain> handlers(PersonRepository personRepo,
	                              HotStream<Person> personStream,
	                              ObjectMapper jsonMapper,
	                              ModelMapper beanMapper) {
		return (chain) -> {
			chain.handler("person", ctx -> {
				ctx.byMethod(spec -> spec
						.get(c -> c.render(Streams.just(c)
						                          .dispatchOn(cachedDispatcher())
						                          .map(o -> personRepo.findAll())))

						.post(c -> c.render(Streams.just(c.parse(fromJson(Person.class)))
						                           .dispatchOn(cachedDispatcher())
						                           .<Person>map(personRepo::save)
						                           .observe(personStream::broadcastNext)))

						.put(c -> c.render(Streams.just(c.parse(fromJson(Person.class)))
						                          .dispatchOn(cachedDispatcher())
						                          .map(p -> Tuple.of(p, personRepo.findOne(p.getId())))
						                          .observe(tup -> beanMapper.map(tup.getT1(), tup.getT2()))
						                          .map(tup -> personRepo.save(tup.getT2())))));
			});

			chain.handler("person/updates", ctx -> {
				websocketBroadcast(ctx, personStream.map(p -> p.toJson(jsonMapper)));
			});
		};
	}

	@Bean
	public ClientErrorHandler clientErrorHandler() {
		return (ctx, status) -> LOG.error("client error: {}", status);
	}

	@Bean
	public ModelMapper beanMapper() {
		return new ModelMapper();
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
