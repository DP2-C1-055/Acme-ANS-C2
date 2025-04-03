
package acme.features.crew.activityLog;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.activityLog.ActivityLog;
import acme.realms.crew.Crew;

@GuiService
public class CrewActivityLogListService extends AbstractGuiService<Crew, ActivityLog> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CrewActivityLogRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		//		Collection<ActivityLog> activityLogs;
		//		int assignmentId;
		//		boolean status;
		//
		//		assignmentId = super.getRequest().getPrincipal().getActiveRealm().getId();
		//		activityLogs = this.repository.findActivityLogsByAssignmentId(assignmentId);
		//
		//		ActivityLog activityLog = this.repository.findActivityLogById(super.getRequest().getData("assignmentId", int.class));
		//
		//		status = super.getRequest().getPrincipal().hasRealmOfType(Crew.class) && activityLogs.contains(activityLog);
		//
		//		super.getBuffer().addData(status);
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		Collection<ActivityLog> activityLogs;
		int crewId;

		crewId = super.getRequest().getPrincipal().getActiveRealm().getId();
		activityLogs = this.repository.findAllActivityLogs(crewId);

		super.getBuffer().addData(activityLogs);

	}

	//	@Override
	//	public void unbind(final ActivityLog activityLog) {
	//		Dataset dataset;
	//		int assignmentId;
	//		Assignment assignment;
	//		boolean showingCreate;
	//		boolean correctCrewMember;
	//		int memberId;
	//		int userId;
	//
	//		assignmentId = super.getRequest().getData("assignmentId", int.class);
	//		assignment = this.repository.findAssignmentById(assignmentId);
	//		userId = super.getRequest().getPrincipal().getActiveRealm().getId();
	//		memberId = assignment.getCrew().getId();
	//
	//		correctCrewMember = memberId == userId;
	//
	//		showingCreate = !assignment.isDraftMode() && correctCrewMember;
	//
	//		dataset = super.unbindObject(activityLog, "typeIncident", "severityLevel", "registrationMoment");
	//		dataset.put("flightNumber", activityLog.getAssignment().getLeg().getFlightNumber());
	//
	//		super.getResponse().addGlobal("assignmentId", assignmentId);
	//		super.getResponse().addGlobal("showingCreate", showingCreate);
	//
	//		super.addPayload(dataset, activityLog, "description");
	//		super.getResponse().addData(dataset);
	//	}
	@Override
	public void unbind(final ActivityLog activityLog) {
		Dataset dataset;

		dataset = super.unbindObject(activityLog, "registrationMoment", "typeIncident", "severityLevel", "draftMode");
		dataset.put("flightNumber", activityLog.getAssignment().getLeg().getFlightNumber());

		super.addPayload(dataset, activityLog, "description");
		super.getResponse().addData(dataset);
	}
}
