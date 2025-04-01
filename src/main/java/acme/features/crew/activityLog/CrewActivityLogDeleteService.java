
package acme.features.crew.activityLog;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.activityLog.ActivityLog;
import acme.realms.crew.Crew;

@GuiService
public class CrewActivityLogDeleteService extends AbstractGuiService<Crew, ActivityLog> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CrewActivityLogRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		//		boolean status;
		//		ActivityLog activityLog;
		//		int id;
		//		FlightCrewMember flightCrewMember;
		//
		//		id = super.getRequest().getData("id", int.class);
		//		activityLog = this.repository.findActivityLogById(id);
		//		flightCrewMember = activityLog == null ? null : activityLog.getFlightCrewMember();
		//		status = super.getRequest().getPrincipal().hasRealm(flightCrewMember) && (activityLog == null || activityLog.isDraftMode());
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		ActivityLog activityLog;
		int id;

		id = super.getRequest().getData("id", int.class);
		activityLog = this.repository.findActivityLogById(id);

		super.getBuffer().addData(activityLog);
	}

	@Override
	public void bind(final ActivityLog activityLog) {
		super.bindObject(activityLog, "registrationMoment", "typeIncident", "description", "severityLevel");
	}

	@Override
	public void validate(final ActivityLog activityLog) {

	}

	@Override
	public void perform(final ActivityLog activityLog) {

		this.repository.delete(activityLog);
	}

	@Override
	public void unbind(final ActivityLog activityLog) {

		Dataset dataset;

		dataset = super.unbindObject(activityLog, "registrationMoment", "typeIncident", "description", "severityLevel", "draftMode");

		super.getResponse().addData(dataset);
	}
}
