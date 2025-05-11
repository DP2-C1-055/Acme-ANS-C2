
package acme.features.crew.assignment;

import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.assignment.Assignment;
import acme.realms.crew.Crew;

@GuiService
public class CrewAssignmentListLegsPlannedService extends AbstractGuiService<Crew, Assignment> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CrewAssignmentRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean status;

		status = super.getRequest().getPrincipal().hasRealmOfType(Crew.class);

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		int crewId;
		Date now;
		Collection<Assignment> plannedAssignments;

		crewId = super.getRequest().getPrincipal().getActiveRealm().getId();
		now = MomentHelper.getCurrentMoment();

		plannedAssignments = this.repository.findPlannedAssignmentsByCrewId(now, crewId);

		super.getBuffer().addData(plannedAssignments);
	}

	@Override
	public void unbind(final Assignment assignment) {
		Dataset dataset;

		dataset = super.unbindObject(assignment, "duty", "lastUpdate", "currentStatus", "remarks", "draftMode", "leg");
		dataset.put("leg", assignment.getLeg().getFlightNumber());

		super.getResponse().addData(dataset);
	}
}
