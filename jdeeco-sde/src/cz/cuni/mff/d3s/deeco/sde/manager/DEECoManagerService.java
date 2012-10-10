package cz.cuni.mff.d3s.deeco.sde.manager;

import java.util.LinkedList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.ComponentContext;

import cz.cuni.mff.d3s.deeco.provider.AbstractDEECoObjectProvider;
import cz.cuni.mff.d3s.deeco.runtime.Runtime;
import cz.cuni.mff.d3s.deeco.sde.console.ConsolePrinter;

public class DEECoManagerService {

	private List<AbstractDEECoObjectProvider> providers;

	private Runtime rt;
	private ConsolePrinter cp;

	private static DEECoManagerService instance;
	private ClassLoader thisBundleLoader;

	public static DEECoManagerService getInstance() {
		return instance;
	}

	protected void activate(ComponentContext context) {
		Bundle b = context.getBundleContext().getBundle();
		thisBundleLoader = b.adapt(BundleWiring.class).getClassLoader();
		System.out.println("JDEECo SDE Tool activated");
	}

	public DEECoManagerService() {
		providers = new LinkedList<AbstractDEECoObjectProvider>();
		instance = this;
	}

	public synchronized void addDEECoPrimitivesProvider(Object dpp) {
		if (dpp != null && dpp instanceof AbstractDEECoObjectProvider) {
			providers.add((AbstractDEECoObjectProvider) dpp);
			System.out.println("Provider added: " + dpp);
		}
	}

	public synchronized void removeDEECoPrimitivesProvider(Object dpp) {
		if (dpp != null && dpp instanceof AbstractDEECoObjectProvider) {
			if (providers.contains(dpp)) {
				providers.remove(dpp);
				System.out.println("Provider removed: " + dpp);
			}
		}
	}

	public synchronized void registerRuntime(Object rt) {
		unregisterRuntime(null);
		this.rt = (Runtime) rt;
		System.out.println("Runtime registered");
	}

	public synchronized void unregisterRuntime(Object rt) {
		if (this.rt != null) {
			this.rt.stopRuntime();
			this.rt = null;
			System.out.println("Runtime unregistered");
		}
	}

	public List<AbstractDEECoObjectProvider> getProviders() {
		return providers;
	}

	public Runtime getRuntime() {
		return rt;
	}

	public ClassLoader getThisBundleLoader() {
		return thisBundleLoader;
	}

	public void openConsole() {
		if (cp == null)
			cp = new ConsolePrinter();
		System.setErr(cp.openConsoleWindow());
		System.setOut(cp.openConsoleWindow());
	}
}