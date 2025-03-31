
package acme.entities.technicians;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.airport.Airport;

@Repository
public interface TechnicianRepository extends AbstractRepository {

	@Query("select a from Technician a where a.licenseNumber = :licenseNumber")
	Technician findByLicenseNumber(String licenseNumber);
}