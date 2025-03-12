
package acme.features.administrator.airline;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;

@Repository
public interface AdministratorAirlineRepository extends AbstractRepository {

	@Query("select count(a) > 0 from Airline a where a.iataCode = :iataCode")
	boolean existsByIataCode(String iataCode);

}
