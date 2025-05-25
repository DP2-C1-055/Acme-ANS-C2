
package acme.features.crew.assignment;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.activityLog.ActivityLog;
import acme.entities.assignment.Assignment;
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
		;
	}
}
