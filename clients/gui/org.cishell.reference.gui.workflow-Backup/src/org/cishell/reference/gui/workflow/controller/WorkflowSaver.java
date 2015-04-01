package org.cishell.reference.gui.workflow.controller;

import org.cishell.reference.gui.workflow.model.Workflow;
import org.cishell.reference.gui.workflow.model.WorkflowItem;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Rootitem")
public class WorkflowSaver {

	private Workflow currentWorkflow;

	public WorkflowSaver(Workflow wf) {
		this.currentWorkflow = wf;
	}

	public Workflow getCurrentWorkflow() {
		return currentWorkflow;
	}

	public void setCurrentWorkflow(Workflow currentWorkflow) {
		this.currentWorkflow = currentWorkflow;
	}

}
