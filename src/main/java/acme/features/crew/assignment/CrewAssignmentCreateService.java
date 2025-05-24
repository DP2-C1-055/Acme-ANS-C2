
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
		int crewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();
		boolean authorised1 = this.repository.existsCrewMember(crewMemberId);

		boolean authorised2 = true;
		if (super.getRequest().hasData("leg", int.class)) {
			int legId = super.getRequest().getData("leg", int.class);
			if (legId != 0)
				authorised2 = this.repository.existsByIdAndPublishedTrue(legId);
		}

		boolean authorised = authorised1 && authorised2;
		super.getResponse().setAuthorised(authorised);
	}

	@Override
	public void load() {
		Assignment assignment;
		Crew crew = (Crew) super.getRequest().getPrincipal().getActiveRealm();

		assignment = new Assignment();
		assignment.setCrew(crew);
		assignment.setDraftMode(true);
		assignment.setLastUpdate(MomentHelper.getCurrentMoment());
		super.getBuffer().addData(assignment);
	}

	@Override
	public void bind(final Assignment assignment) {
		super.bindObject(assignment, "duty", "lastUpdate", "currentStatus", "remarks", "leg");
	}

	@Override
	public void validate(final Assignment assignment) {
		Crew crew = assignment.getCrew();
		Leg leg = assignment.getLeg();

		if (crew != null && leg != null)
			if (this.isLegIncompatible(assignment)) {
				super.state(false, "leg", "acme.validation.assignment.legIncompatible.message");
				return;
			}

		if (leg != null)
			this.checkPilotAndCopilotAssignment(assignment);

		boolean legCompleted = this.repository.areLegsCompletedByAssignment(assignment.getId(), MomentHelper.getCurrentMoment());

		if (legCompleted)
			super.state(false, "leg", "acme.validation.assignment.LegAlreadyCompleted.message");
	}

	private boolean isLegIncompatible(final Assignment assignment) {
		Collection<Leg> legsByCrew = this.repository.findLegsByCrewId(assignment.getCrew().getId());
		Leg newLeg = assignment.getLeg();

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
		SelectChoices legChoices;
		SelectChoices statuses;
		SelectChoices duties;

		legs = this.repository.findAllLegs();

		legChoices = SelectChoices.from(legs, "flightNumber", assignment.getLeg());
		statuses = SelectChoices.from(CurrentStatus.class, assignment.getCurrentStatus());
		duties = SelectChoices.from(DutyCrew.class, assignment.getDuty());

		dataset = super.unbindObject(assignment, "duty", "lastUpdate", "currentStatus", "remarks", "crew", "leg");
		dataset.put("confirmation", false);
		dataset.put("readonly", false);
		dataset.put("lastUpdate", MomentHelper.getBaseMoment());
		dataset.put("currentStatus", statuses);
		dataset.put("duty", duties);
		dataset.put("leg", legChoices.getSelected().getKey());
		dataset.put("legs", legChoices);

		super.getResponse().addData(dataset);
	}
}
