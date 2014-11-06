package org.projectreactor.oredev.demo.domain;

import org.springframework.data.repository.CrudRepository;

/**
 * @author Jon Brisbin
 */
public interface PersonRepository extends CrudRepository<Person, String> {
}
