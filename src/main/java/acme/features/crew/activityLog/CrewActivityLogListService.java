
package acme.features.crew.activityLog;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.activityLog.ActivityLog;
import acme.entities.assignment.Assignment;
import acme.realms.crew.Crew;

@GuiService
public class CrewActivityLogListService extends AbstractGuiService<Crew, ActivityLog> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CrewActivityLogRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean status;
		boolean authorised;
		boolean isLogged;

		int assignmentId;
		int crewId;
		Assignment assignment;

		assignmentId = super.getRequest().getData("assignmentId", int.class);
		assignment = this.repository.findAssignmentById(assignmentId);

		crewId = super.getRequest().getPrincipal().getActiveRealm().getId();
		authorised = this.repository.existsCrewMember(crewId);

		status = authorised && assignment != null;
		isLogged = assignment.getCrew().getId() == crewId;

		super.getResponse().setAuthorised(status && isLogged);
	}

	@Override
	public void load() {
		Collection<ActivityLog> activityLogs;
		int assignmentId;
		Assignment assignment;

		assignmentId = super.getRequest().getData("assignmentId", int.class);
		activityLogs = this.repository.findActivityLogsByAssignmentId(assignmentId);

		assignment = this.repository.findAssignmentById(assignmentId);
		super.getResponse().addGlobal("draftModeAssignment", assignment.isDraftMode());
		boolean isCompleted = assignment.getLeg().getScheduledArrival().before(MomentHelper.getCurrentMoment());
		super.getResponse().addGlobal("isCompleted", isCompleted);

		super.getResponse().addGlobal("id", assignmentId);
		super.getBuffer().addData(activityLogs);
	}

	@Override
	public void unbind(final ActivityLog activityLog) {
		Dataset dataset;
		int assignmentId;

		assignmentId = super.getRequest().getData("assignmentId", int.class);
		dataset = super.unbindObject(activityLog, "registrationMoment", "typeIncident", "severityLevel", "draftMode");
		dataset.put("flightNumber", activityLog.getAssignment().getLeg().getFlightNumber());

		super.getResponse().addGlobal("id", assignmentId);
		super.getResponse().addData(dataset);
	}
}
