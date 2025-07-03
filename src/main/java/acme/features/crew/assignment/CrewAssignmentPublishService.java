
package acme.features.crew.assignment;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.assignment.Assignment;
import acme.entities.assignment.CurrentStatus;
import acme.entities.assignment.DutyCrew;
import acme.entities.leg.Leg;
import acme.realms.crew.AvailabilityStatus;
import acme.realms.crew.Crew;

@GuiService
public class CrewAssignmentPublishService extends AbstractGuiService<Crew, Assignment> {

	@Autowired
	private CrewAssignmentRepository repository;


	@Override
	public void authorise() {
		boolean status = false;
		int assignmentId;
		Assignment assignment = null;
		int crewMemberId;
		String method;

		crewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();
		assignmentId = super.getRequest().getData("id", int.class);
		assignment = this.repository.findAssignmentById(assignmentId);

		if (assignment != null) {
			boolean legExists = assignment.getLeg() != null;

			boolean isCrewMemberValid = this.repository.existsCrewMember(crewMemberId);
			boolean isAssignmentOwnedByCrewMember = this.repository.isAssignmentOwnedByCrewMember(assignmentId, crewMemberId);
			boolean isDraftMode = assignment.isDraftMode();
			boolean isFutureScheduledArrival = legExists && MomentHelper.isFuture(assignment.getLeg().getScheduledArrival());
			boolean isAssignmentOwnedByCurrentCrewMember = assignment.getCrew() != null && assignment.getCrew().getId() == crewMemberId;
			boolean isLegPublished = legExists && !assignment.getLeg().isDraftMode();

			status = isCrewMemberValid && isAssignmentOwnedByCrewMember && isDraftMode && isFutureScheduledArrival && isAssignmentOwnedByCurrentCrewMember && isLegPublished;

			method = super.getRequest().getMethod();
			if ("POST".equals(method) && status) {
				// Validación de duty
				String dutyStatus = super.getRequest().getData("duty", String.class);
				if (!"0".equals(dutyStatus))
					status = status && Arrays.stream(DutyCrew.values()).anyMatch(tc -> tc.name().equalsIgnoreCase(dutyStatus));

				// Validación de currentStatus
				String currentStatus = super.getRequest().getData("currentStatus", String.class);
				if (!"0".equals(currentStatus))
					status = status && Arrays.stream(CurrentStatus.values()).anyMatch(tc -> tc.name().equalsIgnoreCase(currentStatus));

				// Validación de leg
				int legId = super.getRequest().getData("leg", int.class);
				Leg leg = this.repository.findLegById(legId);
				if (legId != 0 && leg == null)
					status = false;

				// Validación de lastUpdate
				Date lastUpdateClient = super.getRequest().getData("lastUpdate", Date.class);
				Date lastUpdateServer = assignment.getLastUpdate();
				if (lastUpdateClient == null || !lastUpdateClient.equals(lastUpdateServer))
					status = false;
			}

		}

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		int assignmentId = super.getRequest().getData("id", int.class);
		Assignment assignment = this.repository.findAssignmentById(assignmentId);
		super.getBuffer().addData(assignment);
	}

	@Override
	public void bind(final Assignment assignment) {
		Integer legId;
		Leg leg;

		legId = super.getRequest().getData("leg", int.class);
		leg = this.repository.findLegById(legId);

		super.bindObject(assignment, "duty", "currentStatus", "remarks");
		assignment.setLeg(leg);
	}

	@Override
	public void validate(final Assignment assignment) {
		Assignment original = this.repository.findAssignmentById(assignment.getId());
		Crew crew = assignment.getCrew();
		Leg leg = assignment.getLeg();

		boolean cambioDuty = !original.getDuty().equals(assignment.getDuty());
		boolean cambioLeg = !original.getLeg().equals(assignment.getLeg());
		boolean cambioMoment = !original.getLastUpdate().equals(assignment.getLastUpdate());
		boolean cambioStatus = !original.getCurrentStatus().equals(assignment.getCurrentStatus());

		if (!(cambioDuty || cambioLeg || cambioMoment || cambioStatus))
			return;

		if (crew != null && leg != null && cambioLeg && !this.isLegCompatible(assignment))
			super.state(false, "crew", "acme.validation.assignment.CrewIncompatibleLegs.message");

		if (leg != null && (cambioDuty || cambioLeg))
			this.checkPilotAndCopilotAssignment(assignment);

		if (leg != null && cambioLeg) {
			boolean legCompleted = this.repository.areLegsCompletedByAssignment(assignment.getId(), MomentHelper.getCurrentMoment());
			if (legCompleted)
				super.state(false, "leg", "acme.validation.assignment.LegAlreadyCompleted.message");
		}
	}

