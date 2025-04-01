
package acme.features.manager.leg;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.aircraft.Aircraft;
import acme.entities.airport.Airport;
import acme.entities.leg.Leg;

@Repository
public interface ManagerLegRepository extends AbstractRepository {

	@Query("select l from Leg l where l.flightNumber = :flightNumber")
	Leg findLegByFlightNumber(String flightNumber);

	@Query("select l from Leg l where l.flight.id = :flightId order by l.scheduledDeparture asc")
	Collection<Leg> findLegsByFlightIdOrderByScheduledDepartureAsc(int flightId);

	@Query("select l from Leg l where l.flight.id = :id order by l.scheduledArrival desc")
	Collection<Leg> findLegsByFlightIdOrderByScheduledArrivalDesc(int id);

	@Query("select l from Leg l where l.flight.id = :id")
	Collection<Leg> findLegsByFlightId(int id);

	@Query("select l from Leg l where l.flight.manager.id = :managerId order by l.scheduledDeparture asc")
	Collection<Leg> findLegsByManagerIdOrderedByScheduledDeparture(int managerId);

	@Query("select l from Leg l where l.id = :id")
	Leg findLegById(int id);

	@Query("select a from Airport a")
	Collection<Airport> findAllAirports();

	@Query("select a from Aircraft a")
	Collection<Aircraft> findAllAircrafts();

	@Query("select l from Leg l where l.flightNumber = :flightNumber")
	Leg findByFlightNumber(String flightNumber);

}
