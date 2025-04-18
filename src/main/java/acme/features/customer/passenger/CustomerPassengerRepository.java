
package acme.features.customer.passenger;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.passenger.Passenger;

@Repository
public interface CustomerPassengerRepository extends AbstractRepository {

	@Query("Select p from Passenger p where p.id =:id")
	Passenger findPassengerById(int id);

	@Query("Select p.passportNumber from Passenger p")
	Collection<String> getAllPassportNumber();

	@Query("Select p from Passenger p where p.customer.id =:customerId ")
	Collection<Passenger> findPassengenrsByCustomerId(int customerId);

	@Query("Select p from Passenger p where p.customer.id =:customerId and p.draftMode = false ")
	Collection<Passenger> findPassengenrsDraftModeByCustomerId(int customerId);

}
