
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
		boolean status = false;
		int assignmentId = super.getRequest().getData("assignmentId", int.class);
		int crewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();

		Assignment assignment = this.repository.findAssignmentById(assignmentId);

		if (assignment != null) {
			boolean isDraftMode = assignment.isDraftMode();
			boolean isCrewMemberAuthorised = this.repository.existsCrewMember(crewMemberId);
			boolean isOwnedByCrewMember = assignment.getCrew().getId() == crewMemberId;
			boolean hasRealmAccess = super.getRequest().getPrincipal().hasRealm(assignment.getCrew());

			status = isDraftMode && isCrewMemberAuthorised && isOwnedByCrewMember && hasRealmAccess;

			if (status && super.getRequest().getMethod().equals("POST")) {
				Date lastUpdateClient = super.getRequest().getData("registrationMoment", Date.class);
				Date lastUpdateServer = MomentHelper.getCurrentMoment();

				if (lastUpdateClient == null || !lastUpdateClient.equals(lastUpdateServer))
					status = false;
			}
		}

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
		activityLog.setDescription("");
		activityLog.setRegistrationMoment(MomentHelper.getCurrentMoment());
		activityLog.setSeverityLevel(0);
		activityLog.setTypeIncident("");

		super.getBuffer().addData(activityLog);
	}

	@Override
	public void bind(final ActivityLog log) {
		super.bindObject(log, "registrationMoment", "typeIncident", "description", "severityLevel");
	}

	@Override
	public void validate(final ActivityLog activityLog) {
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
		int assignmentId;

		member = super.getRequest().getPrincipal().getActiveRealm().getId();
		assignments = this.repository.findAssignmentPublishedByCrewId(member);
		selectedAssignments = SelectChoices.from(assignments, "leg.flightNumber", activityLog.getAssignment());
		assignmentId = super.getRequest().getData("assignmentId", int.class);

		dataset = super.unbindObject(activityLog, "registrationMoment", "typeIncident", "description", "severityLevel", "draftMode");
		dataset.put("id", activityLog.getAssignment().getId());
		dataset.put("assignmentId", assignmentId);
		dataset.put("assignments", selectedAssignments);
		dataset.put("assignment", selectedAssignments.getSelected().getKey());
		dataset.put("draftMode", activityLog.isDraftMode());
		dataset.put("readonly", false);
		dataset.put("masterDraftMode", !this.repository.isAssignmentAlreadyPublishedById(assignmentId));
		super.getResponse().addData(dataset);
	}
}
