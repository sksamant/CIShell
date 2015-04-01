/* **************************************************************************** 
 * CIShell: Cyberinfrastructure Shell, An Algorithm Integration Framework.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Apache License v2.0 which accompanies
 * this distribution, and is available at:
 * http://www.apache.org/licenses/LICENSE-2.0.html
 * 
 * Created on Aug 21, 2006 at Indiana University.
 * Changed on Dec 19, 2006 at Indiana University
 * 
 * Contributors:
 * 	   Weixia(Bonnie) Huang, Bruce Herr, Ben Markines
 *     School of Library and Information Science, Indiana University 
 * ***************************************************************************/
package org.cishell.reference.gui.workflow.views;

import java.util.Calendar;
import java.util.Dictionary;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Stack;

import org.cishell.app.service.scheduler.SchedulerListener;
import org.cishell.framework.algorithm.Algorithm;
import org.cishell.framework.algorithm.AlgorithmCreationFailedException;
import org.cishell.framework.algorithm.AlgorithmFactory;
import org.cishell.framework.algorithm.AlgorithmProperty;
import org.cishell.framework.data.Data;
import org.cishell.reference.gui.menumanager.menu.AlgorithmWrapper;
import org.cishell.reference.gui.workflow.Activator;
import org.cishell.reference.gui.workflow.Utilities.Constant;
import org.cishell.reference.gui.workflow.Utilities.Utils;
import org.cishell.reference.gui.workflow.controller.WorkflowMaker;
import org.cishell.reference.gui.workflow.controller.WorkflowManager;
import org.cishell.reference.gui.workflow.model.AlgorithmWorkflowItem;
import org.cishell.reference.gui.workflow.model.NormalWorkflow;
import org.cishell.reference.gui.workflow.model.SchedulerContentModel;
import org.cishell.reference.gui.workflow.model.Workflow;
import org.cishell.reference.gui.workflow.model.WorkflowItem;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeProvider;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.service.metatype.ObjectClassDefinition;

import java.util.ArrayList;
import java.util.List;
/**
 * Creates and maintains the overall GUI for the workflow. Controls the table
 * and controls (moving, removing, etc.).
 * 
 */
public class WorkflowView extends ViewPart implements SchedulerListener {
	private static WorkflowView workFlowView;
	public static final String ID_VIEW = "org.cishell.reference.gui.workflow.views.WorkflowView";
	private TreeViewer viewer;
	private WorkflowGUI rootItem, currentWorkFlowItem;
	private Tree tree;
	private Menu menu;
	private Menu whiteSpacemenu;
	private SaveListener saveListener;
	private LoadListener loadListener;
	private UndoListener undoListener;
	private RunListener runListener;
	private PauseListener pauseListener;
	private DeleteListener deleteListener;
	private WorkflowMode mode;
	private TreeEditor editor;
	private Text newEditor;
	private boolean updatingTreeItem;
	private WorkflowTreeItem currentParentItem;
	private String brandPluginID;
/*	private Button removeButton;
	private Button addButton;*/
	
	private Button playStateButton;
	private Button stopStateButton;
	private Button deleteStateButton;
	private Button saveStateButton;
	private Button editStateButton;
	private Button combineStateButton;
	private Button recordStateButton;
	private Button pauseStateButton;
	//private static Image playImage = Activator.createImage("play.png");
	private static Image playImage;
	private static Image stopImage;
	private static Image pauseImage;
	private static Image deleteImage;
	private static Image saveImage;
	private static Image editImage;
	private static Image combineImage;
	private static Image recordImage;
	private TableViewer viewerTable;
	private Button removeButton;
	
	private Stack<WorkflowGUI> undoStack;
	private Stack<WorkflowGUI> redoStack;
	
	
	public WorkflowView(String brandPluginID) {
		this.brandPluginID = brandPluginID;
		workFlowView = this;
		
		playImage = Utils.getImage("play.png", brandPluginID);
		stopImage = Utils.getImage("stop.png", brandPluginID);
		deleteImage = Utils.getImage("delete.png", brandPluginID);
		saveImage = Utils.getImage("save.png", brandPluginID);
		editImage = Utils.getImage("edit.png", brandPluginID);
		combineImage = Utils.getImage("merge.png", brandPluginID);
		recordImage = Utils.getImage("record.png", brandPluginID);
		pauseImage = Utils.getImage("pause.png", brandPluginID);
		
		undoStack = new Stack<WorkflowGUI>();
		redoStack = new Stack<WorkflowGUI>();
	}

