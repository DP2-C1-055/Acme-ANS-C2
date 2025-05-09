
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
import acme.realms.crew.AvailabilityStatus;
import acme.realms.crew.Crew;

@GuiService
public class CrewAssignmentDeleteService extends AbstractGuiService<Crew, Assignment> {

	@Autowired
	private CrewAssignmentRepository repository;


	@Override
	public void authorise() {
		boolean isAuthorised;
		int assignmentId;
		Assignment assignment;
		Crew member;
		boolean isOwner;
		boolean isDraftMode;

		assignmentId = super.getRequest().getData("id", int.class);
		assignment = this.repository.findAssignmentById(assignmentId);
		member = assignment == null ? null : assignment.getCrew();

		isOwner = assignment != null && super.getRequest().getPrincipal().hasRealm(member);
		isDraftMode = assignment != null && assignment.isDraftMode();

		isAuthorised = isOwner && isDraftMode;
		super.getResponse().setAuthorised(isAuthorised);

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
		Integer crewId;

		legId = super.getRequest().getData("leg", int.class);
		leg = this.repository.findLegById(legId);

		crewId = super.getRequest().getData("crewMember", int.class);
		member = this.repository.findCrewById(crewId);

		super.bindObject(assignment, "duty", "lastUpdate", "currentStatus", "remarks");
		assignment.setLeg(leg);
		assignment.setCrew(member);
		assignment.setLastUpdate(MomentHelper.getCurrentMoment());
	}

	@Override
	public void validate(final Assignment assignment) {
		super.state(assignment.isDraftMode(), "*", "acme.validation.assignment.cannot-delete-published.message");
	}

	@Override
	public void perform(final Assignment assignment) {

		Collection<ActivityLog> activityLogs = this.repository.findActivitiesLogsByAssignmentId(assignment.getId());
		this.repository.deleteAll(activityLogs);

		this.repository.delete(assignment);
	}

	@Override
	public void unbind(final Assignment assignment) {
		Dataset dataset;
		Collection<Leg> legs;
		Collection<Crew> crewMembers;
		SelectChoices legChoices;
		SelectChoices crewMembersChoices;
		SelectChoices currentStatus;
		SelectChoices duty;

		legs = this.repository.findAllLegs();
		crewMembers = this.repository.findCrewByAvailability(AvailabilityStatus.AVAILABLE);

		legChoices = SelectChoices.from(legs, "flightNumber", assignment.getLeg());
		crewMembersChoices = SelectChoices.from(crewMembers, "employeeCode", assignment.getCrew());
		currentStatus = SelectChoices.from(CurrentStatus.class, assignment.getCurrentStatus());
		duty = SelectChoices.from(DutyCrew.class, assignment.getDuty());

		dataset = super.unbindObject(assignment, "duty", "lastUpdate", "currentStatus", "remarks", "draftMode");
		dataset.put("confirmation", false);
		dataset.put("readonly", false);
		dataset.put("lastUpdate", MomentHelper.getBaseMoment());
		dataset.put("currentStatus", currentStatus);
		dataset.put("duty", duty);
		dataset.put("leg", legChoices.getSelected().getKey());
		dataset.put("legs", legChoices);
		dataset.put("crewMember", crewMembersChoices.getSelected().getKey());
		dataset.put("crewMembers", crewMembersChoices);

		super.getResponse().addData(dataset);
	}
}
