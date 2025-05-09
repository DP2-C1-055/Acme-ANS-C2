
package acme.features.crew.assignment;

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
public class CrewAssignmentUpdateService extends AbstractGuiService<Crew, Assignment> {

	@Autowired
	private CrewAssignmentRepository repository;


	@Override
	public void authorise() {
		int currentCrewMemberId;
		int assignmentId;
		Assignment assignment;
		boolean crewMemberExists;
		boolean assignmentBelongsToCrewMember;
		boolean isAssignmentOwner;
		boolean isLeadAttendant;

		currentCrewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();

		assignmentId = super.getRequest().getData("id", int.class);
		assignment = this.repository.findAssignmentById(assignmentId);

		crewMemberExists = this.repository.existsCrewMember(currentCrewMemberId);

		assignmentBelongsToCrewMember = crewMemberExists && this.repository.isAssignmentOwnedByCrewMember(assignmentId, currentCrewMemberId);

		isAssignmentOwner = assignment.getCrew().getId() == currentCrewMemberId;

		isLeadAttendant = this.repository.existsAssignmentWithDuty(currentCrewMemberId, DutyCrew.LEAD_ATTENDANT);

		super.getResponse().setAuthorised(assignmentBelongsToCrewMember && isAssignmentOwner && isLeadAttendant);
	}

	@Override
	public void load() {
		Assignment assignment;
		int assignmentId;

		assignmentId = super.getRequest().getData("id", int.class);
		assignment = this.repository.findAssignmentById(assignmentId);

		super.getBuffer().addData(assignment);
	}

	@Override
	public void bind(final Assignment assignment) {
		Integer legId;
		Leg leg;
		Crew member;
		Integer crewId;

		legId = super.getRequest().getData("leg", int.class);
		leg = this.repository.findLegById(legId);

		crewId = super.getRequest().getData("crewMember", int.class);
		member = this.repository.findCrewById(crewId);

		super.bindObject(assignment, "duty", "currentStatus", "remarks");
		assignment.setLeg(leg);
		assignment.setCrew(member);
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
		assignment.setLastUpdate(MomentHelper.getCurrentMoment());
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
		Date currentMoment;

		Collection<Crew> crewMembers;
		SelectChoices crewMemberChoices;

		assignmentId = super.getRequest().getData("id", int.class);
		currentMoment = MomentHelper.getCurrentMoment();
		isCompleted = this.repository.areLegsCompletedByAssignment(assignmentId, currentMoment);

		legs = this.repository.findAllLegs();
		crewMembers = this.repository.findCrewByAvailability(AvailabilityStatus.AVAILABLE);

		legChoices = SelectChoices.from(legs, "flightNumber", assignment.getLeg());
		crewMemberChoices = SelectChoices.from(crewMembers, "code", assignment.getCrew());

		statuses = SelectChoices.from(CurrentStatus.class, assignment.getCurrentStatus());
		duties = SelectChoices.from(DutyCrew.class, assignment.getDuty());

		dataset = super.unbindObject(assignment, "duty", "lastUpdate", "currentStatus", "remarks", "draftMode");
		dataset.put("readonly", false);
		dataset.put("lastUpdate", MomentHelper.getCurrentMoment());
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
