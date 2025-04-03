
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
		ActivityLog activityLog;
		Date registrationMoment;
		registrationMoment = MomentHelper.getCurrentMoment();

		int assignmentId;
		Assignment assignment;

		assignmentId = super.getRequest().getData("assignmentId", int.class);
		assignment = this.repository.findAssignmentById(assignmentId);

		activityLog = new ActivityLog();
		activityLog.setRegistrationMoment(registrationMoment);
		activityLog.setTypeIncident("");
		activityLog.setDescription("");
		activityLog.setDraftMode(true);
		activityLog.setAssignment(assignment);

		super.getBuffer().addData(activityLog);
	}

	@Override
	public void bind(final ActivityLog log) {
		Date now;
		int assignmentId;
		Assignment assignment;

		assignmentId = super.getRequest().getData("assignmentId", int.class);
		assignment = this.repository.findAssignmentById(assignmentId);
		now = MomentHelper.getCurrentMoment();

		super.bindObject(log, "typeIncident", "description", "severityLevel");
		log.setRegistrationMoment(now);
		log.setAssignment(assignment);
	}

	@Override
	public void validate(final ActivityLog activityLog) {
		if (!activityLog.getAssignment().isDraftMode())
			super.state(false, "*", "acme.validation.activityLog.assignment-published.message");

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
		Crew member;

		member = (Crew) super.getRequest().getPrincipal().getActiveRealm();
		assignments = this.repository.findAssignmentsByCrewId(member.getId());
		selectedAssignments = SelectChoices.from(assignments, "leg.flightNumber", activityLog.getAssignment());

		dataset = super.unbindObject(activityLog, "registrationMoment", "typeIncident", "description", "severityLevel", "draftMode");
		dataset.put("assignments", selectedAssignments);
		dataset.put("assignment", selectedAssignments.getSelected().getKey());
		dataset.put("assignmentId", super.getRequest().getData("assignmentId", int.class));

		super.getResponse().addData(dataset);
	}
}
