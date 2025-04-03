
package acme.features.crew.activityLog;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
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
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		Collection<ActivityLog> activityLogs;
		int assignmentId;

		assignmentId = super.getRequest().getData("id", int.class);
		activityLogs = this.repository.findActivityLogsByAssignmentId(assignmentId);

		super.getBuffer().addData(activityLogs);
	}

	@Override
	public void unbind(final ActivityLog activityLog) {
		Dataset dataset;
		int assignmentId;
		Assignment assignment;
		boolean showingCreate;
		boolean correctFlightCrewMember;
		int memberId;
		int userId;

		assignmentId = super.getRequest().getData("assignmentId", int.class);
		assignment = this.repository.findAssignmentById(assignmentId);
		userId = super.getRequest().getPrincipal().getActiveRealm().getId();
		memberId = assignment.getCrew().getId();

		correctFlightCrewMember = memberId == userId;

		showingCreate = !assignment.isDraftMode() && correctFlightCrewMember;

		dataset = super.unbindObject(activityLog, "typeIncident", "severityLevel", "registrationMoment");
		dataset.put("flightNumber", activityLog.getAssignment().getLeg().getFlightNumber());

		super.getResponse().addGlobal("assignmentId", assignmentId);
		super.getResponse().addGlobal("showingCreate", showingCreate);

		super.addPayload(dataset, activityLog, "description");
		super.getResponse().addData(dataset);
	}
}
