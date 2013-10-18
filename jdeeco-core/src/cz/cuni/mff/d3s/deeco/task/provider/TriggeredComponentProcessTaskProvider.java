package cz.cuni.mff.d3s.deeco.task.provider;

import java.util.List;

import cz.cuni.mff.d3s.deeco.model.ComponentProcess;
import cz.cuni.mff.d3s.deeco.model.Trigger;
import cz.cuni.mff.d3s.deeco.monitoring.MonitorProvider;
import cz.cuni.mff.d3s.deeco.task.ComponentProcessTask;
import cz.cuni.mff.d3s.deeco.task.Task;

public class TriggeredComponentProcessTaskProvider extends
		TriggeredTaskProvider {

	private final ComponentProcess componentProcess;

	public TriggeredComponentProcessTaskProvider(
			ComponentProcess componentProcess, MonitorProvider monitorProvider) {
		super(monitorProvider);
		this.componentProcess = componentProcess;
	}

	public Task getTask(String componentId) {
		ComponentProcessTask task = new ComponentProcessTask(componentProcess,
				componentId);
		task.setListener(monitorProvider.getProcessMonitor(task));
		return task;
	}

	@Override
	public List<Trigger> getTriggers() {
		return componentProcess.getSchedule().getTriggers();
	}
}