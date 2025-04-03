
package acme.features.crew.assignment;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.controllers.AbstractGuiController;
import acme.client.controllers.GuiController;
import acme.entities.assignment.Assignment;
import acme.realms.crew.Crew;

@GuiController
public class CrewAssignmentController extends AbstractGuiController<Crew, Assignment> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CrewAssignmentListLegsCompletedService	listCompletedService;

	@Autowired
	private CrewAssignmentListLegsPlannedService	listPlannedService;

	@Autowired
	private CrewAssignmentShowService				showService;

	@Autowired
	private CrewAssignmentCreateService				createService;

	@Autowired
	private CrewAssignmentUpdateService				updateService;

	@Autowired
	private CrewAssignmentDeleteService				deleteService;

	@Autowired
	private CrewAssignmentPublishService			publishService;

	// Constructors -----------------------------------------------------------


	@PostConstruct
	protected void initialise() {
		super.addCustomCommand("list-completed", "list", this.listCompletedService);
		super.addCustomCommand("list-planned", "list", this.listPlannedService);
		super.addBasicCommand("show", this.showService);
		super.addBasicCommand("create", this.createService);
		super.addBasicCommand("update", this.updateService);
		super.addBasicCommand("delete", this.deleteService);

		super.addCustomCommand("publish", "update", this.publishService);
	}

}
