
package acme.features.crew.activityLog;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.activityLog.ActivityLog;

@Repository
public interface CrewActivityLogRepository extends AbstractRepository {

	@Query("select a from ActivityLog a where a.id = :id")
	ActivityLog findActivityLogById(int id);

	@Query("select a from ActivityLog a where a.assignment.crew.id = :flightCrewMemberId")
	Collection<ActivityLog> findAllActivityLogs(int flightCrewMemberId);

}
