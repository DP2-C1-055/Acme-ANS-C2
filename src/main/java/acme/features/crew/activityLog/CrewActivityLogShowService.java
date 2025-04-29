
package acme.features.crew.activityLog;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
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
		int activityLogId;

		ActivityLog activityLog;

		activityLogId = super.getRequest().getData("id", int.class);
		activityLog = this.repository.findActivityLogById(activityLogId);
		int flightCrewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();
		Assignment assignment = this.repository.findAssignmentByActivityLogId(activityLogId);
		boolean authorised1 = this.repository.existsFlightCrewMember(flightCrewMemberId);
		boolean authorised = authorised1 && this.repository.thatActivityLogIsOf(activityLogId, flightCrewMemberId);
		boolean isAuthenticated = assignment.getCrew().getId() == flightCrewMemberId;

		super.getResponse().setAuthorised(authorised && activityLog != null && isAuthenticated);
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
		SelectChoices selectedAssignments;
		Collection<Assignment> assignments;
		int member;

		member = super.getRequest().getPrincipal().getActiveRealm().getId();
		assignments = this.repository.findAssignmentPublishedByCrewId(member);
		selectedAssignments = SelectChoices.from(assignments, "leg.flightNumber", activityLog.getAssignment());

		dataset = super.unbindObject(activityLog, "registrationMoment", "typeIncident", "description", "severityLevel", "draftMode");
		dataset.put("assignmentId", activityLog.getAssignment().getId());
		dataset.put("assignments", selectedAssignments);
		dataset.put("assignment", selectedAssignments.getSelected().getKey());
		super.getResponse().addData(dataset);
	}

}
