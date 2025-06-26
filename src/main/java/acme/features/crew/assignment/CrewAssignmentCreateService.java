
package acme.features.crew.assignment;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

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

		boolean status = super.getRequest().getPrincipal().hasRealmOfType(Crew.class);

		if (status && "POST".equals(super.getRequest().getMethod()) && super.getRequest().hasData("leg", Integer.class)) {

			String dutyStatus = super.getRequest().getData("duty", String.class);
			if (!"0".equals(dutyStatus))
				status = status && Arrays.stream(DutyCrew.values()).anyMatch(tc -> tc.name().equalsIgnoreCase(dutyStatus));

			String currentStatus = super.getRequest().getData("currentStatus", String.class);
			if (!"0".equals(currentStatus))
				status = status && Arrays.stream(CurrentStatus.values()).anyMatch(tc -> tc.name().equalsIgnoreCase(currentStatus));

			Integer legId = super.getRequest().getData("leg", Integer.class);
			Leg leg = this.repository.findLegById(legId);

			if (leg == null || leg.isDraftMode() || !leg.getScheduledDeparture().after(MomentHelper.getCurrentMoment()))
				status = false;

			Date lastUpdateClient = super.getRequest().getData("lastUpdate", Date.class);
			Date lastUpdateExpected = MomentHelper.getBaseMoment();

			if (lastUpdateClient == null || !lastUpdateClient.equals(lastUpdateExpected))
				status = false;
		}

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Crew crew = (Crew) super.getRequest().getPrincipal().getActiveRealm();
		Assignment assignment = new Assignment();

		assignment.setCrew(crew);
		assignment.setDraftMode(true);
		assignment.setLastUpdate(MomentHelper.getCurrentMoment());

		super.getBuffer().addData(assignment);
	}

	@Override
	public void bind(final Assignment assignment) {
		Integer legId = super.getRequest().getData("leg", int.class);
		Leg leg = this.repository.findLegById(legId);

		super.bindObject(assignment, "duty", "currentStatus", "remarks");

		assignment.setLeg(leg);
	}

	@Override
	public void validate(final Assignment assignment) {
		Crew crew = assignment.getCrew();
		Leg leg = assignment.getLeg();

		/* Crew debe estar AVAILABLE */
		if (crew != null && crew.getAvailability() != AvailabilityStatus.AVAILABLE)
			super.state(false, "crew", "acme.validation.assignment.crewUnavailable.message");

		/* La leg no puede estar en el pasado */
		if (leg != null && leg.getScheduledDeparture().before(MomentHelper.getCurrentMoment()))
			super.state(false, "leg", "acme.validation.assignment.LegAlreadyCompleted.message");

		/* No puede solaparse con otras legs del mismo crew */
		if (crew != null && leg != null && this.isLegIncompatible(assignment))
			super.state(false, "leg", "acme.validation.assignment.legIncompatible.message");

		/* Regla piloto / copiloto únicos */
		if (leg != null)
			this.checkPilotAndCopilotAssignment(assignment);
	}

	// ------------------------------------------------------------------------
	// Helper methods
	// ------------------------------------------------------------------------

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
		Collection<Leg> legs = this.repository.findAllPublishedFutureLegs(MomentHelper.getCurrentMoment());
		SelectChoices legChoices = SelectChoices.from(legs, "flightNumber", assignment.getLeg());
		SelectChoices statusChoices = SelectChoices.from(CurrentStatus.class, assignment.getCurrentStatus());
		SelectChoices dutyChoices = SelectChoices.from(DutyCrew.class, assignment.getDuty());
		Dataset dataset = super.unbindObject(assignment, "duty", "lastUpdate", "currentStatus", "remarks", "crew", "leg");

		dataset.put("confirmation", false);
		dataset.put("readonly", false);
		dataset.put("lastUpdate", MomentHelper.getBaseMoment());
		dataset.put("currentStatus", statusChoices);
		dataset.put("duty", dutyChoices);
		dataset.put("leg", legChoices.getSelected().getKey());
		dataset.put("legs", legChoices);

		super.getResponse().addData(dataset);
	}
}
