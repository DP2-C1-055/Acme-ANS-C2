
package acme.features.crew.assignment;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.assignment.Assignment;
import acme.realms.crew.Crew;

@GuiService
public class CrewAssignmentListService extends AbstractGuiService<Crew, Assignment> {

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
		int crewId = super.getRequest().getPrincipal().getActiveRealm().getId();

		// Recuperar las asignaciones completadas de la tripulación desde la base de datos
		Collection<Assignment> completedAssignments = this.repository.findCompletedAssignmentsByCrewId(crewId);

		// Recuperar las asignaciones planificadas de la tripulación desde la base de datos
		Collection<Assignment> plannedAssignments = this.repository.findPlannedAssignmentsByCrewId(crewId);

		// Agregar las asignaciones completadas a la respuesta
		for (Assignment assignment : completedAssignments) {
			Dataset dataset = super.unbindObject(assignment, "duty", "lastUpdate", "currentStatus", "remarks");
			super.getResponse().addData("completedAssignment", dataset);
		}

		// Agregar las asignaciones planificadas a la respuesta
		for (Assignment assignment : plannedAssignments) {
			Dataset dataset = super.unbindObject(assignment, "duty", "lastUpdate", "currentStatus", "remarks");
			super.getResponse().addData("plannedAssignment", dataset);
		}
	}

	@Override
	public void unbind(final Assignment assignment) {
		Dataset dataset = super.unbindObject(assignment, "duty", "lastUpdate", "currentStatus", "remarks");
		super.getResponse().addData(dataset);
	}
}
