
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
public class CrewAssignmentShowService extends AbstractGuiService<Crew, Assignment> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CrewAssignmentRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		int currentCrewMemberId;
		int assignmentId;
		Assignment assignment;
		boolean crewMemberExists;
		boolean assignmentBelongsToCrewMember;
		boolean isAssignmentOwner;

		currentCrewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();
		assignmentId = super.getRequest().getData("id", int.class);
		assignment = this.repository.findAssignmentById(assignmentId);

		crewMemberExists = this.repository.existsCrewMember(currentCrewMemberId);
		assignmentBelongsToCrewMember = crewMemberExists && this.repository.isAssignmentOwnedByCrewMember(assignmentId, currentCrewMemberId);
		isAssignmentOwner = assignment.getCrew().getId() == currentCrewMemberId;

		super.getResponse().setAuthorised(assignmentBelongsToCrewMember && isAssignmentOwner);
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
	public void unbind(final Assignment assignment) {
		Collection<Leg> legs;
		SelectChoices legChoices;
		Collection<Crew> crewMembers;
		SelectChoices crewMemberChoices;
		Dataset dataset;
		SelectChoices currentStatus;
		int assignmentId;
		SelectChoices duties;
		boolean isCompleted;
		Date currentMoment;

		assignmentId = super.getRequest().getData("id", int.class);
		legs = this.repository.findAllLegs();
		crewMembers = this.repository.findCrewByAvailability(AvailabilityStatus.AVAILABLE);
		currentStatus = SelectChoices.from(CurrentStatus.class, assignment.getCurrentStatus());
		duties = SelectChoices.from(DutyCrew.class, assignment.getDuty());
		legChoices = SelectChoices.from(legs, "flightNumber", assignment.getLeg());
		crewMemberChoices = SelectChoices.from(crewMembers, "code", assignment.getCrew());
		currentMoment = MomentHelper.getCurrentMoment();
		isCompleted = this.repository.areLegsCompletedByAssignment(assignmentId, currentMoment);

		Collection<Assignment> otherAssignments;
		Collection<String> crewDetails;

		otherAssignments = this.repository.findAssignmentByLegId(assignment.getLeg().getId());
		crewDetails = otherAssignments.stream().map(a -> String.format("%s (%s)", a.getCrew().getIdentity().getFullName(), a.getDuty())).toList();

		dataset = super.unbindObject(assignment, "duty", "lastUpdate", "currentStatus", "remarks", "draftMode");
		dataset.put("currentStatus", currentStatus);
		dataset.put("duty", duties);
		dataset.put("leg", legChoices.getSelected().getKey());
		dataset.put("legs", legChoices);
		dataset.put("crewMember", crewMemberChoices.getSelected().getKey());
		dataset.put("crewMembers", crewMemberChoices);
		dataset.put("isCompleted", isCompleted);
		dataset.put("otherCrewMembers", crewDetails);  // 👈 Añadido

		super.getResponse().addData(dataset);
	}

}
