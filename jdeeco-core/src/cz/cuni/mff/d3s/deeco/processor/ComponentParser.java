package cz.cuni.mff.d3s.deeco.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.bcel.generic.FieldObserver;

import cz.cuni.mff.d3s.deeco.annotations.ELockingMode;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.annotations.StrongLocking;
import cz.cuni.mff.d3s.deeco.annotations.TimeStamps;
import cz.cuni.mff.d3s.deeco.annotations.WeakLocking;
import cz.cuni.mff.d3s.deeco.exceptions.KMAccessException;
import cz.cuni.mff.d3s.deeco.invokable.ParameterizedMethod;
import cz.cuni.mff.d3s.deeco.invokable.SchedulableComponentProcess;
import cz.cuni.mff.d3s.deeco.knowledge.Component;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgePathHelper;
import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.deeco.performance.ComponentKnowledgeDecomposer;
import cz.cuni.mff.d3s.deeco.performance.KnowledgeInfo;
import cz.cuni.mff.d3s.deeco.scheduling.ProcessPeriodicSchedule;
import cz.cuni.mff.d3s.deeco.scheduling.ProcessSchedule;

public class ComponentParser {
	
	
	public static List<String> extractComponentFields(Class<?> c, String root, KnowledgeManager km){
	    Field fs[] = c.getFields();
	    List<String> result = new ArrayList<String>();
	    for (Field f : fs) {
	      TimeStamps a = f.getAnnotation(TimeStamps.class);
	      if (a != null) {
	        String st = KnowledgePathHelper.appendToRoot(root, f.getName());
	        result.add(st);
	      }
	    }
	    return result;
	}

	/**
	 * Static function used to extract {@link SchedulableComponentProcess}
	 * instance from the class definition
	 * 
	 * @param c
	 *            class to be parsed for extraction
	 * @param root
	 *            component id for which process executes
	 * @param km
	 *            {@link KnowledgeManager} instance that is used for knowledge
	 *            repository communication
	 * @return list of {@link SchedulableComponentProcess} instances extracted
	 *         from the class definition
	 */
	
	public static List<SchedulableComponentProcess> extractComponentProcess(
			Class<?> c, String root) {
		
		if (c == null) {
			return null;
		}

		
		List<Method> methods = AnnotationHelper.getAnnotatedMethods(c,
				Process.class);

		if (methods == null || methods.size() == 0) {
			return null;
		}

		final List<SchedulableComponentProcess> result = new ArrayList<SchedulableComponentProcess>();

		for (Method m : methods) {

			final ParameterizedMethod currentMethod = ParserHelper
					.extractParametrizedMethod(m, root);

			if (currentMethod == null) {
				// Not a process method
				continue;
			}

			ProcessSchedule ps = null;
			final ProcessSchedule periodicSchedule = ScheduleHelper
					.getPeriodicSchedule(AnnotationHelper.getAnnotation(
							PeriodicScheduling.class, m.getAnnotations()));
			if (periodicSchedule != null) {
				ps = periodicSchedule;
			}

			if (ps == null) {
				final ProcessSchedule triggeredSchedule = ScheduleHelper
						.getTriggeredSchedule(m.getParameterAnnotations(),
								currentMethod.in, currentMethod.inOut);
				if (triggeredSchedule != null) {
					ps = triggeredSchedule;
				}
			}

			if (ps == null) {
				// No scheduling specified by annotations, using defaults
				ps = new ProcessPeriodicSchedule();
			}

			ELockingMode lm;
			if (AnnotationHelper.getAnnotation(StrongLocking.class,
					m.getAnnotations()) == null) {
				if (AnnotationHelper.getAnnotation(WeakLocking.class,
						m.getAnnotations()) == null)
					lm = (ps instanceof ProcessPeriodicSchedule) ? ELockingMode.WEAK
							: ELockingMode.STRONG;
				else
					lm = ELockingMode.WEAK;
			} else {
				lm = ELockingMode.STRONG;
			}

			final SchedulableComponentProcess skp = new SchedulableComponentProcess(
					null, ps, currentMethod, lm, root, null);
			result.add(skp);
		}

		return result;
	}

	/**
	 * Retrieves init method from the <code>Component</code> class.
	 * 
	 * @param c
	 *            class to be parsed
	 * @return init method or null in case no matching found
	 */
	public static Component extractInitialKnowledge(Class<?> c) {
		Component ck;
		try {
			Constructor<?> constructor = c.getConstructor();
			if (constructor != null) {
				ck = (Component) constructor.newInstance(new Object[] {});
				assignUIDIfNotSet(ck);
				return ck;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.w("No initial state can be retrieved for the component: " + c);
		return null;
	}

	public static boolean isComponentDefinition(Class<?> clazz) {
		return clazz != null && Component.class.isAssignableFrom(clazz);
	}

	// ------------- Private functions -------------------

	private static void assignUIDIfNotSet(Component ck) {
		if (ck.id == null || ck.id.equals(""))
			ck.id = UUID.randomUUID().toString();
	}

}
