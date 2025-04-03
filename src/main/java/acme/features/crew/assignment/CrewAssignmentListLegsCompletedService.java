
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
public class CrewAssignmentListLegsCompletedService extends AbstractGuiService<Crew, Assignment> {

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
		Date now = MomentHelper.getCurrentMoment();

		Collection<Assignment> completedAssignments = this.repository.findCompletedAssignmentsByCrewId(now, crewId);

		super.getBuffer().addData(completedAssignments);
	}

	@Override
	public void unbind(final Assignment assignment) {
		Dataset dataset = super.unbindObject(assignment, "duty", "lastUpdate", "currentStatus");

		super.addPayload(dataset, assignment, "remarks");

		super.getResponse().addData(dataset);
	}
}
