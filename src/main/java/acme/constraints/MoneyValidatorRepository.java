
package acme.constraints;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;

@Repository
public interface MoneyValidatorRepository extends AbstractRepository {

	@Query("SELECT s.acceptedCurrency FROM SystemConfiguration s")
	String findAcceptedCurrencies();

}
