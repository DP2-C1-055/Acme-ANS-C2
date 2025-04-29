
package acme.features.crew.assignment;

import java.util.Collection;
import java.util.Date;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.activityLog.ActivityLog;
import acme.entities.assignment.Assignment;
import acme.entities.leg.Leg;
import acme.realms.crew.AvailabilityStatus;
import acme.realms.crew.Crew;

@Repository
public interface CrewAssignmentRepository extends AbstractRepository {

	@Query("select a from Assignment a where a.id = :id")
	Assignment findAssignmentById(int id);

	@Query("select a from Assignment a")
	Collection<Assignment> findAllAssignments();

	@Query("SELECT a FROM Assignment a WHERE a.leg.scheduledArrival < :now AND a.crew.id = :crewId")
	Collection<Assignment> findCompletedAssignmentsByCrewId(Date now, int crewId);

	@Query("SELECT a FROM Assignment a WHERE a.leg.scheduledDeparture > :now AND a.crew.id = :crewId")
	Collection<Assignment> findPlannedAssignmentsByCrewId(Date now, int crewId);

	@Query("select a from Assignment a where a.crew.id = :crewId")
	Collection<Assignment> findAssignmentsByCrewId(int crewId);

	@Query("select aL from ActivityLog aL where aL.assignment.id = :id")
	Collection<ActivityLog> findActivitiesLogsByAssignmentId(int id);

	@Query("select l from Leg l")
	Collection<Leg> findAllLegs();

	@Query("select c from Crew c")
	Collection<Crew> findAllCrewMembers();

	@Query("select l from Leg l where l.id = :legId")
	Leg findLegById(Integer legId);

	@Query("select c from Crew c where c.id = :crewId")
	Crew findCrewById(Integer crewId);

	@Query("select distinct a.leg from Assignment a where a.crew.id = :id")
	Collection<Leg> findLegsByCrewId(int id);

	@Query("select a from Assignment a where a.leg.id = :id")
	Collection<Assignment> findAssignmentByLegId(int id);

	@Query("select fcm from Crew fcm where fcm.availability = :available")
	Collection<Crew> findCrewByAvailability(AvailabilityStatus available);

	@Query("SELECT CASE WHEN COUNT(fcm) > 0 THEN true ELSE false END FROM Crew fcm WHERE fcm.id = :crewId")
	boolean existsFlightCrewMember(int crewId);

}