	private boolean isLegCompatible(final Assignment assignment) {
		Collection<Leg> legsByCrew = this.repository.findLegsByCrewId(assignment.getCrew().getId());
		Leg newLeg = assignment.getLeg();

		return legsByCrew.stream().allMatch(existingLeg -> this.areLegsCompatible(newLeg, existingLeg));
	}

	private boolean areLegsCompatible(final Leg newLeg, final Leg oldLeg) {
		return !(MomentHelper.isInRange(newLeg.getScheduledDeparture(), oldLeg.getScheduledDeparture(), oldLeg.getScheduledArrival()) || MomentHelper.isInRange(newLeg.getScheduledArrival(), oldLeg.getScheduledDeparture(), oldLeg.getScheduledArrival())
			|| newLeg.getScheduledDeparture().before(oldLeg.getScheduledDeparture()) && newLeg.getScheduledArrival().after(oldLeg.getScheduledArrival()));
	}

	private void checkPilotAndCopilotAssignment(final Assignment assignment) {
		boolean havePilot = this.repository.existsCrewWithDutyInLeg(assignment.getLeg().getId(), DutyCrew.PILOT);
		boolean haveCopilot = this.repository.existsCrewWithDutyInLeg(assignment.getLeg().getId(), DutyCrew.CO_PILOT);

		if (DutyCrew.PILOT.equals(assignment.getDuty()))
			super.state(!havePilot, "duty", "acme.validation.assignment.havePilot.message");

		if (DutyCrew.CO_PILOT.equals(assignment.getDuty()))
			super.state(!haveCopilot, "duty", "acme.validation.assignment.haveCopilot.message");
	}

	@Override
	public void perform(final Assignment assignment) {
		Assignment original = this.repository.findAssignmentById(assignment.getId());
		boolean change = false;

		boolean changeCrewMember = !original.getCrew().equals(assignment.getCrew());
		boolean changeDuty = !original.getDuty().equals(assignment.getDuty());
		boolean changeLeg = !original.getLeg().equals(assignment.getLeg());
		boolean changeStatus = !original.getCurrentStatus().equals(assignment.getCurrentStatus());

		boolean changeRemarks = original.getRemarks() != null ? !original.getRemarks().equals(assignment.getRemarks()) : assignment.getRemarks() != null;

		change = changeDuty || changeCrewMember || changeLeg || changeStatus || changeRemarks;

		if (change)
			assignment.setLastUpdate(MomentHelper.getCurrentMoment());
		assignment.setDraftMode(false);

		this.repository.save(assignment);
	}

	@Override
	public void unbind(final Assignment assignment) {
		Dataset dataset;
		SelectChoices statuses;
		SelectChoices duties;
		Collection<Leg> legs;
		SelectChoices legChoices;
		boolean isCompleted;
		int assignmentId;

		assignmentId = super.getRequest().getData("id", int.class);
		Date currentMoment = MomentHelper.getCurrentMoment();
		isCompleted = this.repository.areLegsCompletedByAssignment(assignmentId, currentMoment);

		Collection<Crew> crewMembers = this.repository.findCrewByAvailability(AvailabilityStatus.AVAILABLE);
		SelectChoices crewMemberChoices = SelectChoices.from(crewMembers, "code", assignment.getCrew());

		legs = this.repository.findAllLegs();
		legChoices = SelectChoices.from(legs, "flightNumber", assignment.getLeg());

		statuses = SelectChoices.from(CurrentStatus.class, assignment.getCurrentStatus());
		duties = SelectChoices.from(DutyCrew.class, assignment.getDuty());

		dataset = super.unbindObject(assignment, "duty", "lastUpdate", "currentStatus", "remarks", "draftMode");
		dataset.put("readonly", false);
		dataset.put("lastUpdate", assignment.getLastUpdate());
		dataset.put("currentStatus", statuses);
		dataset.put("duty", duties);
		dataset.put("leg", legChoices.getSelected().getKey());
		dataset.put("legs", legChoices);
		dataset.put("crewMember", crewMemberChoices.getSelected().getKey());
		dataset.put("crewMembers", crewMemberChoices);
		dataset.put("isCompleted", isCompleted);
		dataset.put("draftMode", assignment.isDraftMode());

		super.getResponse().addData(dataset);
	}

}
