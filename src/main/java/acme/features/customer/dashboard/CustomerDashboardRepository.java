
package acme.features.customer.dashboard;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.booking.Booking;
import acme.entities.passenger.Passenger;
import acme.realms.Customer.Customer;

@Repository
public interface CustomerDashboardRepository extends AbstractRepository {

	@Query("Select c from Customer c where c.id =:id")
	Customer getCustomerById(int id);

	@Query("Select b from Booking b where b.draftMode = false and b.customer.id =:customerId")
	Collection<Booking> findBookingConfirmedByCustomer(int customerId);

	@Query("Select b.id from Booking b where b.draftMode = false and b.customer.id =:customerId")
	Collection<Long> findBookingIdConfirmedByCustomer(int customerId);

	@Query("Select br.passenger from BookingRecord br where br.booking.id =:bookingId")
	Collection<Passenger> findPassengenrsByBooking(int bookingId);

	@Query("SELECT br.booking.id, COUNT(br.booking.id) FROM BookingRecord br WHERE br.booking.id IN :bookingsId GROUP BY br.booking.id")
	List<Object[]> countBookingsById(@Param("bookingsId") Collection<Integer> bookingsId);

	@Query("select b from Booking b where b.customer = :customer and b.flight.cost.currency = :currency and b.draftMode = false and b.purchaseMoment >= :fiveYearsAgo")
	Collection<Booking> findBookingsOfLastFiveYears(Customer customer, String currency, Date fiveYearsAgo);

	@Query("Select b from Booking b where b.draftMode = false and b.customer.id =:customerId order by b.purchaseMoment desc")
	Collection<Booking> findLastBookingConfirmedByCustomer(int customerId);

	@Query("SELECT b.travelClass, COUNT(b) FROM Booking b WHERE b.customer = :customer AND b.draftMode = false GROUP BY b.travelClass")
	List<Object[]> findBookingCountByTravelClass(Customer customer);

}
