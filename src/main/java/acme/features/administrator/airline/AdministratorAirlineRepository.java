
package acme.features.administrator.airline;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.airline.Airline;

@Repository
public interface AdministratorAirlineRepository extends AbstractRepository {

	@Query("select count(a) > 0 from Airline a where a.iataCode = :iataCode")
	boolean existsByIataCode(String iataCode);

	@Query("Select a from Airline a")
	Collection<Airline> findAllAirlines();

	@Query("Select a from Airline a where a.id =:id")
	Airline findAirlineById(int id);

	@Query("select a from Airline a where a.iataCode = :iataCode")
	Airline findAirlineByIataCode(String iataCode);

	@Query("Select a.iataCode from Airline a")
	Collection<String> getAllIataCode();
}
