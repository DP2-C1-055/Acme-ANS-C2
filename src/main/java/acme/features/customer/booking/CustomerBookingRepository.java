
package acme.features.customer.booking;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.booking.Booking;
import acme.entities.flight.Flight;
import acme.realms.Customer.Customer;

@Repository
public interface CustomerBookingRepository extends AbstractRepository {

	@Query("Select b from Booking b where b.id =:bookingId")
	Booking findBookingById(int bookingId);

	@Query("Select b from Booking b where b.customer.id =:customerId")
	Collection<Booking> findBookingByCustomer(int customerId);

	@Query("Select b.locatorCode from Booking b")
	Collection<String> getAllLocatorCode();

	@Query("Select f from Flight f where f.draftMode = false")
	Collection<Flight> getAllFlightWithDraftModeFalse();

	@Query("Select f from Flight f where f.id =:id")
	Flight getFlightById(int id);

	@Query("Select b.flight from Booking b where b.id =:id")
	Collection<Flight> getOneFlightByBookingId(int id);

	@Query("Select c from Customer c where c.id =:id")
	Customer getCustomerById(int id);

}
