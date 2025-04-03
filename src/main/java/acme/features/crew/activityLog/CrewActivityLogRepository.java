
package acme.features.crew.activityLog;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.activityLog.ActivityLog;
import acme.entities.assignment.Assignment;

@Repository
public interface CrewActivityLogRepository extends AbstractRepository {

	@Query("select a from ActivityLog a where a.id = :id")
	ActivityLog findActivityLogById(int id);

	@Query("select a from ActivityLog a where a.assignment.crew.id = :flightCrewMemberId")
	Collection<ActivityLog> findAllActivityLogs(int flightCrewMemberId);

	@Query("select a from Assignment a where a.id = :assignmentId")
	Assignment findAssignmentById(int assignmentId);

	@Query("select a from Assignment a where a.crew.id = :id")
	Collection<Assignment> findAssignmentsByCrewId(int id);

	@Query("select al from ActivityLog al where al.assignment.crew.id = :id or al.draftMode = false")
	Collection<ActivityLog> findLogsPublishedAndByCrewId(int id);

	@Query("select al from ActivityLog al where al.assignment.id = :assignmentId")
	Collection<ActivityLog> findActivityLogsByAssignmentId(int assignmentId);

	@Query("select a from Assignment a where a.crew.id = :crewId or a.draftMode = false")
	Collection<Assignment> findActivityLogPublishedByCrewId(int crewId);
}
