package org.cishell.reference.gui.workflow.controller;

import org.cishell.reference.gui.workflow.model.WorkflowItem;

public class WorkflowMerger {

	private WorkflowItem currentWorkflowItem;

	public WorkflowMerger(WorkflowItem wf) {
		this.currentWorkflowItem = wf;
	}

	public WorkflowItem getCurrentWorkflowItem() {
		return currentWorkflowItem;
	}

	public void setCurrentWorkflowItem(WorkflowItem currentWorkflowItem) {
		this.currentWorkflowItem = currentWorkflowItem;
	}

}
