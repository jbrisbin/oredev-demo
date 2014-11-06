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
import ratpack.handling.Context;
import ratpack.render.Renderer;
import ratpack.render.RendererSupport;
import ratpack.spring.annotation.EnableRatpack;
import reactor.core.Environment;
import reactor.rx.Stream;
import reactor.rx.Streams;
import reactor.rx.stream.HotStream;

import static ratpack.jackson.Jackson.fromJson;
import static ratpack.jackson.Jackson.json;
import static ratpack.websocket.WebSockets.websocketBroadcast;
import static reactor.core.Environment.get;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableRatpack
public class DemoApplication {

	private static final Logger LOG = LoggerFactory.getLogger(DemoApplication.class);

	static {
		// init static Environment
		Environment.initialize();
	}

	@Bean
	public HotStream<Person> personStream() {
		return Streams.defer();
	}

	@Bean
	public Action<Chain> handlers(PersonRepository persons,
	                              HotStream<Person> personStream,
	                              ObjectMapper jsonMapper,
	                              ModelMapper beanMapper) {
		return (chain) -> {
			chain.handler("person", ctx ->
					ctx.byMethod(spec ->
							             spec
									             .get(c -> {
										             c.render(Streams.just("")
										                             .dispatchOn(get())
										                             .map(o -> persons.findAll()));
									             })
									             .put(c -> {
										             Person p = c.parse(fromJson(Person.class));

										             c.render(Streams.just(p.getId())
										                             .dispatchOn(get())
										                             .<Person>map(persons::findOne)
										                             .observe(pers -> beanMapper.map(p, pers))
										                             .map(persons::save));
									             })
									             .post(c -> {
										             Person p = c.parse(fromJson(Person.class));

										             c.render(Streams.just(p)
										                             .dispatchOn(get())
										                             .map(persons::save)
										                             .observe(personStream::broadcastNext));
									             })
					));

			chain.handler("person/updates", ctx -> websocketBroadcast(ctx, personStream.map(p -> p.toJson(jsonMapper))));
		};
	}

	@Bean
	public ClientErrorHandler clientErrorHandler() {
		return (ctx, status) -> LOG.error("client error: {}", status);
	}

	@Bean
	public Renderer<Stream> streamRenderer() {
		return new RendererSupport<Stream>() {
			@Override
			public void render(Context ctx, Stream s) throws Exception {
				ctx.promise(f -> s.when(Throwable.class, t -> f.error((Throwable) t))
				                  .consume(f::success))
				   .onError(t -> {
					   ctx.clientError(500);
					   LOG.error(t.getMessage(), t);
				   })
				   .then(o -> ctx.render(json(o)));
			}
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
