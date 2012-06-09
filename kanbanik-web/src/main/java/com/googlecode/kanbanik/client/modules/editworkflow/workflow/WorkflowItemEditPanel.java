package com.googlecode.kanbanik.client.modules.editworkflow.workflow;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.googlecode.kanbanik.dto.ItemType;

public class WorkflowItemEditPanel extends VerticalPanel {

	private TextBox nameBox;
	
	private TextBox wipLimitBox;
	
	private CheckBox wipLimitEnabled;
	
	private RadioButton horizontal = new RadioButton("itemTypeGroup", "Horizontal");

	private RadioButton vertical = new RadioButton("itemTypeGroup", "Vertical");
	
	public void setWipLimit(int wipLimit) {
		wipLimitBox = new TextBox();
		HorizontalPanel panel = createNameWaluePair("WIP Limit: ", Integer.toString(wipLimit), wipLimitBox);
		
		wipLimitEnabled = new CheckBox();
		wipLimitEnabled.setValue(true);
		
		if (wipLimit == -1) {
			disableWipLimit();
			wipLimitEnabled.setValue(false);
		} 
		
		wipLimitEnabled.addClickHandler(new WipLimitEnabledClickHandler());
		panel.add(wipLimitEnabled);
		
	}

	private void disableWipLimit() {
		wipLimitBox.setText("");
		wipLimitBox.setEnabled(false);
	}
	
	public void setType(ItemType itemType) {
		VerticalPanel panel = new VerticalPanel();
		panel.add(horizontal);
		panel.add(vertical);
		add(panel);
		
		if (itemType == ItemType.HORIZONTAL) {
			horizontal.setValue(true);
		} else {
			vertical.setValue(true);
		}
	}
	
	class WipLimitEnabledClickHandler implements ClickHandler {

		public void onClick(ClickEvent event) {
			if (!wipLimitEnabled.getValue()) {
				wipLimitBox.setEnabled(false);
			} else {
				wipLimitBox.setEnabled(true);
			}
		}
		
	}
	
	public void setName(String name) {
		nameBox = new TextBox();
		createNameWaluePair("Name: ", name, nameBox);	
	}

	private HorizontalPanel createNameWaluePair(String label, String value, TextBox textBox) {
		HorizontalPanel panel = new HorizontalPanel();
		panel.add(new Label(label));
		textBox.setText(value);
		panel.add(textBox);
		add(panel);
		return panel;
	}
	
	public String getName() {
		return nameBox.getText();
	}
	
	public int getWipLimit() {
		if (!wipLimitEnabled.getValue()) {
			return -1;
		}
		try {
			return Integer.parseInt(wipLimitBox.getText());
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public ItemType getItemType() {
		if (horizontal.getValue()) {
			return ItemType.HORIZONTAL;
		}
		
		return ItemType.VERTICAL;
	}
	
}