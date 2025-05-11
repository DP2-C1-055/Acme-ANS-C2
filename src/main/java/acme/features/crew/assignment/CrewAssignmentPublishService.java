
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
public class CrewAssignmentPublishService extends AbstractGuiService<Crew, Assignment> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CrewAssignmentRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean status;
		int assignmentId;
		Assignment assignment;
		int crewMemberId;
		boolean isAssignmentOwnedByCrewMember;
		boolean isCrewMemberValid;
		boolean isDraftMode;
		boolean isFutureScheduledArrival;
		boolean isAssignmentOwnedByCurrentCrewMember;
		boolean isLeadAttendant;

		crewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();
		assignmentId = super.getRequest().getData("id", int.class);
		assignment = this.repository.findAssignmentById(assignmentId);

		isCrewMemberValid = this.repository.existsCrewMember(crewMemberId);
		isAssignmentOwnedByCrewMember = this.repository.isAssignmentOwnedByCrewMember(assignmentId, crewMemberId);
		isDraftMode = assignment != null && assignment.isDraftMode();
		isFutureScheduledArrival = assignment != null && MomentHelper.isFuture(assignment.getLeg().getScheduledArrival());
		isAssignmentOwnedByCurrentCrewMember = assignment != null && assignment.getCrew().getId() == crewMemberId;
		isLeadAttendant = this.repository.existsAssignmentWithDuty(crewMemberId, DutyCrew.LEAD_ATTENDANT);

		status = isCrewMemberValid && isAssignmentOwnedByCrewMember && isDraftMode && isFutureScheduledArrival;

		super.getResponse().setAuthorised(status && isAssignmentOwnedByCurrentCrewMember && isLeadAttendant);
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
		boolean cambioCrew = !original.getCrew().equals(crew);
		boolean cambioDuty = !original.getDuty().equals(assignment.getDuty());
		boolean cambioLeg = !original.getLeg().equals(assignment.getLeg());
		boolean cambioMoment = !original.getLastUpdate().equals(assignment.getLastUpdate());
		boolean cambioStatus = !original.getCurrentStatus().equals(assignment.getCurrentStatus());

		if (!(cambioDuty || cambioLeg || cambioMoment || cambioStatus))
			return;

		// Validación de compatibilidad del Leg
		if (crew != null && leg != null && cambioLeg) {
			Collection<Leg> legsByCrew = this.repository.findLegsByCrewId(crew.getId());
			Leg newLeg = assignment.getLeg();

			boolean hasIncompatibleLeg = legsByCrew.stream()
				.anyMatch(existingLeg -> MomentHelper.isInRange(newLeg.getScheduledDeparture(), existingLeg.getScheduledDeparture(), existingLeg.getScheduledArrival())
					|| MomentHelper.isInRange(newLeg.getScheduledArrival(), existingLeg.getScheduledDeparture(), existingLeg.getScheduledArrival())
					|| newLeg.getScheduledDeparture().before(existingLeg.getScheduledDeparture()) && newLeg.getScheduledArrival().after(existingLeg.getScheduledArrival()));

			if (hasIncompatibleLeg) {
				super.state(false, "crew", "acme.validation.assignment.CrewIncompatibleLegs.message");
				return;
			}
		}

		// Validación de Asignación de Piloto y Copiloto
		if (leg != null && (cambioDuty || cambioLeg || cambioCrew)) {
			boolean havePilot = this.repository.existsCrewWithDutyInLeg(leg.getId(), DutyCrew.PILOT);
			boolean haveCopilot = this.repository.existsCrewWithDutyInLeg(leg.getId(), DutyCrew.CO_PILOT);

			if (DutyCrew.PILOT.equals(assignment.getDuty()))
				super.state(!havePilot, "duty", "acme.validation.assignment.havePilot.message");
			if (DutyCrew.CO_PILOT.equals(assignment.getDuty()))
				super.state(!haveCopilot, "duty", "acme.validation.assignment.haveCopilot.message");
		}

		// Verificación de leg completado
		boolean legCompleted = this.repository.areLegsCompletedByAssignment(assignment.getId(), MomentHelper.getCurrentMoment());

		if (legCompleted)
			super.state(false, "leg", "acme.validation.assignment.LegAlreadyCompleted.message");
	}

	@Override
	public void perform(final Assignment assignment) {
		boolean change = false;
		Assignment original = this.repository.findAssignmentById(assignment.getId());
		Crew crewMember = assignment.getCrew();
		boolean changeCrewMember = !original.getCrew().equals(crewMember);
		boolean changeDuty = !original.getDuty().equals(assignment.getDuty());
		boolean changeLeg = !original.getLeg().equals(assignment.getLeg());
		boolean changeStatus = !original.getCurrentStatus().equals(assignment.getCurrentStatus());
		boolean changeRemarks = false;
		if (original.getRemarks() != null)
			changeRemarks = !original.getRemarks().equals(assignment.getRemarks());
		else if (assignment.getRemarks() != null)
			changeRemarks = !assignment.getRemarks().equals(original.getRemarks());
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

		Date currentMoment;
		currentMoment = MomentHelper.getCurrentMoment();
		isCompleted = this.repository.areLegsCompletedByAssignment(assignmentId, currentMoment);
		Collection<Crew> crewMembers;
		SelectChoices crewMemberChoices;

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
