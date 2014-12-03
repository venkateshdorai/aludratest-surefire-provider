package org.aludratest.maven.surefire;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aludratest.AludraTest;
import org.aludratest.scheduler.RunnerListenerRegistry;
import org.aludratest.scheduler.node.RunnerLeaf;
import org.aludratest.scheduler.util.CommonRunnerLeafAttributes;
import org.apache.maven.surefire.util.ReflectionUtils;

final class AludraTestReflectionUtil {

	private AludraTestReflectionUtil() {
	}

	public static Class<?> getAludraTestClass(ClassLoader classLoader, String className) {
		return ReflectionUtils.tryLoadClass(classLoader, className);
	}

	public static void runAludraTest(Object aludraTest, Class<?> testOrSuiteClass) throws InvocationTargetException {
		Method m = ReflectionUtils.getMethod(aludraTest, "run", new Class<?>[] { Class.class });

		try {
			m.invoke(aludraTest, testOrSuiteClass);
		}
		catch (InvocationTargetException e) {
			throw e;
		}
		catch (Exception e) {
			throw new InvocationTargetException(e);
		}
	}

	public static void registerRunnerListener(Object runnerListener, Object aludraTest, Class<?> runnerListenerClass)
			throws InvocationTargetException {
		Object runnerListenerRegistry = getImplementorInstance(aludraTest, RunnerListenerRegistry.ROLE);
		
		// call the add method
		ReflectionUtils.invokeSetter(runnerListenerRegistry, "addRunnerListener", runnerListenerClass, runnerListener);
	}

	public static Object startFramework(ClassLoader classLoader) throws InvocationTargetException {
		try {
			Class<?> clazz = classLoader.loadClass(AludraTest.class.getName());
			return clazz.getMethod("startFramework").invoke(null);
		}
		catch (InvocationTargetException e) {
			throw e;
		}
		catch (Exception e) {
			throw new InvocationTargetException(e);
		}
	}

	private static Object getImplementorInstance(Object aludraTest, String className) throws InvocationTargetException {
		try {
			Object serviceManager = ReflectionUtils.invokeGetter(aludraTest, "getServiceManager");
			Method m = ReflectionUtils.getMethod(serviceManager, "newImplementorInstance", new Class<?>[] { Class.class });
			return m.invoke(serviceManager, serviceManager.getClass().getClassLoader().loadClass(className));
		}
		catch (InvocationTargetException e) {
			throw e;
		}
		catch (Exception e) {
			throw new InvocationTargetException(e);
		}
	}

	public static void stopFramework(Object aludraTest) throws InvocationTargetException {
		try {
			ReflectionUtils.getMethod(aludraTest, "stopFramework", new Class[0]).invoke(aludraTest);
		}
		catch (InvocationTargetException e) {
			throw e;
		}
		catch (Exception e) {
			throw new InvocationTargetException(e);
		}
	}

	public static String getTestClassName(Object runnerLeaf) {
		Object testInvoker = ReflectionUtils.invokeGetter(runnerLeaf, "getTestInvoker");
		Class<?> testClass = (Class<?>) ReflectionUtils.invokeGetter(testInvoker, "getTestClass");
		return testClass.getName();
	}

	public static String getName(Object runnerGroupOrLeaf) {
		return (String) ReflectionUtils.invokeGetter(runnerGroupOrLeaf, "getName");
	}

	public static String getRootName(Object runnerTree) {
		return getName(ReflectionUtils.invokeGetter(runnerTree, "getRoot"));
	}

	public static boolean groupContainsLeafs(Object runnerGroup) {
		Iterable<?> ls = (Iterable<?>) ReflectionUtils.invokeGetter(runnerGroup, "getChildren");
		for (Object o : ls) {
			if (o != null && o.getClass().getName().equals(RunnerLeaf.class.getName())) {
				return true;
			}
		}

		return false;
	}

	public static String getParentName(Object runnerGroup) {
		Object parent = ReflectionUtils.invokeGetter(runnerGroup, "getParent");
		return parent == null ? getName(runnerGroup) : getName(parent);
	}

	public static boolean isIgnored(Object runnerLeaf) {
		try {
			Method m = runnerLeaf.getClass().getMethod("getAttribute", String.class);
			Boolean value = (Boolean) m.invoke(runnerLeaf, CommonRunnerLeafAttributes.IGNORE);
			return value != null && Boolean.TRUE.equals(value);
		}
		catch (Throwable t) {
			return false;
		}
	}

}
