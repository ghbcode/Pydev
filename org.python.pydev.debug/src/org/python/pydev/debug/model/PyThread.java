/*
 * Author: atotic
 * Created on Apr 21, 2004
 * License: Common Public License v1.0
 */
package org.python.pydev.debug.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.tasklist.ITaskListResourceAdapter;
import org.python.pydev.debug.model.remote.AbstractDebuggerCommand;
import org.python.pydev.debug.model.remote.AbstractRemoteDebugger;
import org.python.pydev.debug.model.remote.StepCommand;
import org.python.pydev.debug.model.remote.ThreadRunCommand;
import org.python.pydev.debug.model.remote.ThreadSuspendCommand;
import org.python.pydev.plugin.PydevPlugin;

/**
 * Represents python threads.
 * Stack global variables are associated with threads.
 */
public class PyThread extends PlatformObject implements IThread {

	private AbstractDebugTarget target;
	private String name;
	private String id;

    /**
     * true if this is a debugger thread, that can't be killed/suspended
     */
	private boolean isPydevThread;

	private boolean isSuspended = false;
	private boolean isStepping = false;
	private IStackFrame[] stack;
	
	public PyThread(AbstractDebugTarget target, String name, String id) {
		this.target = target;
		this.name = name;
		this.id = id;
		isPydevThread = id.equals("-1");	// use a special id for pydev threads
	}

	/**
	 * If a thread is entering a suspended state, pass in the stack
	 */
	public void setSuspended(boolean state, IStackFrame[] stack) {
		isSuspended = state;
		this.stack = stack;
	}

	public String getName() throws DebugException {
		return name;
	}
	
	public String getId() {
		return id;
	}
	
	public boolean isPydevThread() {
		return isPydevThread;
	}

	public int getPriority() throws DebugException {
		return 0;
	}

	public String getModelIdentifier() {
		return target.getModelIdentifier();
	}

	public IDebugTarget getDebugTarget() {
		return target;
	}

	public ILaunch getLaunch() {
		return target.getLaunch();
	}

	public boolean canTerminate() {
		return !isPydevThread;
	}

	public boolean isTerminated() {
		return target.isTerminated();
	}

	public void terminate() throws DebugException {
		target.terminate();
	}

	public boolean canResume() {
		return !isPydevThread && isSuspended;
	}

	public boolean canSuspend() {
		return !isPydevThread && !isSuspended;
	}

	public boolean isSuspended() {
		return isSuspended;
	}

	public void resume() throws DebugException {
		if (!isPydevThread) {
			stack = null;
			isStepping = false;
			//RemoteDebugger d = target.getDebugger();
			AbstractRemoteDebugger d = target.getDebugger();
			if(d != null){
				d.postCommand(new ThreadRunCommand(d, id));
			}else{//no debugger?
				PydevPlugin.log("Terminating: No debugger in target when resuming.");
				terminate();
			}
		}
	}

	public void suspend() throws DebugException {
		if (!isPydevThread) {
			stack = null;
			AbstractRemoteDebugger d = target.getDebugger();
			d.postCommand(new ThreadSuspendCommand(d, id));
		}
	}

	public boolean canStepInto() {
		return !isPydevThread && isSuspended;
	}

	public boolean canStepOver() {
		return !isPydevThread && isSuspended;
	}

	public boolean canStepReturn() {
		return !isPydevThread && isSuspended;
	}

	public boolean isStepping() {
		return isStepping;
	}

	public void stepInto() throws DebugException {
		if (!isPydevThread) {
			isStepping = true;
			AbstractRemoteDebugger d = target.getDebugger();
			d.postCommand(new StepCommand(d, AbstractDebuggerCommand.CMD_STEP_INTO, id));
		}		
	}

	public void stepOver() throws DebugException {
		if (!isPydevThread) {
			isStepping = true;
			AbstractRemoteDebugger d = target.getDebugger();
			d.postCommand(new StepCommand(d, AbstractDebuggerCommand.CMD_STEP_OVER, id));
		}		
	}

	public void stepReturn() throws DebugException {
		if (!isPydevThread) {
			isStepping = true;
			AbstractRemoteDebugger d = target.getDebugger();
			d.postCommand(new StepCommand(d, AbstractDebuggerCommand.CMD_STEP_RETURN, id));
		}		
	}

	public IStackFrame[] getStackFrames() throws DebugException {
        if(isSuspended && stack != null){
            return stack;
        }
        return new IStackFrame[0];
	}

	public boolean hasStackFrames() throws DebugException {
		return (stack != null && stack.length > 0);
	}

	public IStackFrame getTopStackFrame() throws DebugException {
		return stack == null ? null : stack[0];
	}

	public PyStackFrame findStackFrameByID(String id) {
		if (stack != null) {
            
			for (int i=0; i<stack.length; i++){
                
				if (id.equals(((PyStackFrame)stack[i]).getId())){
                    
					return (PyStackFrame)stack[i];
                }
            }
        }
		return null;
	}

	public IBreakpoint[] getBreakpoints() {
		// should return breakpoint that caused this thread to suspend
		// not implementing this seems to cause no harm
		PyBreakpoint[] breaks = new PyBreakpoint[0];
		return breaks;
	}

	public Object getAdapter(Class adapter) {
		AdapterDebug.print(this, adapter);
		
		if (adapter.equals(ILaunch.class) ||
			adapter.equals(IResource.class)){
			return target.getAdapter(adapter);
		}else if (adapter.equals(ITaskListResourceAdapter.class)){
			return null;
		}else if (adapter.equals(IPropertySource.class) 
				|| adapter.equals(ITaskListResourceAdapter.class)
				|| adapter.equals(org.eclipse.debug.ui.actions.IToggleBreakpointsTarget.class)
				|| adapter.equals(org.eclipse.debug.ui.actions.IRunToLineTarget.class)
				|| adapter.equals(org.eclipse.ui.IContributorResourceAdapter.class)
				|| adapter.equals(org.eclipse.ui.model.IWorkbenchAdapter.class)
				|| adapter.equals(org.eclipse.ui.IActionFilter.class)
				) {
			return  super.getAdapter(adapter);
		}
		//Platform.getAdapterManager().getAdapter(this, adapter);
		AdapterDebug.printDontKnow(this, adapter);
		// ongoing, I do not fully understand all the interfaces they'd like me to support
		return super.getAdapter(adapter);
	}

}