	/**
	 * Get the current workflow view
	 * 
	 * @return The workflow view
	 */
	public static WorkflowView getDefault() {
		return workFlowView;
	}

	/**
	 * Creates buttons, table, and registers listeners
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 * @param parent
	 *            The SWT parent
	 */

	@Override
	public void createPartControl(Composite parent) {
        Composite control = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.horizontalSpacing = 5;
        layout.verticalSpacing = 2;
        
        control.setLayout(layout );
        
        Composite control1 = new Composite(control, SWT.NONE);
        GridLayout layout1 = new GridLayout();
        layout1.numColumns = 8;
        control1.setLayout(layout1 );
/*        RowLayout layout = new RowLayout();
        layout.wrap = true;
        layout.pack = false;
        layout.justify = true;
        layout.type = SWT.HORIZONTAL;
        layout.marginLeft = 5;
        layout.marginTop = 5;
        layout.marginRight = 5;
        layout.marginBottom = 5;
        layout.spacing = 0;
        control.setLayout(layout);*/
        
		playStateButton = new Button(control1, SWT.PUSH);
		
		GridData gridData = new GridData();
		//gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalSpan = 1;
		playStateButton.setLayoutData(gridData);
		
		playStateButton.setImage(playImage);
		playStateButton.setEnabled(true);
		playStateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				runSelection();
			}
		});		
		
		stopStateButton = new Button(control1, SWT.PUSH);
		
		gridData = new GridData();
		//gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalSpan = 1;
		stopStateButton.setLayoutData(gridData);
		
		stopStateButton.setImage(stopImage);
		stopStateButton.setEnabled(true);
		stopStateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				stopSelection();
			}
		});
		
		deleteStateButton = new Button(control1, SWT.PUSH);
		
		gridData = new GridData();
		//gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalSpan = 1;
		deleteStateButton.setLayoutData(gridData);
		
		deleteStateButton.setImage(deleteImage);	
		deleteStateButton.setEnabled(true);
		deleteStateButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                	removeSelection();
                }
            });
			
		saveStateButton = new Button(control1, SWT.PUSH);
		
		gridData = new GridData();
		//gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalSpan = 1;
		saveStateButton.setLayoutData(gridData);
		
		saveStateButton.setImage(saveImage);
		saveStateButton.setEnabled(true);
		saveStateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				saveSelection();
			}
		});
		
		editStateButton = new Button(control1, SWT.PUSH);
		
		gridData = new GridData();
		//gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalSpan = 1;
		editStateButton.setLayoutData(gridData);
		
		editStateButton.setImage(editImage);
		editStateButton.setEnabled(true);
		editStateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleInput();
			}
		});
		
		combineStateButton = new Button(control1, SWT.PUSH);
		
		gridData = new GridData();
		//gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalSpan = 1;
		combineStateButton.setLayoutData(gridData);
		
		combineStateButton.setImage(combineImage);
		combineStateButton.setEnabled(true);
		combineStateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				combineSelection();
			}
		});
		
		recordStateButton = new Button(control1, SWT.PUSH);
		
		gridData = new GridData();
		gridData.verticalSpan = 1;
		recordStateButton.setLayoutData(gridData);
		
		recordStateButton.setImage(recordImage);
		recordStateButton.setEnabled(true);
		recordStateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				recordSelection();
			}
		});
		
		pauseStateButton = new Button(control1, SWT.PUSH);
		
		gridData = new GridData();
		//gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalSpan = 1;
		pauseStateButton.setLayoutData(gridData);
		
		pauseStateButton.setImage(pauseImage);
		pauseStateButton.setEnabled(true);
		pauseStateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				pauseSelection();
			}
		});
		
       // Composite control1 = new Composite(parent, SWT.NONE);
      // GridLayout layout1 = new GridLayout();
       // layout1.numColumns = 1;
       // control.setLayout(layout1);
		layout.numColumns = 1;
		this.viewer = new TreeViewer(control, SWT.VERTICAL | SWT.MULTI);
	    gridData = new GridData();
	    gridData.grabExcessHorizontalSpace = true;
	    gridData.grabExcessVerticalSpace = true;
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.verticalAlignment = GridData.FILL;
	    viewer.getTree().setLayoutData(gridData);
	            
		this.viewer.setContentProvider(new DataTreeContentProvider());
		this.viewer.setLabelProvider(new DataTreeLabelProvider());

		this.rootItem = new WorkflowGUI(null, null, 2, this.brandPluginID);
		this.viewer.setInput(this.rootItem);
		this.viewer.expandAll();
		this.tree = this.viewer.getTree();
		this.tree.addSelectionListener(new DatamodelSelectionListener());
		this.tree.addMouseListener(new ContextMenuListener());

		// Setup the context menu for the tree.

		this.menu = new Menu(tree);
		this.menu.setVisible(false);

		MenuItem changeItem = new MenuItem(this.menu, SWT.PUSH);
		changeItem.setText("Edit");
		changeItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				handleInput();
			}
		});

		MenuItem undoItem = new MenuItem(this.menu, SWT.PUSH);
		undoItem.setText("Undo");
		this.undoListener = new UndoListener();
		undoItem.addListener(SWT.Selection, undoListener);
		
		MenuItem runItem = new MenuItem(this.menu, SWT.PUSH);
		runItem.setText("Run");
		this.runListener = new RunListener();
		runItem.addListener(SWT.Selection, runListener);

		MenuItem saveItem = new MenuItem(this.menu, SWT.PUSH);
		saveItem.setText("Save");
		this.saveListener = new SaveListener();
		saveItem.addListener(SWT.Selection, this.saveListener);
		
		MenuItem pauseItem = new MenuItem(this.menu, SWT.PUSH);
		pauseItem.setText("pause");
		this.pauseListener = new PauseListener();
		pauseItem.addListener(SWT.Selection, this.pauseListener);

		MenuItem deleteItem = new MenuItem(this.menu, SWT.PUSH);
		deleteItem.setText("Delete");
		this.deleteListener = new DeleteListener();
		deleteItem.addListener(SWT.Selection, this.deleteListener);

		this.editor = new TreeEditor(this.tree);
		this.editor.horizontalAlignment = SWT.LEFT;
		this.editor.grabHorizontal = true;
		this.editor.minimumWidth = 50;

		// create white space menu

		this.whiteSpacemenu = new Menu(tree);
		this.whiteSpacemenu.setVisible(false);

		MenuItem newItem = new MenuItem(this.whiteSpacemenu, SWT.PUSH);
		newItem.setText("New Workflow");

		MenuItem loadItem = new MenuItem(this.whiteSpacemenu, SWT.PUSH);
		loadItem.setText("Load");
		this.loadListener = new LoadListener();
		loadItem.addListener(SWT.Selection, this.loadListener);

		guiRun(new Runnable() {
			public void run() {
				try {
					IWorkbenchPage page = WorkflowView.this.getSite().getPage();
					page.showView("org.cishell.reference.gui.datamanager.DataManagerView");
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}

		});

		newItem.addListener(SWT.Selection, new NewWorkflow());

		addNewWorkflow("Workflow ");
		SchedulerContentModel.getInstance().register(this);
	}

    /**
     *Run items that are selected
     */		
	private void runSelection() {	
		TreeItem[] items = WorkflowView.this.tree.getSelection();
		if (items.length != 1)
			return;
		WorkflowTreeItem itm = (WorkflowTreeItem) items[0].getData();
		String type = itm.getType();
		if (type == Constant.Workflow) {
			WorkflowView.this.mode = WorkflowMode.RUNNING;
			((WorkflowGUI) itm).getWorkflow().run();
			WorkflowView.this.mode = WorkflowMode.STOPPED;
		}
	}
	
    /**
     *Stop runing items that are selected
     */		
	private void stopSelection() {	
		TreeItem[] items = WorkflowView.this.tree.getSelection();
		if (items.length != 1)
			return;
		WorkflowTreeItem itm = (WorkflowTreeItem) items[0].getData();
		String type = itm.getType();
		if (type == Constant.Workflow) {
			WorkflowView.this.mode = WorkflowMode.STOPPED;
			stopStateButton.setEnabled(false);
			playStateButton.setEnabled(true);
			//((WorkflowGUI) itm).getWorkflow().run();
			//WorkflowView.this.mode = WorkflowMode.STOPPED;
		}
	}	
	
    /**
     *Record items that are selected
     */		
	private void recordSelection() {	
		TreeItem[] items = WorkflowView.this.tree.getSelection();
		if (items.length != 1)
			return;
		WorkflowTreeItem itm = (WorkflowTreeItem) items[0].getData();
		String type = itm.getType();
		if (type == Constant.Workflow) {
			WorkflowView.this.mode = WorkflowMode.PAUSED;
			playStateButton.setEnabled(false);
			pauseStateButton.setEnabled(true);
			//((WorkflowGUI) itm).getWorkflow().run();
			//WorkflowView.this.mode = WorkflowMode.STOPPED;
		}
	}
	
    /**
     *Run items that are selected
     */		
	private void pauseSelection() {	
		TreeItem[] items = WorkflowView.this.tree.getSelection();
		if (items.length != 1)
			return;
		WorkflowTreeItem itm = (WorkflowTreeItem) items[0].getData();
		String type = itm.getType();
		if (type == Constant.Workflow) {
			WorkflowView.this.mode = WorkflowMode.RUNNING;
			pauseStateButton.setEnabled(false);
			recordStateButton.setEnabled(true);
			//((WorkflowGUI) itm).getWorkflow().run();
			//WorkflowView.this.mode = WorkflowMode.STOPPED;
		}
	}
	
    /**
     * Merge items that are selected
     */
    private void combineSelection() {
        TreeItem[] treeItems = WorkflowView.this.tree.getSelection();
        if (treeItems.length <= 1)
            return;

        WorkflowGUI targetWorkflowGUI = (WorkflowGUI) treeItems[0].getData();  
        
        WorkflowGUI addWorkflowGUI = (WorkflowGUI) treeItems[1].getData();

                Object[] wfChildren = addWorkflowGUI.getChildren();

          for (int i = 1; i < wfChildren.length; i++) {
            if (wfChildren[i] instanceof AlgorithmWorkflowItem) {
                AlgorithmWorkflowItem algoWFItem = (AlgorithmWorkflowItem) wfChildren[i];
                algoWFItem
                        .setInternalId(((NormalWorkflow) targetWorkflowGUI
                                .getWorkflow()).getUniqueInternalId());
            }
            
            WorkflowTreeItem workflowTreeItem = (WorkflowTreeItem) wfChildren[i];
            if (Constant.Workflow.equals(workflowTreeItem.getType())) {
            	targetWorkflowGUI.addChild(workflowTreeItem);
            }
        }
        WorkflowView.this.tree.setSelection(treeItems[0]);
        WorkflowMaker combinedState = new WorkflowMaker();
        combinedState.combine(targetWorkflowGUI.getWorkflow());
    }
