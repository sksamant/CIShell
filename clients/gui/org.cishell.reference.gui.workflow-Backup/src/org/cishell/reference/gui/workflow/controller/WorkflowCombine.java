package org.cishell.reference.gui.workflow.controller;

import org.cishell.reference.gui.workflow.model.WorkflowItem;

public class WorkflowCombine {
	private WorkflowItem currentWorkflowItem;

	public WorkflowCombine(WorkflowItem wf) {
		this.currentWorkflowItem = wf;
	}

	public WorkflowItem getCurrentWorkflowItem() {
		return currentWorkflowItem;
	}

	public void setCurrentWorkflow(WorkflowItem currentWorkflowItem) {
		this.currentWorkflowItem = currentWorkflowItem;
	}
}
