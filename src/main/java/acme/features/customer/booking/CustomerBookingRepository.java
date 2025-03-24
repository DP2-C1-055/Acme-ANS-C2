
package acme.features.customer.booking;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.booking.Booking;

@Repository
public interface CustomerBookingRepository extends AbstractRepository {

	@Query("Select b from Booking b where b.id =:bookingId")
	Booking findBookingById(int bookingId);

	@Query("Select b from Booking b where b.customer.id =:customerId")
	Collection<Booking> findBookingByCustomer(int customerId);

}
