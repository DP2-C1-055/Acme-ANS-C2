
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
import acme.entities.leg.Leg;
import acme.realms.crew.Crew;

@GuiService
public class CrewActivityLogPublishService extends AbstractGuiService<Crew, ActivityLog> {

	@Autowired
	private CrewActivityLogRepository repository;


	@Override
	public void authorise() {
		boolean status = false;

		if ("POST".equalsIgnoreCase(super.getRequest().getMethod())) {
			int activityLogId = super.getRequest().getData("id", int.class);
			ActivityLog activityLog = this.repository.findActivityLogById(activityLogId);
			int crewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();

			boolean isActivityLogOwnedByCrewMember = this.repository.thatActivityLogIsOf(activityLogId, crewMemberId);
			boolean isCrewMemberValid = this.repository.existsCrewMember(crewMemberId) && isActivityLogOwnedByCrewMember;

			status = isCrewMemberValid && activityLog != null && activityLog.isDraftMode();

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
		ActivityLog log;
		int logId;

		logId = super.getRequest().getData("id", int.class);
		log = this.repository.findActivityLogById(logId);

		super.getBuffer().addData(log);
	}

	@Override
	public void bind(final ActivityLog log) {
		super.bindObject(log, "typeIncident", "description", "severityLevel");

	}

	@Override
	public void validate(final ActivityLog activityLog) {
		if (activityLog == null)
			return;

		Assignment assignment = this.repository.findAssignmentByActivityLogId(activityLog.getId());
		if (assignment == null)
			return;

		Leg leg = assignment.getLeg();
		if (leg == null || leg.getScheduledArrival() == null)
			return;

		Date registrationMoment = activityLog.getRegistrationMoment();
		if (registrationMoment == null)
			return;

		// Verificar que el registrationMoment es posterior a la llegada programada
		boolean isMomentValid = this.repository.isAssociatedWithCompletedLeg(activityLog.getId(), registrationMoment);
		super.state(isMomentValid, "WrongActivityLogDate", "acme.validation.activityLog.wrongMoment.message");

		// Verificar que el assignment no está en draft mode (está publicado)
		boolean assignmentIsPublished = !assignment.isDraftMode();
		super.state(assignmentIsPublished, "activityLog", "acme.validation.activityLog.assignmentNotPublished.message");

		// Verificar que el vuelo ya terminó (fecha actual posterior a scheduledArrival)
		Date now = MomentHelper.getCurrentMoment();
		super.state(!now.before(leg.getScheduledArrival()), "*", "acme.validation.activityLog.legNotFinished.message");
	}

	@Override
	public void perform(final ActivityLog log) {
		ActivityLog oldLog = this.repository.findActivityLogById(log.getId());
		boolean hasChanged = !oldLog.getDescription().equals(log.getDescription()) || // 
			oldLog.getSeverityLevel() != log.getSeverityLevel() || //
			oldLog.getTypeIncident() != log.getTypeIncident();

		if (hasChanged)
			log.setRegistrationMoment(MomentHelper.getCurrentMoment());
		log.setDraftMode(false);
		this.repository.save(log);
	}

	@Override
	public void unbind(final ActivityLog log) {
		Dataset dataset;
		SelectChoices selectedAssignments;
		Collection<Assignment> assignments;
		Crew member;

		member = (Crew) super.getRequest().getPrincipal().getActiveRealm();
		assignments = this.repository.findAssignmentPublishedByCrewId(member.getId());
		selectedAssignments = SelectChoices.from(assignments, "leg.flightNumber", log.getAssignment());

		dataset = super.unbindObject(log, "registrationMoment", "typeIncident", "description", "severityLevel", "draftMode");
		dataset.put("assignments", selectedAssignments);
		dataset.put("assignment", selectedAssignments.getSelected().getKey());
		dataset.put("id", super.getRequest().getData("id", int.class));
		dataset.put("draftMode", log.isDraftMode());
		dataset.put("readonly", false);

		super.getResponse().addData(dataset);
	}

}
