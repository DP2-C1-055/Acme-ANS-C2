
package acme.features.crew.activityLog;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.activityLog.ActivityLog;
import acme.entities.assignment.Assignment;
import acme.realms.crew.Crew;

@GuiService
public class CrewActivityLogShowService extends AbstractGuiService<Crew, ActivityLog> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CrewActivityLogRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean existCrewMember;
		boolean isAuthorised;
		boolean isLogged;

		int crewId;
		int activityLogId;

		Assignment assignment;
		ActivityLog activityLog;

		activityLogId = super.getRequest().getData("id", int.class);
		activityLog = this.repository.findActivityLogById(activityLogId);

		crewId = super.getRequest().getPrincipal().getActiveRealm().getId();
		assignment = this.repository.findAssignmentByActivityLogId(activityLogId);
		existCrewMember = this.repository.existsFlightCrewMember(crewId);

		isAuthorised = existCrewMember && this.repository.thatActivityLogIsOf(activityLogId, crewId);
		isLogged = assignment.getCrew().getId() == crewId;

		super.getResponse().setAuthorised(isAuthorised && activityLog != null && isLogged);
	}

	@Override
	public void load() {
		ActivityLog activityLog;
		int assignmentId;

		assignmentId = super.getRequest().getData("id", int.class);
		activityLog = this.repository.findActivityLogById(assignmentId);

		super.getBuffer().addData(activityLog);
	}

	@Override
	public void unbind(final ActivityLog activityLog) {

		Dataset dataset;
		Assignment assignment;

		assignment = this.repository.findAssignmentByActivityLogId(activityLog.getId());

		dataset = super.unbindObject(activityLog, "registrationMoment", "typeIncident", "description", "severityLevel", "draftMode");
		dataset.put("id", activityLog.getId());
		dataset.put("assignmentId", assignment.getId());
		dataset.put("draftMode", activityLog.isDraftMode());
		dataset.put("masterDraftMode", assignment.isDraftMode());
		dataset.put("readonly", false);

		super.getResponse().addData(dataset);
	}

}
