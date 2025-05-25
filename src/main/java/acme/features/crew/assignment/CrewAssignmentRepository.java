
package acme.features.crew.assignment;

import java.util.Collection;
import java.util.Date;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.activityLog.ActivityLog;
import acme.entities.assignment.Assignment;
import acme.entities.assignment.DutyCrew;
import acme.entities.leg.Leg;
import acme.realms.crew.AvailabilityStatus;
import acme.realms.crew.Crew;

@Repository
public interface CrewAssignmentRepository extends AbstractRepository {

	@Query("select a from Assignment a where a.id = :id")
	Assignment findAssignmentById(int id);

	@Query("select a from Assignment a")
	Collection<Assignment> findAllAssignments();

	@Query("select a from Assignment a where a.leg.scheduledArrival < :now and a.crew.id = :crewId")
	Collection<Assignment> findCompletedAssignmentsByCrewId(Date now, int crewId);

	@Query("select a from Assignment a where a.leg.scheduledArrival >= :now and a.crew.id = :crewId")
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

	@Query("select cm from Crew cm where cm.availability = :available")
	Collection<Crew> findCrewByAvailability(AvailabilityStatus available);

	@Query("select case when count(cm) > 0 then true else false end from Crew cm where cm.id = :crewId")
	boolean existsCrewMember(int crewId);

	@Query("select count(a) > 0 from Assignment a where a.leg.id = :id and a.duty = :coPilot")
	boolean existsCrewWithDutyInLeg(int id, DutyCrew coPilot);

	@Query("select count(a) > 0 from Assignment a where a.id = :assignmentId and a.crew.id = :crewMemberId")
	boolean isAssignmentOwnedByCrewMember(int assignmentId, int crewMemberId);

	@Query("select case when count(a) > 0 then true else false end " + "from Assignment a " + "where a.id = :id " + "and a.leg.scheduledArrival < :currentMoment")
	boolean areLegsCompletedByAssignment(int id, Date currentMoment);

	@Query("select count(a) > 0 from Assignment a where a.crew.id = :crewId and a.duty = :duty")
	boolean existsAssignmentWithDuty(int crewId, DutyCrew duty);

	@Query("select case when count(l) > 0 then true else false end from Leg l where l.id = :legId and l.draftMode = false")
	boolean existsByIdAndPublishedTrue(int legId);

}
