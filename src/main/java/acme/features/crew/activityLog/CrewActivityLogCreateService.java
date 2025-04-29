
package acme.features.crew.activityLog;

import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.activityLog.ActivityLog;
import acme.entities.assignment.Assignment;
import acme.realms.crew.Crew;

@GuiService
public class CrewActivityLogCreateService extends AbstractGuiService<Crew, ActivityLog> {

	@Autowired
	private CrewActivityLogRepository repository;


	@Override
	public void authorise() {
		boolean status;
		int assignmentId;

		assignmentId = super.getRequest().getData("assignmentId", int.class);
		Assignment assignment = this.repository.findAssignmentById(assignmentId);
		Crew member = assignment == null ? null : assignment.getCrew();
		status = assignment != null && assignment.isDraftMode() && super.getRequest().getPrincipal().hasRealm(member);

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		ActivityLog activityLog;
		int assignmentId;
		Assignment assignment;

		assignmentId = super.getRequest().getData("assignmentId", int.class);
		assignment = this.repository.findAssignmentById(assignmentId);

		activityLog = new ActivityLog();
		activityLog.setAssignment(assignment);
		activityLog.setDraftMode(true);
		activityLog.setRegistrationMoment(MomentHelper.getCurrentMoment());

		super.getBuffer().addData(activityLog);
	}

	@Override
	public void bind(final ActivityLog log) {
		super.bindObject(log, "typeIncident", "description", "severityLevel");
	}

	@Override
	public void validate(final ActivityLog activityLog) {
		Date now = MomentHelper.getCurrentMoment();
		if (MomentHelper.isBefore(now, activityLog.getAssignment().getLeg().getScheduledArrival()))
			super.state(false, "*", "El momento de registro del registro debe ocurrir después de que termine la escala");
		;
	}

	@Override
	public void perform(final ActivityLog activityLog) {
		this.repository.save(activityLog);
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
		dataset.put("id", activityLog.getAssignment().getId());
		//dataset.put("assignmentId", activityLog.getAssignment().getId());
		dataset.put("assignments", selectedAssignments);
		dataset.put("assignment", selectedAssignments.getSelected().getKey());
		super.getResponse().addData(dataset);
	}
}
