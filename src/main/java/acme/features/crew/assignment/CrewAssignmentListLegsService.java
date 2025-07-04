
package acme.features.crew.assignment;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.assignment.Assignment;
import acme.realms.crew.Crew;

@GuiService
public class CrewAssignmentListLegsService extends AbstractGuiService<Crew, Assignment> {

	@Autowired
	private CrewAssignmentRepository repository;


	@Override
	public void authorise() {
		boolean status = super.getRequest().getPrincipal().hasRealmOfType(Crew.class);
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		int crewId = super.getRequest().getPrincipal().getActiveRealm().getId();

		Collection<Assignment> allAssignments = this.repository.findAssignmentsByCrewId(crewId);

		super.getBuffer().addData(allAssignments);
	}

	@Override
	public void unbind(final Assignment assignment) {
		Dataset dataset = super.unbindObject(assignment, "duty", "lastUpdate", "currentStatus", "remarks", "leg");
		dataset.put("leg", assignment.getLeg().getFlightNumber());

		Date now = MomentHelper.getCurrentMoment();

		if (assignment.isDraftMode()) {
			final Locale locale = super.getRequest().getLocale();
			String draftModeText;
			if (locale.equals(Locale.ENGLISH))
				draftModeText = "Yes";
			else
				draftModeText = "Sí";
			dataset.put("draftMode", draftModeText);
		} else
			dataset.put("draftMode", "No");

		String status;
		if (assignment.getLeg().getScheduledArrival().before(now))
			status = "DONE";
		else
			status = "PENDING";

		dataset.put("assignmentStatus", status);

		super.getResponse().addData(dataset);
	}
}
