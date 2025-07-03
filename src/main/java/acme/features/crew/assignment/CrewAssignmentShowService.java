
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
import acme.realms.crew.Crew;

@GuiService
public class CrewAssignmentShowService extends AbstractGuiService<Crew, Assignment> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CrewAssignmentRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		int currentCrewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();
		int assignmentId = super.getRequest().getData("id", int.class);
		Assignment assignment = this.repository.findAssignmentById(assignmentId);

		boolean isAuthorised = assignment != null && assignment.getCrew().getId() == currentCrewMemberId;
		super.getResponse().setAuthorised(isAuthorised);
	}

	@Override
	public void load() {
		int assignmentId = super.getRequest().getData("id", int.class);
		Assignment assignment = this.repository.findAssignmentById(assignmentId);
		super.getBuffer().addData(assignment);
	}

	@Override
	public void unbind(final Assignment assignment) {
		Collection<Leg> legs = this.repository.findAllLegs();
		SelectChoices legChoices = SelectChoices.from(legs, "flightNumber", assignment.getLeg());
		SelectChoices currentStatus = SelectChoices.from(CurrentStatus.class, assignment.getCurrentStatus());
		SelectChoices duty = SelectChoices.from(DutyCrew.class, assignment.getDuty());

		int assignmentId = super.getRequest().getData("id", int.class);
		int crewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();
		Crew crewMember = this.repository.findCrewById(crewMemberId);

		Date currentMoment = MomentHelper.getCurrentMoment();
		boolean isCompleted = this.repository.areLegsCompletedByAssignment(assignmentId, currentMoment);
		Collection<Crew> crewMembers = this.repository.findCrewMembersByLegId(assignment.getLeg().getId());
		Dataset dataset = super.unbindObject(assignment, "duty", "lastUpdate", "currentStatus", "remarks", "draftMode");
		dataset.put("currentStatus", currentStatus);
		dataset.put("duty", duty);
		dataset.put("legs", legChoices);
		dataset.put("leg", legChoices.getSelected().getKey());
		dataset.put("crewMember", crewMember.getCode());
		dataset.put("isCompleted", isCompleted);
		String crewCodes = crewMembers.stream().map(Crew::getCode).distinct().reduce((a, b) -> a + ", " + b).orElse("-");

		dataset.put("crewMembers", crewCodes);

		super.getResponse().addData(dataset);
	}
}
