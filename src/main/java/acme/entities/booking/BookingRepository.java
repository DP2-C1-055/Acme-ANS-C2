
package acme.entities.booking;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;

import acme.client.repositories.AbstractRepository;
import acme.entities.passenger.Passenger;

public interface BookingRepository extends AbstractRepository {

	@Query("select b.passenger from BookingRecord b where b.booking.id = :bookingId")
	Collection<Passenger> findPassengerByBooking(int bookingId);
}
