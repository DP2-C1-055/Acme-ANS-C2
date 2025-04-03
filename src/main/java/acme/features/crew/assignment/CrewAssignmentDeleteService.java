
package acme.features.crew.assignment;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.activityLog.ActivityLog;
import acme.entities.assignment.Assignment;
import acme.entities.assignment.CurrentStatus;
import acme.entities.assignment.DutyCrew;
import acme.entities.leg.Leg;
import acme.realms.crew.Crew;

@GuiService
public class CrewAssignmentDeleteService extends AbstractGuiService<Crew, Assignment> {

	@Autowired
	private CrewAssignmentRepository repository;


	@Override
	public void authorise() {
		int assignmentId = super.getRequest().getData("id", int.class);
		Assignment assignment = this.repository.findAssignmentById(assignmentId);
		Crew member = assignment == null ? null : assignment.getCrew();

		boolean isOwner = assignment != null && super.getRequest().getPrincipal().hasRealm(member);
		boolean isDraftMode = assignment != null && assignment.isDraftMode();

		boolean authorised = isOwner && isDraftMode;
		super.getResponse().setAuthorised(authorised);

		if (!isDraftMode)
			super.state(false, "*", "acme.validation.assignment.cannot-delete-published.message");
	}

	@Override
	public void load() {
		int id = super.getRequest().getData("id", int.class);
		Assignment assignment = this.repository.findAssignmentById(id);
		super.getBuffer().addData(assignment);
	}

	@Override
	public void bind(final Assignment assignment) {
		Integer legId;
		Leg leg;
		Crew member;

		legId = super.getRequest().getData("leg", int.class);
		leg = this.repository.findLegById(legId);
		member = (Crew) super.getRequest().getPrincipal().getActiveRealm();

		super.bindObject(assignment, "duty", "currentStatus", "remarks");
		assignment.setLeg(leg);
		assignment.setCrew(member);
		assignment.setLastUpdate(MomentHelper.getCurrentMoment());
	}

	@Override
	public void validate(final Assignment assignment) {
		// Verificar que la asignación no esté publicada antes de eliminarla
		super.state(assignment.isDraftMode(), "*", "acme.validation.assignment.cannot-delete-published.message");
	}

	@Override
	public void perform(final Assignment assignment) {
		// Eliminar registros dependientes antes de eliminar la asignación
		Collection<ActivityLog> activityLogs = this.repository.findActivitiesLogsByAssignmentId(assignment.getId());
		this.repository.deleteAll(activityLogs);

		// Ahora eliminamos la asignación
		this.repository.delete(assignment);
	}

	@Override
	public void unbind(final Assignment assignment) {
		SelectChoices statuses;
		SelectChoices duties;
		Dataset dataset;
		SelectChoices selectedLegs;

		Collection<Leg> legs = this.repository.findAllLegs();

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
