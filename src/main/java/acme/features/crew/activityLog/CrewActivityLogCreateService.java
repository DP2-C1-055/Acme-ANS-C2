
package acme.features.crew.activityLog;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.activityLog.ActivityLog;
import acme.realms.crew.Crew;

@GuiService
public class CrewActivityLogCreateService extends AbstractGuiService<Crew, ActivityLog> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CrewActivityLogRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		ActivityLog activityLog;
		Date registrationMoment;

		registrationMoment = MomentHelper.getCurrentMoment();

		activityLog = new ActivityLog();
		activityLog.setRegistrationMoment(registrationMoment);
		activityLog.setTypeIncident("");
		activityLog.setDescription("");
		activityLog.setSeverityLevel(0);
		activityLog.setDraftMode(true);

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
		Date registrationMoment;

		registrationMoment = MomentHelper.getCurrentMoment();
		activityLog.setRegistrationMoment(registrationMoment);
		this.repository.save(activityLog);
	}

	@Override
	public void unbind(final ActivityLog activityLog) {
		Dataset dataset;

		dataset = super.unbindObject(activityLog, "registrationMoment", "typeIncident", "description", "severityLevel");
		dataset.put("draftMode", false);

		super.getResponse().addData(dataset);
	}
}
