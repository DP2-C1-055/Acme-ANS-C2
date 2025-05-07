
package acme.features.crew.activityLog;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.activityLog.ActivityLog;
import acme.entities.assignment.Assignment;
import acme.entities.leg.Leg;
import acme.realms.crew.Crew;

@GuiService
public class CrewActivityLogUpdateService extends AbstractGuiService<Crew, ActivityLog> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CrewActivityLogRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean status;
		int activityLogId;
		ActivityLog activityLog;
		int crewMemberId;
		boolean isCrewMemberValid;
		boolean isActivityLogOwnedByCrewMember;

		activityLogId = super.getRequest().getData("id", int.class);
		activityLog = this.repository.findActivityLogById(activityLogId);
		crewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();

		isCrewMemberValid = this.repository.existsFlightCrewMember(crewMemberId);
		isActivityLogOwnedByCrewMember = isCrewMemberValid && this.repository.thatActivityLogIsOf(activityLogId, crewMemberId);

		status = isActivityLogOwnedByCrewMember && activityLog != null && activityLog.isDraftMode();

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		ActivityLog activityLog;
		int id;

		id = super.getRequest().getData("id", int.class);
		activityLog = this.repository.findActivityLogById(id);

		super.getBuffer().addData(activityLog);
	}

	@Override
	public void bind(final ActivityLog log) {
		super.bindObject(log, "registrationMoment", "typeIncident", "description", "severityLevel");
	}

	@Override
	public void validate(final ActivityLog activityLog) {
		if (activityLog == null)
			return;
		if (!activityLog.isDraftMode())
			super.state(false, "*", "acme.validation.activityLog.assignment-published.message");

		Assignment assignment = this.repository.findAssignmentByActivityLogId(activityLog.getId());
		if (activityLog.getRegistrationMoment() == null || assignment == null)
			return;

		Leg leg = assignment.getLeg();
		if (leg == null || leg.getScheduledArrival() == null)
			return;

		boolean isActivityLogMomentAfterScheduledArrival = this.repository.isAssociatedWithCompletedLeg(activityLog.getId(), MomentHelper.getCurrentMoment());
		super.state(isActivityLogMomentAfterScheduledArrival, "WrongActivityLogDate", "acme.validation.activityLog.wrongMoment.message");
	}

	@Override
	public void perform(final ActivityLog activityLog) {
		activityLog.setRegistrationMoment(MomentHelper.getCurrentMoment());
		activityLog.setDraftMode(true);

		this.repository.save(activityLog);
	}

	@Override
	public void unbind(final ActivityLog activityLog) {
		Dataset dataset;
		SelectChoices selectedAssignments;
		Collection<Assignment> assignments;
		Crew member;

		member = (Crew) super.getRequest().getPrincipal().getActiveRealm();
		assignments = this.repository.findAssignmentPublishedByCrewId(member.getId());
		selectedAssignments = SelectChoices.from(assignments, "leg.flightNumber", activityLog.getAssignment());

		dataset = super.unbindObject(activityLog, "registrationMoment", "typeIncident", "description", "severityLevel", "draftMode");
		dataset.put("assignments", selectedAssignments);
		dataset.put("assignment", selectedAssignments.getSelected().getKey());
		dataset.put("draftMode", activityLog.isDraftMode());
		dataset.put("readonly", false);

		super.getResponse().addData(dataset);
	}

}
