package org.projectreactor.oredev.demo;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import reactor.event.Event;
import reactor.function.Predicate;
import reactor.rx.Streams;
import reactor.rx.stream.HotStream;

import static reactor.event.selector.Selectors.$;
import static reactor.event.selector.Selectors.U;

/**
 * @author Jon Brisbin
 */
public class DemoTests {

	static Environment ENV = new Environment();
	static Logger      LOG = LoggerFactory.getLogger(DemoTests.class);

	@After
	public void cleanup() throws InterruptedException {
		Thread.sleep(500);
	}

	@Test
	public void reactorWithStringSelector() {
		Reactor reactor = Reactors.reactor(ENV);

		reactor.on($("topic.string"), (Event<String> ev) -> {
			LOG.info("from consumer: {}", ev);
		});

		reactor.notify("topic.string", Event.wrap("Hello World!"));
	}

	@Test
	public void reactorWithUriSelector() {
		Reactor reactor = Reactors.reactor(ENV);

		reactor.on(U("/first/{second}/third"), (Event<String> ev) -> {
			LOG.info("from consumer: {}", ev);
			LOG.info("path param: {}", ev.getHeaders().<String>get("second"));
		});

		reactor.notify("/first/second/third", Event.wrap("Hello World!"));
	}

	@Test
	public void simpleHotStream() {
		HotStream<String> str = Streams.defer(ENV);

		str
				.map(String::toUpperCase)
				.filter(new Predicate<String>() {
					@Override
					public boolean test(String s) {
						return s.startsWith("HELLO");
					}
				})
				.observeComplete(v -> LOG.info("complete()"))
				.consume(s -> LOG.info("consumed string: {}", s));

		str.broadcastNext("Hello World!");
		str.broadcastNext("Goodbye World!");
		str.broadcastComplete();
	}

}
