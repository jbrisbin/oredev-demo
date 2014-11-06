package org.projectreactor.oredev.demo;

import org.projectreactor.oredev.demo.domain.Person;
import org.springframework.stereotype.Component;
import ratpack.handling.Context;
import ratpack.render.RendererSupport;

import static ratpack.jackson.Jackson.json;

/**
 * @author Jon Brisbin
 */
@Component
public class PersonRenderer extends RendererSupport<Person> {
	@Override
	public void render(Context ctx, Person p) throws Exception {
		ctx.render(json(p));
	}
}
