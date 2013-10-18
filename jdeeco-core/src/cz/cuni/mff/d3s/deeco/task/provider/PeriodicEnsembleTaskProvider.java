package cz.cuni.mff.d3s.deeco.task.provider;

import cz.cuni.mff.d3s.deeco.model.Ensemble;
import cz.cuni.mff.d3s.deeco.monitoring.MonitorProvider;
import cz.cuni.mff.d3s.deeco.task.EnsembleTask;
import cz.cuni.mff.d3s.deeco.task.Task;

public class PeriodicEnsembleTaskProvider extends PeriodicTaskProvider {

	private final Ensemble ensemble;
	private final String coordinator;
	private final String member;

	public PeriodicEnsembleTaskProvider(Ensemble ensemble, String coordinator,
			String member, MonitorProvider monitorProvider) {
		super(monitorProvider);
		this.ensemble = ensemble;
		this.coordinator = coordinator;
		this.member = member;
	}

	@Override
	public Task getTask() {
		EnsembleTask task = new EnsembleTask(ensemble, coordinator, member);
		task.setListener(monitorProvider.getExchangeMonitor(task));
		return task;
	}

	@Override
	public long getPeriod() {
		return ensemble.getSchedule().getPeriod();
	}

}