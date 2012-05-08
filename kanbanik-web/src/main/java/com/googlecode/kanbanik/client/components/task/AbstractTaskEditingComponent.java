package com.googlecode.kanbanik.client.components.task;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.kanbanik.client.components.PanelContainingDialog;
import com.googlecode.kanbanik.client.components.PanelContainingDialog.PanelContainingDialolgListener;
import com.googlecode.kanbanik.client.messaging.MessageBus;
import com.googlecode.kanbanik.dto.ClassOfService;
import com.googlecode.kanbanik.dto.TaskDto;

public abstract class AbstractTaskEditingComponent {
	
	private Panel panel = new VerticalPanel();
	
	private TextBox taskName = new TextBox();
	
	private Label ticketId = new Label("");
	
	private Widget description;
	
	private RichTextArea richTextArea;
	
	private ListBox classOfService = new ListBox();
	
	private PanelContainingDialog dialog;
	
	private String name;

	private final HasClickHandlers clickHandler;
	
	public AbstractTaskEditingComponent(HasClickHandlers clickHandler) {
		this.clickHandler = clickHandler;
		this.name = "Task Details";
	}
	
	protected void initialize() {
		description = initializeRichTextEditor();
		
		Grid header = new Grid(3, 2);
		header.setWidget(0, 0, new Label("ID"));
		header.setWidget(0, 1, ticketId);
		
		header.setWidget(1, 0, new Label("Short descritption"));
		header.setWidget(1, 1, taskName);
		
		header.setWidget(2, 0, new Label("Class of service"));
		header.setWidget(2, 1, classOfService);
		
		header.setWidth("500px");
		
		panel.add(header);
		panel.add(description);
		panel.setWidth("100%");
		
		setupValues();
		
		dialog = new PanelContainingDialog(name, panel);
		dialog.addListener(new AddTaskButtonHandler());
		clickHandler.addClickHandler(new ShowDialogHandler());
	}
	
	  public Widget initializeRichTextEditor() {
		    richTextArea = new RichTextArea();
		    richTextArea.ensureDebugId("cwRichText-area");
		    richTextArea.setSize("100%", "400px");
		    RichTextToolbar toolbar = new RichTextToolbar(richTextArea);
		    toolbar.ensureDebugId("cwRichText-toolbar");
		    toolbar.setWidth("100%");

		    Grid grid = new Grid(2, 1);
		    grid.setStyleName("cw-RichText");
		    grid.setWidget(0, 0, toolbar);
		    grid.setWidget(1, 0, richTextArea);
		    return grid;
		  }
	
	private void setupValues() {
		ticketId.setText(getTicketId());
		taskName.setValue(getTaskName());
		richTextArea.setHTML(getDescription());
		
		String currentClassOfService = getClassOfServiceAsString();
		classOfService.clear();
		classOfService.addItem(currentClassOfService);
		for(ClassOfService item : ClassOfService.values()) {
			if (item.toString().equals(currentClassOfService)) {
				continue;
			}
			classOfService.addItem(item.toString());
		}
	}

	protected abstract String getClassOfServiceAsString();
	protected abstract String getTicketId();
	protected abstract String getTaskName();
	protected abstract String getDescription();
	protected abstract String getId();

	private TaskDto createTaskDTO() {
		TaskDto taskDTO = createBasicDTO();
		taskDTO.setName(taskName.getText());
		taskDTO.setDescription(richTextArea.getHTML());
		taskDTO.setClassOfService(ClassOfService.STANDARD);
		taskDTO.setId(getId());
		taskDTO.setClassOfService(getClassOfService());
		return taskDTO;
	}

	protected abstract TaskDto createBasicDTO();

	private ClassOfService getClassOfService() {
		int index = classOfService.getSelectedIndex();
		String value = classOfService.getValue(index);
		for(ClassOfService item : ClassOfService.values()) {
			if (item.toString().equals(value)) {
				return item;
			}
		}
		
		return ClassOfService.STANDARD;
	}

	class ShowDialogHandler implements ClickHandler {

		public void onClick(ClickEvent event) {
			setupValues();
			dialog.center();
			taskName.setFocus(true);
		}
		
	}
	
	class AddTaskButtonHandler implements PanelContainingDialolgListener {

		public void okClicked(PanelContainingDialog dialog) {
			MessageBus.sendMessage(new TaskChangedMessage(createTaskDTO(), AbstractTaskEditingComponent.this));
		}

		public void cancelClicked(PanelContainingDialog dialog) {
			
		}
		
	}
}