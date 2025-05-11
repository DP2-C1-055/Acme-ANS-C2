
package acme.features.crew.assignment;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
@Service
public class CrewAssignmentCreateService extends AbstractGuiService<Crew, Assignment> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CrewAssignmentRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		int crewId;
		boolean isAuthorised;

		crewId = super.getRequest().getPrincipal().getActiveRealm().getId();
		isAuthorised = this.repository.existsCrewMember(crewId);

		super.getResponse().setAuthorised(isAuthorised);
	}

	@Override
	public void load() {
		Assignment assignment;
		int crewId;

		crewId = super.getRequest().getPrincipal().getActiveRealm().getId();

		assignment = new Assignment();

		assignment.setDraftMode(true);
		assignment.setCrew(this.repository.findCrewById(crewId));
		assignment.setLastUpdate(MomentHelper.getCurrentMoment());
		assignment.setRemarks("");
		assignment.setCurrentStatus(CurrentStatus.PENDING);
		assignment.setDuty(DutyCrew.CABIN_ATTENDANT);

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
		// Extracting necessary data
		Crew crew = assignment.getCrew();
		Leg leg = assignment.getLeg();

		// Check if crew and leg are assigned and if leg is compatible
		if (crew != null && leg != null)
			if (this.isLegIncompatible(assignment)) {
				super.state(false, "leg", "acme.validation.assignment.legIncompatible.message");
				return;
			}

		// Check if the duty assignment is valid (Pilot, Co-pilot)
		if (leg != null)
			this.checkPilotAndCopilotAssignment(assignment);

		// Check if the leg is already completed
		boolean legCompleted = this.repository.areLegsCompletedByAssignment(assignment.getId(), MomentHelper.getCurrentMoment());

		if (legCompleted)
			super.state(false, "leg", "acme.validation.assignment.LegAlreadyCompleted.message");
	}

	private boolean isLegIncompatible(final Assignment assignment) {
		// Retrieve all legs for the assigned crew member
		Collection<Leg> legsByCrew = this.repository.findLegsByCrewId(assignment.getCrew().getId());
		Leg newLeg = assignment.getLeg();

		// Check if any of the legs overlap with the new leg
		return legsByCrew.stream()
			.anyMatch(existingLeg -> MomentHelper.isInRange(newLeg.getScheduledDeparture(), existingLeg.getScheduledDeparture(), existingLeg.getScheduledArrival())
				|| MomentHelper.isInRange(newLeg.getScheduledArrival(), existingLeg.getScheduledDeparture(), existingLeg.getScheduledArrival())
				|| newLeg.getScheduledDeparture().before(existingLeg.getScheduledDeparture()) && newLeg.getScheduledArrival().after(existingLeg.getScheduledArrival()));
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
		Collection<Leg> legs;
		Collection<Crew> crewMembers;
		SelectChoices legChoices;
		SelectChoices crewMembersChoices;
		SelectChoices statuses;
		SelectChoices duties;

		legs = this.repository.findAllLegs();
		crewMembers = this.repository.findCrewByAvailability(AvailabilityStatus.AVAILABLE);

		legChoices = SelectChoices.from(legs, "flightNumber", assignment.getLeg());
		crewMembersChoices = SelectChoices.from(crewMembers, "code", assignment.getCrew());
		statuses = SelectChoices.from(CurrentStatus.class, assignment.getCurrentStatus());
		duties = SelectChoices.from(DutyCrew.class, assignment.getDuty());

		dataset = super.unbindObject(assignment, "duty", "lastUpdate", "currentStatus", "remarks", "draftMode");
		dataset.put("confirmation", false);
		dataset.put("readonly", false);
		dataset.put("lastUpdate", MomentHelper.getBaseMoment());
		dataset.put("currentStatus", statuses);
		dataset.put("duty", duties);
		dataset.put("leg", legChoices.getSelected().getKey());
		dataset.put("legs", legChoices);
		dataset.put("crewMember", crewMembersChoices.getSelected().getKey());
		dataset.put("crewMembers", crewMembersChoices);

		super.getResponse().addData(dataset);
	}
}
