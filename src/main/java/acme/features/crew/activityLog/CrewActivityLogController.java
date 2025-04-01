
package acme.features.crew.activityLog;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.controllers.AbstractGuiController;
import acme.client.controllers.GuiController;
import acme.entities.activityLog.ActivityLog;
import acme.realms.crew.Crew;

@GuiController
public class CrewActivityLogController extends AbstractGuiController<Crew, ActivityLog> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CrewActivityLogListService		listService;

	@Autowired
	private CrewActivityLogShowService		showService;

	@Autowired
	private CrewActivityLogCreateService	createService;

	@Autowired
	private CrewActivityLogDeleteService	deleteService;

	@Autowired
	private CrewActivityLogUpdateService	updateService;

	@Autowired
	private CrewActivityLogPublishService	publishService;

	// Constructors -----------------------------------------------------------


	@PostConstruct
	protected void initialise() {
		super.addBasicCommand("list", this.listService);
		super.addBasicCommand("show", this.showService);
		super.addBasicCommand("create", this.createService);
		super.addBasicCommand("update", this.updateService);
		super.addBasicCommand("delete", this.deleteService);

		super.addCustomCommand("publish", "update", this.publishService);

	}

}
