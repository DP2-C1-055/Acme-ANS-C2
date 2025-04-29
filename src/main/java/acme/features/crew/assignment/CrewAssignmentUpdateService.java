
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

	// Internal state ---------------------------------------------------------

	@Autowired
	private CrewAssignmentRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		int assignmentId = super.getRequest().getData("id", int.class);
		Assignment assignment = this.repository.findAssignmentById(assignmentId);

		Crew member = assignment == null ? null : assignment.getCrew();
		boolean status = assignment != null && assignment.isDraftMode() && super.getRequest().getPrincipal().hasRealm(member);

		super.getResponse().setAuthorised(status);
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

		legId = super.getRequest().getData("leg", int.class);
		leg = this.repository.findLegById(legId);

		super.bindObject(assignment, "duty", "currentStatus", "remarks");
		assignment.setLeg(leg);
	}

	@Override
	public void validate(final Assignment assignment) {

		// Validación si el Leg ha ocurrido
		Date now = MomentHelper.getCurrentMoment();
		boolean hasOccurred = now.after(assignment.getLeg().getScheduledArrival());
		if (hasOccurred)
			super.state(false, "*", "acme.validation.assignment.leg-has-occurred.message"); // El leg ya ha ocurrido.

		// Validación de disponibilidad del miembro
		boolean available = assignment.getCrew().getAvailability().equals(AvailabilityStatus.AVAILABLE);
		if (!available)
			super.state(false, "*", "acme.validation.assignment.member-not-available.message"); // El miembro no está disponible.

		// Validación de compatibilidad del Leg
		if (assignment.getLeg() == null)
			super.state(false, "*", "acme.validation.assignment.leg-null.message"); // The leg cannot be null.

		Collection<Leg> existingLegs = this.repository.findLegsByCrewId(assignment.getCrew().getId());
		boolean hasIncompatibleLeg = existingLegs.stream().anyMatch(existingLeg -> this.legIsNotOverlapping(assignment.getLeg(), existingLeg));
		if (hasIncompatibleLeg)
			super.state(false, "*", "acme.validation.assignment.member-with-overlapping-legs.message"); // El miembro ya está asignado a otro leg.

		// Validación de Duty Assignment, solo si el duty no es null
		if (assignment.getDuty() != null) {
			Collection<Assignment> assignedDuties = this.repository.findAssignmentByLegId(assignment.getLeg().getId());

			boolean legWithCopilot = assignedDuties.stream().map(Assignment::getDuty).anyMatch(duty -> duty.equals(DutyCrew.CO_PILOT));
			boolean legWithPilot = assignedDuties.stream().map(Assignment::getDuty).anyMatch(duty -> duty.equals(DutyCrew.PILOT));

			super.state(!(assignment.getDuty().equals(DutyCrew.PILOT) && legWithPilot), "*", "acme.validation.assignment.leg-has-pilot.message");
			super.state(!(assignment.getDuty().equals(DutyCrew.CO_PILOT) && legWithCopilot), "*", "acme.validation.assignment.leg-has-copilot.message");
		} else
			super.state(false, "*", "acme.validation.assignment.duty-null.message"); // El duty es nulo y no puede ser asignado.
	}

	private boolean legIsNotOverlapping(final Leg newLeg, final Leg existingLeg) {
		boolean isDepartureOverlapping = MomentHelper.isInRange(newLeg.getScheduledDeparture(), existingLeg.getScheduledDeparture(), existingLeg.getScheduledArrival());
		boolean isArrivalOverlapping = MomentHelper.isInRange(newLeg.getScheduledArrival(), existingLeg.getScheduledDeparture(), existingLeg.getScheduledArrival());
		return isDepartureOverlapping && isArrivalOverlapping;
	}

	@Override
	public void perform(final Assignment assignment) {
		this.repository.save(assignment);
	}

	@Override
	public void unbind(final Assignment assignment) {
		Dataset dataset;
		SelectChoices statuses;
		SelectChoices duties;
		Collection<Leg> legs;
		SelectChoices selectedLegs;

		legs = this.repository.findAllLegs();

		statuses = SelectChoices.from(CurrentStatus.class, assignment.getCurrentStatus());
		duties = SelectChoices.from(DutyCrew.class, assignment.getDuty());
		selectedLegs = SelectChoices.from(legs, "flightNumber", assignment.getLeg());

		dataset = super.unbindObject(assignment, "duty", "lastUpdate", "currentStatus", "remarks", "draftMode");
		dataset.put("statuses", statuses);
		dataset.put("duties", duties);
		dataset.put("leg", selectedLegs.getSelected().getKey());
		dataset.put("legs", selectedLegs);

		super.getResponse().addData(dataset);
	}

}
