package demo;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.event.dispatch.Dispatcher;

/**
 * @author Jon Brisbin
 */
public class TestSubscriber implements Subscriber<String> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Dispatcher dispatcher;

	private Subscription subscription;

	public TestSubscriber(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		if (null != this.subscription) {
			subscription.cancel();
			return;
		}
		this.subscription = subscription;

		dispatcher.execute(() -> this.subscription.request(1));
	}

	@Override
	public void onNext(String s) {
		log.info("onNext: {}", s);
		if (s.startsWith("Goodbye")) {
			log.info("stop receiving events...");
			dispatcher.execute(() -> this.subscription.cancel());
		} else {
			dispatcher.execute(() -> this.subscription.request(1));
		}
	}

	@Override
	public void onError(Throwable throwable) {

	}

	@Override
	public void onComplete() {
		log.info("onComplete: {}");
	}

}
