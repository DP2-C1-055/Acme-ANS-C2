
package acme.features.crew.activityLog;

import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.helpers.PrincipalHelper;
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
		boolean status = false;

		if ("POST".equalsIgnoreCase(super.getRequest().getMethod())) {
			int activityLogId = super.getRequest().getData("id", int.class);
			ActivityLog activityLog = this.repository.findActivityLogById(activityLogId);
			int crewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();

			boolean isCrewMemberValid = this.repository.existsCrewMember(crewMemberId);
			boolean isActivityLogOwnedByCrewMember = isCrewMemberValid && this.repository.thatActivityLogIsOf(activityLogId, crewMemberId);

			status = isActivityLogOwnedByCrewMember && activityLog != null && activityLog.isDraftMode();

			if (status) {
				Date registrationMomentClient = super.getRequest().getData("registrationMoment", Date.class);
				Date registrationMomentServer = activityLog.getRegistrationMoment();
				if (registrationMomentClient == null || !registrationMomentClient.equals(registrationMomentServer))
					status = false;
			}
		}

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
		System.out.println("bind draftMode:" + log.isDraftMode());
		super.bindObject(log, "typeIncident", "description", "severityLevel");
	}
	@Override
	public void validate(final ActivityLog activityLog) {
		if (activityLog == null)
			return;
		if (!activityLog.isDraftMode())
			super.state(false, "*", "acme.validation.activityLog.assignment-published.message");

		Assignment assignment = this.repository.findAssignmentByActivityLogId(activityLog.getId());

		if (assignment != null) {
			Leg leg = assignment.getLeg();
			if (leg != null && leg.getScheduledArrival() != null) {
				boolean isActivityLogMomentAfterScheduledArrival = this.repository.isAssociatedWithCompletedLeg(activityLog.getId(), MomentHelper.getCurrentMoment());

				super.state(isActivityLogMomentAfterScheduledArrival, "WrongActivityLogDate", "acme.validation.activityLog.wrongMoment.message");
			}
		}
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

	@Override
	public void onSuccess() {
		if (super.getRequest().getMethod().equals("POST"))
			PrincipalHelper.handleUpdate();
	}

}
