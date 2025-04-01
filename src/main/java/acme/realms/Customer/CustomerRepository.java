
package acme.realms.Customer;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;

@Repository
public interface CustomerRepository extends AbstractRepository {

	@Query("SELECT c.identifier FROM Customer c")
	List<String> findAllIdentifiers();

}
