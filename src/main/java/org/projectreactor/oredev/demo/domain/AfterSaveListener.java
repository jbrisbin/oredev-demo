package org.projectreactor.oredev.demo.domain;

import org.springframework.context.ApplicationListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;
import reactor.rx.stream.HotStream;

import javax.inject.Inject;

/**
 * @author Jon Brisbin
 */
@Component
public class AfterSaveListener implements ApplicationListener<AfterSaveEvent<Person>> {

	private final HotStream<Person> personStream;

	@Inject
	public AfterSaveListener(HotStream<Person> personStream) {
		this.personStream = personStream;
	}

	@Override
	public void onApplicationEvent(AfterSaveEvent<Person> evt) {
		personStream.broadcastNext(evt.getSource());
	}

}
