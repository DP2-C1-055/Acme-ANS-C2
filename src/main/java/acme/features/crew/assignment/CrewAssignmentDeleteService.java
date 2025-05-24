
package acme.features.crew.assignment;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
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
	}

	@Override
	public void load() {
		int id = super.getRequest().getData("id", int.class);
		Assignment assignment = this.repository.findAssignmentById(id);
		super.getBuffer().addData(assignment);
	}

	@Override
	public void bind(final Assignment assignment) {

		super.bindObject(assignment, "duty", "lastUpdate", "currentStatus", "remarks");

	}

	@Override
	public void validate(final Assignment assignment) {
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
		SelectChoices legChoices;
		SelectChoices currentStatus;
		SelectChoices duty;

		legs = this.repository.findAllLegs();

		legChoices = SelectChoices.from(legs, "flightNumber", assignment.getLeg());
		int crewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();
		Crew crewMember = this.repository.findCrewById(crewMemberId);
		currentStatus = SelectChoices.from(CurrentStatus.class, assignment.getCurrentStatus());
		duty = SelectChoices.from(DutyCrew.class, assignment.getDuty());

		dataset = super.unbindObject(assignment, "id", "duty", "lastUpdate", "currentStatus", "remarks", "draftMode");
		dataset.put("confirmation", false);
		dataset.put("readonly", false);
		dataset.put("lastUpdate", assignment.getLastUpdate());
		dataset.put("currentStatus", currentStatus);
		dataset.put("duty", duty);
		dataset.put("leg", legChoices.getSelected().getKey());
		dataset.put("legs", legChoices);
		dataset.put("crewMember", crewMember.getCode());

		super.getResponse().addData(dataset);
	}
}
