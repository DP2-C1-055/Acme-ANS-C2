
package acme.features.crew.activityLog;

import java.util.Collection;
import java.util.Date;

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
	Collection<ActivityLog> findAllActivityLogsByCrewId(int flightCrewMemberId);

	@Query("select a from Assignment a where a.id = :assignmentId")
	Assignment findAssignmentById(int assignmentId);

	@Query("select a from Assignment a where a.crew.id = :id")
	Collection<Assignment> findAssignmentsByCrewId(int id);

	@Query("select al from ActivityLog al where al.assignment.crew.id = :id or al.draftMode = false")
	Collection<ActivityLog> findLogsPublishedAndByCrewId(int id);

	@Query("select al from ActivityLog al where al.assignment.id = :assignmentId")
	Collection<ActivityLog> findActivityLogsByAssignmentId(int assignmentId);

	@Query("select a from Assignment a where a.crew.id = :crewId or a.draftMode = false")
	Collection<Assignment> findAssignmentPublishedByCrewId(int crewId);

	@Query("select case when count(fcm) > 0 then true else false end from Crew fcm where fcm.id = :crewId")
	boolean existsFlightCrewMember(int crewId);

	@Query("select case when count(fa) > 0 then true else false end from Assignment fa where fa.id = :id and fa.draftMode = false")
	boolean isAssignmentAlreadyPublishedById(int id);

	@Query("select count(al) > 0 from ActivityLog al where al.id = :activityLogId and al.assignment.crew.id = :flightCrewMemberId")
	boolean thatActivityLogIsOf(int activityLogId, int flightCrewMemberId);

	@Query("select al.assignment from ActivityLog al where al.id = :id")
	Assignment findAssignmentByActivityLogId(int id);

	@Query("select case when count(al) > 0 then true else false end from ActivityLog al where al.id = :activityLogId and al.assignment.leg.scheduledArrival < :activityLogMoment")
	boolean isAssociatedWithCompletedLeg(int activityLogId, Date activityLogMoment);
}
