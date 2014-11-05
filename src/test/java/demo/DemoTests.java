package demo;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import reactor.event.Event;
import reactor.event.selector.Selectors;
import reactor.function.Predicate;
import reactor.rx.Stream;
import reactor.rx.Streams;
import reactor.rx.stream.HotStream;

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
	public void simpleReactor() {
		Reactor reactor = Reactors.reactor(ENV);

		reactor.on(Selectors.uri("/first/{second}/third"), (Event<String> ev) -> {
			LOG.info("from consumer: {}", ev);
			LOG.info("path param: {}", ev.getHeaders().<String>get("second"));
		});

		reactor.notify("/first/second/third", Event.wrap("Hello World!"));
	}

	@Test
	public void simpleHotStream() {
		Stream<String> items = Streams.just("Hello World!", "Goodbye World!");
		HotStream<String> str = Streams.defer(ENV);

		items.map(String::toUpperCase)
		   .filter(new Predicate<String>() {
			   @Override
			   public boolean test(String s) {
				   return s.startsWith("HELLO");
			   }
		   })
		   .consume(s -> LOG.info("consumed string: {}", s));

		str.broadcastNext("Hello World!");
	}

	@Test
	public void simpleReactiveSubscriber() {
		HotStream<String> str = Streams.defer(ENV);

		str.subscribe(new TestSubscriber(str.getDispatcher()));

		str.broadcastNext("Hello World!");
		str.broadcastNext("Goodbye World!");
		str.broadcastNext("Hello World!");
		str.broadcastComplete();
	}

}
