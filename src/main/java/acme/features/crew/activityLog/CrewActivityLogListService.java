
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
		boolean status;
		boolean authorised;
		boolean isLogged;

		int assignmentId;
		int crewId;
		Assignment assignment;

		assignmentId = super.getRequest().getData("assignmentId", int.class);
		assignment = this.repository.findAssignmentById(assignmentId);

		crewId = super.getRequest().getPrincipal().getActiveRealm().getId();
		authorised = this.repository.existsFlightCrewMember(crewId);

		status = authorised && assignment != null;
		isLogged = assignment.getCrew().getId() == crewId;

		super.getResponse().setAuthorised(status && isLogged);
	}

	@Override
	public void load() {
		Collection<ActivityLog> activityLogs;
		int crewId;

		crewId = super.getRequest().getPrincipal().getActiveRealm().getId();
		activityLogs = this.repository.findAllActivityLogsByCrewId(crewId);

		super.getBuffer().addData(activityLogs);

	}

	@Override
	public void unbind(final ActivityLog activityLog) {
		Dataset dataset;

		dataset = super.unbindObject(activityLog, "registrationMoment", "typeIncident", "severityLevel", "draftMode");
		dataset.put("flightNumber", activityLog.getAssignment().getLeg().getFlightNumber());

		super.addPayload(dataset, activityLog, "description");
		super.getResponse().addGlobal("id", activityLog.getAssignment().getId());
		super.getResponse().addData(dataset);
	}
}
