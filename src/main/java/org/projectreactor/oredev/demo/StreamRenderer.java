package org.projectreactor.oredev.demo;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ratpack.handling.Context;
import ratpack.render.RendererSupport;
import reactor.rx.Stream;

import static ratpack.jackson.Jackson.json;

/**
 * @author Jon Brisbin
 */
@Component
public class StreamRenderer extends RendererSupport<Stream> {
	@Override
	public void render(Context ctx, Stream s) throws Exception {
		ctx.promise(f -> s.when(Throwable.class, t -> f.error((Throwable) t))
		                  .consume(f::success))
		   .onError(t -> {
			   ctx.clientError(500);
			   LoggerFactory.getLogger(getClass()).error(t.getMessage(), t);
		   })
		   .then(o -> ctx.render(json(o)));
	}
}
