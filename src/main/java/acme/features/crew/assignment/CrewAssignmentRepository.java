
package acme.features.crew.assignment;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.activityLog.ActivityLog;
import acme.entities.assignment.Assignment;
import acme.entities.assignment.DutyCrew;

@Repository
public interface CrewAssignmentRepository extends AbstractRepository {

	@Query("select a from Assignment a where a.id = :id")
	Assignment findAssignmentById(int id);

	@Query("select a from Assignment a")
	Collection<Assignment> findAllAssignments();

	@Query("SELECT a FROM Assignment a WHERE a.crew.id = :crewId AND a.leg.status = 'LANDED'")
	Collection<Assignment> findCompletedAssignmentsByCrewId(int crewId);

	@Query("SELECT a FROM Assignment a WHERE a.crew.id = :crewId AND a.leg.status != 'LANDED'")
	Collection<Assignment> findPlannedAssignmentsByCrewId(int crewId);

	@Query("select a from Assignment a where a.crew.id = :crewId")
	Collection<Assignment> findAssignmentsByCrewId(int crewId);

	@Query("select count(a) from Assignment a where a.leg.id = :legId and a.duty = :duty")
	long countByLegAndDuty(int legId, DutyCrew duty);

	@Query("select aL from ActivityLog aL where aL.assignment.id = :id")
	Collection<ActivityLog> findActivitiesLogsByAssignmentId(int id);

}
