
package acme.features.crew.assignment;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.assignment.Assignment;
import acme.entities.assignment.CurrentStatus;
import acme.entities.assignment.DutyCrew;
import acme.realms.crew.Crew;

@GuiService
public class CrewAssignmentShowService extends AbstractGuiService<Crew, Assignment> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CrewAssignmentRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		Assignment assignment;
		int id;

		id = super.getRequest().getData("id", int.class);
		assignment = this.repository.findAssignmentById(id);

		if (assignment != null)
			super.getBuffer().addData(assignment);
	}

	@Override
	public void unbind(final Assignment assignment) {
		SelectChoices choices, choices2;
		Dataset dataset;

		choices = SelectChoices.from(DutyCrew.class, assignment.getDuty());
		choices2 = SelectChoices.from(CurrentStatus.class, assignment.getCurrentStatus());

		dataset = super.unbindObject(assignment, "duty", "lastUpdate", "currentStatus", "remarks");
		dataset.put("confirmation", false);
		dataset.put("duty", choices);
		dataset.put("currentStatus", choices2);

		// Agregar detalles del tramo de vuelo asociado
		dataset.put("leg", assignment.getLeg());

		// Agregar detalles del miembro de la tripulación de vuelo asociado
		dataset.put("crew", assignment.getCrew());

		super.getResponse().addData(dataset);
	}
}