/*        LinkedHashMap<Long, WorkflowItem>  wfItm = wf.getItemMap();
        WorkflowTreeItem treeItm = (WorkflowTreeItem)targetWorkflowGUI;
        WorkflowItem itm = wfItm.get(1);
       
        TreeItem secondTreeItem = treeItems[1];
        WorkflowGUI secondWorkflowGUI = (WorkflowGUI) secondTreeItem.getData();
       
        System.out.println("######################################");
 
       
        System.out.println("First object"+targetWorkflowGUI.getWorkflow().getName());
        // int childCount = targetWorkflowGUI.getChildren().length;

        WorkflowMaker targetWorkflowWriter = new WorkflowMaker();*/
    
       
       
       
/*        for (int j = 1; j < treeItems.length; j++) {
            WorkflowGUI wf = (WorkflowGUI) treeItems[j].getData();
            Object[] wfChildren = wf.getChildren();

            for (int i = 1; i < wfChildren.length; i++) {
                if (wfChildren[i] instanceof AlgorithmWorkflowItem) {
                    AlgorithmWorkflowItem algoWFItem = (AlgorithmWorkflowItem) wfChildren[i];
                    algoWFItem
                            .setInternalId(((NormalWorkflow) targetWorkflowGUI
                                    .getWorkflow()).getUniqueInternalId());
                }
               
                WorkflowTreeItem workflowTreeItem = (WorkflowTreeItem) wfChildren[i];
                if (Constant.Workflow.equals(workflowTreeItem.getType())) {
                    targetWorkflowGUI.addChild(workflowTreeItem);
                }
            }
        }*/

	
    /**
     * Edit items that are selected
     */	
	private void saveSelection() {
		TreeItem[] items = WorkflowView.this.tree.getSelection();
		if (items.length != 1)
			return;
		WorkflowTreeItem itm = (WorkflowTreeItem) items[0].getData();
		String type = itm.getType();
		if (type == Constant.Workflow) {
			WorkflowGUI wfGUI = (WorkflowGUI) itm;
	
			WorkflowMaker savedState = new WorkflowMaker();
			savedState.save(wfGUI.getWorkflow());
		}
	}
	
    /**
     * Remove all of the items that are selected
     */
    private void removeSelection() {
		try {
			TreeItem[] items = WorkflowView.this.tree.getSelection();
			if (items.length != 1)
				return;
			WorkflowTreeItem itm = (WorkflowTreeItem) items[0].getData();
			String type = itm.getType();
			if (type == Constant.Workflow) {
				WorkflowGUI wfGUI = (WorkflowGUI) itm;
				WorkflowManager.getInstance().removeWorkflow(
						wfGUI.getWorkflow());// model
				itm.removeAllChildren();// GUI
				rootItem.removeChild(wfGUI);// GUI
				WorkflowView.this.viewer.refresh();
				if (WorkflowView.this.rootItem.getRootsChildren().length == 0
						|| WorkflowView.this.currentWorkFlowItem == wfGUI) {
					WorkflowView.this.addNewWorkflow("Workflow ");
				}
			} else if (type == Constant.AlgorithmUIItem) {
				AlgorithmItemGUI aiGUI = (AlgorithmItemGUI) itm;
				AlgorithmWorkflowItem wfItem = (AlgorithmWorkflowItem) aiGUI
						.getWfItem();
				Workflow wf = wfItem.getWorkflow();

				WorkflowTreeItem parent = itm.getParent();// GUI
				itm.removeAllChildren();
				parent.removeChild(itm);
				WorkflowView.this.viewer.refresh();
				wf.remove(wfItem);// model
				if (parent.getChildren().length == 0
						|| WorkflowView.this.currentParentItem == aiGUI) {
					WorkflowView.this.currentParentItem = parent;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
		

	protected String getMetaTypeID(ServiceReference ref) {
		String pid = (String) ref.getProperty(Constants.SERVICE_PID);
		String metatype_pid = (String) ref
				.getProperty(AlgorithmProperty.PARAMETERS_PID);
		if (metatype_pid == null) {
			metatype_pid = pid;
		}
		return metatype_pid;
	}

	@Override
	public void algorithmScheduled(Algorithm algorithm, Calendar time) {
	}

	@Override
	public void algorithmRescheduled(Algorithm algorithm, Calendar time) {
	}

	@Override
	public void algorithmUnscheduled(Algorithm algorithm) {
	}

	@Override
	public void algorithmStarted(Algorithm algorithm) {
	}

	public void addNewWorkflow(String name) {
	       
		Workflow workfFlow = WorkflowManager.getInstance().createWorkflow(name,
				Constant.NormalWorkflow);
		final WorkflowGUI dataItem = new WorkflowGUI(workfFlow,
				this.currentWorkFlowItem, 1, this.brandPluginID);
		this.currentWorkFlowItem = dataItem;
		this.currentParentItem = dataItem;
		this.rootItem.addChild(dataItem);
		refresh(dataItem);
	}
	
	

   
/*   *//**
    * Performs the Undo action. A new corresponding Redo step is automatically
    * pushed to the stack.
    *//*
   private void undo() {
       if (stack.hasUndo()) {
           isUndo = true;
           revertEvent(stack.popUndo());
           isUndo = false;
       }
   }

   *//**
    * Performs the Redo action. A new corresponding Undo step is automatically
    * pushed to the stack.
    *//*
   private void redo() {
       if (stack.hasRedo()) {
           isRedo = true;
           revertEvent(stack.popRedo());
           isRedo = false;
       }
   }*/

	@Override
	public void algorithmFinished(Algorithm algorithm, Data[] createdData) {
		if (mode == WorkflowMode.RUNNING)
			return;
		Dictionary<String, Object> parameters = null;
		if (algorithm instanceof AlgorithmWrapper) {

			AlgorithmWrapper algo = (AlgorithmWrapper) algorithm;
			parameters = algo.getParameters();
		}
		// get service reference
		ServiceReference serviceReference = Activator.getSchedulerService()
				.getServiceReference(algorithm);
		String algorithmLabel = "";
		if (serviceReference != null) {
			algorithmLabel = (String) serviceReference
					.getProperty(AlgorithmProperty.LABEL);
		}
		AlgorithmFactory factory = (AlgorithmFactory) Activator.getContext()
				.getService(serviceReference);

		String pid = (String) serviceReference
				.getProperty(Constants.SERVICE_PID);

		AlgorithmWorkflowItem wfi = new AlgorithmWorkflowItem(algorithmLabel,
				((NormalWorkflow) currentWorkFlowItem.getWorkflow())
						.getUniqueInternalId(), pid);
		wfi.setParameters(parameters);
		wfi.setWorkflow(currentWorkFlowItem.getWorkflow());
		currentWorkFlowItem.getWorkflow().add(wfi);

		final AlgorithmItemGUI dataItem = new AlgorithmItemGUI(wfi,
				this.currentParentItem, this.brandPluginID);
		this.currentParentItem.addChild(dataItem);
		this.currentParentItem = dataItem;
		refresh(dataItem);
		// Create algorithm parameters.
		String metatypePID = getMetaTypeID(serviceReference);
		// get the input parameters
		MetaTypeProvider provider = null;

		try {
			provider = getPossiblyMutatedMetaTypeProvider(metatypePID, pid,
					factory, serviceReference);
		} catch (AlgorithmCreationFailedException e) {
			String format = "An error occurred when creating the algorithm \"%s\" with the data you "
					+ "provided.  (Reason: %s)";
			String logMessage = String.format(format,
					serviceReference.getProperty(AlgorithmProperty.LABEL),
					e.getMessage());

			return;
		} catch (Exception e) {
			return;
		}

		if (parameters == null || parameters.isEmpty())
			return;

		final GeneralTreeItem paramItem = new GeneralTreeItem("Parameters",
				Constant.Label, dataItem, Utils.getImage("play.png",
						brandPluginID));
		dataItem.addChild(paramItem);
		ObjectClassDefinition obj = provider.getObjectClassDefinition(
				metatypePID, null);
		if (obj != null) {
			AttributeDefinition[] attr = obj
					.getAttributeDefinitions(ObjectClassDefinition.ALL);

			for (int i = 0; i < attr.length; i++) {
				String id = attr[i].getID();
				String name = attr[i].getName();
				// add this into the hashmap of Algorithm Item
				wfi.add(name, id);
				Object valueRaw = parameters.get(id);
				String value = "";
				if (valueRaw != null) {
					value = valueRaw.toString();
				}
				
				GeneralTreeItem paramName = new GeneralTreeItem(name,
						Constant.ParameterName, paramItem, Utils.getImage(
								"parameters.png", brandPluginID));
				paramItem.addChildren(paramName);
				GeneralTreeItem paramValue = new GeneralTreeItem(value,
						Constant.ParameterValue, paramName, Utils.getImage(
								"parameters.png", brandPluginID));
				paramName.addChildren(paramValue);
			}
		}

		refresh(paramItem);
	}

	private void refresh(final WorkflowTreeItem item) {
		guiRun(new Runnable() {
			public void run() {
				if (!tree.isDisposed()) {
					// update the TreeView
					WorkflowView.this.viewer.refresh();
					// update the global selection
					WorkflowView.this.viewer.expandToLevel(item, 0);
				}
			}
		});

	}

	@Override
	public void algorithmError(Algorithm algorithm, Throwable error) {
	}

	@Override
	public void schedulerRunStateChanged(boolean isRunning) {
	}

	@Override
	public void schedulerCleared() {
	}

	private void guiRun(Runnable run) {
		if (Thread.currentThread() == Display.getDefault().getThread()) {
			run.run();
		} else {
			Display.getDefault().syncExec(run);
		}
	}

	public WorkflowMode getMode() {
		return mode;
	}

	protected MetaTypeProvider getPossiblyMutatedMetaTypeProvider(
			String metatypePID, String pid, AlgorithmFactory factory,
			ServiceReference serviceRef)
			throws AlgorithmCreationFailedException {
		MetaTypeProvider provider = null;
		MetaTypeService metaTypeService = (MetaTypeService) Activator
				.getService(MetaTypeService.class.getName());
		if (metaTypeService != null) {
			provider = metaTypeService.getMetaTypeInformation(serviceRef
					.getBundle());
		}
		return provider;
	}

	/*
	 * Listens for right-clicks on TreeItems and opens the context menu when
	 * needed.
	 */
	private class ContextMenuListener extends MouseAdapter {
		public void mouseUp(MouseEvent event) {
			if (event.button == 3) {

				TreeItem item = WorkflowView.this.tree.getItem(new Point(
						event.x, event.y));

				if (item != null) {
					WorkflowView.this.menu.setVisible(true);
					WorkflowView.this.whiteSpacemenu.setVisible(false);
				} else {
					WorkflowView.this.menu.setVisible(false);
					WorkflowView.this.whiteSpacemenu.setVisible(true);
				}
			}
		}
	}

	public boolean isRootItem(WorkflowGUI wfg) {
		if (this.rootItem.equals(wfg))
			return true;
		return false;
	}

	public void UpdateUI() {
		ManageView mview = new ManageView();
		mview.updateUI(this.tree, this.viewer, this.brandPluginID);
	}

	public void addWorflowtoUI(Workflow wf) {
		ManageView mview = new ManageView();
		mview.addworkflow(this.rootItem, wf, this.brandPluginID);
		this.viewer.refresh();

	}

	private void handleInput() {
		// Clean up any previous editor control
		Control oldEditor = this.editor.getEditor();

		if (oldEditor != null) {
			oldEditor.dispose();
		}

		// Identify the selected row, only allow input if there is a single
		// selected row
		TreeItem[] selection = this.tree.getSelection();

		if (selection.length != 1) {
			return;
		}

		final TreeItem item = selection[0];

		if (item == null) {
			return;
		}

		this.newEditor = new Text(this.tree, SWT.NONE);
		this.newEditor.setText(item.getText());
		this.newEditor.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (!updatingTreeItem) {
					updateText(newEditor.getText(), item);
				}
			}
		});
		this.newEditor.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if ((e.character == SWT.CR)
						&& !WorkflowView.this.updatingTreeItem) {
					updateText(WorkflowView.this.newEditor.getText(), item);
				} else if (e.keyCode == SWT.ESC) {
					WorkflowView.this.newEditor.dispose();
				}
			}
		});
		this.newEditor.selectAll();
		this.newEditor.setFocus();
		this.editor.setEditor(this.newEditor, item);
	}

	private void updateText(String newLabel, TreeItem item) {
		this.updatingTreeItem = true;

		if (newLabel.startsWith(">"))
			newLabel = newLabel.substring(1);

		this.editor.getItem().setText(newLabel);
		WorkflowTreeItem wfTreeItem = (WorkflowTreeItem) item.getData();
		if (wfTreeItem.getType() == Constant.ParameterValue) {
			try {
				String paramName = wfTreeItem.getParent().getLabel();
				WorkflowTreeItem alfoITem = wfTreeItem.getParent().getParent()
						.getParent();

				AlgorithmWorkflowItem wfg = (AlgorithmWorkflowItem) ((AlgorithmItemGUI) alfoITem)
						.getWfItem();
				Object obj = wfg.getParameterValue(paramName);
				if (obj != null) {
					if (obj instanceof String) {
						obj = newLabel;
					} else if (obj instanceof Integer) {
						obj = Integer.parseInt(newLabel);
					} else if (obj instanceof java.lang.Boolean) {
						obj = Boolean.parseBoolean(newLabel);
					} else if (obj instanceof java.lang.Float) {
						obj = Float.parseFloat(newLabel);
					} else if (obj instanceof java.lang.Double) {
						obj = Double.parseDouble(newLabel);
					} else if (obj instanceof java.lang.Long) {
						obj = Long.parseLong(newLabel);
					} else if (obj instanceof java.lang.Short) {
						obj = Short.parseShort(newLabel);
					}
				} else {
					obj = newLabel;
				}
				wfg.addParameter(paramName, obj);
				wfTreeItem.setLabel(newLabel);
			} catch (Exception e) {
				viewer.refresh();
				this.newEditor.dispose();
				updatingTreeItem = false;
			}
		} else if (wfTreeItem.getType() == Constant.Workflow) {
			wfTreeItem.setLabel(newLabel);
			((WorkflowGUI) wfTreeItem).getWorkflow().setName(newLabel);
		}
		viewer.refresh();
		this.newEditor.dispose();
		updatingTreeItem = false;
	}

	private class DatamodelSelectionListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
		}
	}

	private class SaveListener implements Listener {
		public void handleEvent(Event event) {
			TreeItem[] items = WorkflowView.this.tree.getSelection();
			if (items.length != 1)
				return;
			WorkflowTreeItem itm = (WorkflowTreeItem) items[0].getData();
			String type = itm.getType();
			if (type == Constant.Workflow) {
				WorkflowGUI wfGUI = (WorkflowGUI) itm;

				WorkflowMaker savedState = new WorkflowMaker();
				savedState.save(wfGUI.getWorkflow());
			}
		}
	}

	private class LoadListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			if(WorkflowMode.RUNNING != null) {
			WorkflowMaker loadState = new WorkflowMaker();
			loadState.load();
			}
		}
	}

	private class DeleteListener implements Listener {

		@Override
		public void handleEvent(Event arg0) {
			try {
				TreeItem[] items = WorkflowView.this.tree.getSelection();
				if (items.length != 1)
					return;
				WorkflowTreeItem itm = (WorkflowTreeItem) items[0].getData();
				String type = itm.getType();
				if (type == Constant.Workflow) {
					WorkflowGUI wfGUI = (WorkflowGUI) itm;
					WorkflowManager.getInstance().removeWorkflow(
							wfGUI.getWorkflow());// model
					itm.removeAllChildren();// GUI
					rootItem.removeChild(wfGUI);// GUI
					WorkflowView.this.viewer.refresh();
					if (WorkflowView.this.rootItem.getRootsChildren().length == 0
							|| WorkflowView.this.currentWorkFlowItem == wfGUI) {
						WorkflowView.this.addNewWorkflow("Workflow ");
					}
				} else if (type == Constant.AlgorithmUIItem) {
					AlgorithmItemGUI aiGUI = (AlgorithmItemGUI) itm;
					AlgorithmWorkflowItem wfItem = (AlgorithmWorkflowItem) aiGUI
							.getWfItem();
					Workflow wf = wfItem.getWorkflow();

					WorkflowTreeItem parent = itm.getParent();// GUI
					itm.removeAllChildren();
					parent.removeChild(itm);
					WorkflowView.this.viewer.refresh();
					wf.remove(wfItem);// model
					if (parent.getChildren().length == 0
							|| WorkflowView.this.currentParentItem == aiGUI) {
						WorkflowView.this.currentParentItem = parent;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private class UndoListener implements Listener {
		public void handleEvent(Event event) {
			TreeItem[] items = WorkflowView.this.tree.getSelection();
			if (items.length != 1)
				return;
			WorkflowTreeItem itm = (WorkflowTreeItem) items[0].getData();
			String type = itm.getType();
			if (type == Constant.Workflow) {
				//this.rootItem.addChild(dataItem);
				//refresh(dataItem);
				undoStack.pop();
			}
		}
	}
	
    /* org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.
    * KeyEvent)
    */
   public void keyPressed(KeyEvent e) {
       // Listen to CTRL+Z for Undo, to CTRL+Y or CTRL+SHIFT+Z for Redo
       boolean isCtrl = (e.stateMask & SWT.CTRL) > 0;
       boolean isAlt = (e.stateMask & SWT.ALT) > 0;
       if (isCtrl && !isAlt) {
           boolean isShift = (e.stateMask & SWT.SHIFT) > 0;
           if (!isShift && e.keyCode == 'z') {
        	   undoStack.pop();
           } else if (!isShift && e.keyCode == 'y' || isShift
                   && e.keyCode == 'z') {
        	  // undoStack.push(dataItem);
           }
       }
   }

	private class RunListener implements Listener {
		public void handleEvent(Event event) {
			TreeItem[] items = WorkflowView.this.tree.getSelection();
			if (items.length != 1)
				return;
			WorkflowTreeItem itm = (WorkflowTreeItem) items[0].getData();
			String type = itm.getType();
			if (type == Constant.Workflow) {
				WorkflowView.this.mode = WorkflowMode.RUNNING;
				((WorkflowGUI) itm).getWorkflow().run();
				WorkflowView.this.mode = WorkflowMode.STOPPED;
			}
		}
	}
	
	private class PauseListener implements Listener {
		public void handleEvent(Event event) {
			TreeItem[] items = WorkflowView.this.tree.getSelection();
			if (items.length != 1)
				return;
			WorkflowTreeItem itm = (WorkflowTreeItem) items[0].getData();
			String type = itm.getType();
			if (type == Constant.Workflow) {
				WorkflowView.this.mode = WorkflowMode.RUNNING;
				pauseStateButton.setEnabled(false);
				playStateButton.setEnabled(true);
				//((WorkflowGUI) itm).getWorkflow().run();
				//WorkflowView.this.mode = WorkflowMode.STOPPED;
			}
		}
	}

	private class NewWorkflow implements Listener {
		public void handleEvent(Event event) {
			WorkflowView.this.addNewWorkflow("Workflow ");
		}
	}

	@Override
	public void setFocus() {
	}
}
