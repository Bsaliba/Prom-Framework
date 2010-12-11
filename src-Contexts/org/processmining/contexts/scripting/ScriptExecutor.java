package org.processmining.contexts.scripting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.annotations.CLI;
import org.processmining.framework.util.StringUtils;

import bsh.EvalError;
import bsh.Interpreter;

public class ScriptExecutor {

	private Interpreter interpreter;
	private final PluginContext context;
	private Set<Signature> availablePlugins;

	public class ScriptExecutionException extends Exception {

		private static final long serialVersionUID = -4777627419215658865L;

		public ScriptExecutionException(EvalError e) {
			super(e);
		}
	}

	public ScriptExecutor(PluginContext context) throws ScriptExecutionException {
		this.context = context;
		init();
	}

	public void execute(String script) throws ScriptExecutionException {
		try {
			interpreter.eval(script);
		} catch (EvalError e) {
			throw new ScriptExecutionException(e);
		}
	}

	private void init() throws ScriptExecutionException {
		StringBuffer init = new StringBuffer();
		String nl = System.getProperty("line.separator");
		int pluginIndex = 0;

		interpreter = new Interpreter();
		availablePlugins = new HashSet<Signature>();

		try {
			interpreter.set("__main_context", context);
			for (PluginDescriptor plugin : context.getPluginManager().getAllPlugins()) {
				// the right context type is checked at start by the
				// pluginmanager
				// if
				// (plugin.getContextType().isAssignableFrom(context.getClass()))
				// {
				for (int j = 0; j < plugin.getParameterTypes().size(); j++) {
					Signature signature = getSignature(plugin, j);

					if (!availablePlugins.contains(signature)) {
						availablePlugins.add(signature);
						pluginIndex++;

						interpreter.set("__plugin_descriptor" + pluginIndex, plugin);
						interpreter.set("__plugin_method_index" + pluginIndex, j);

						if (signature.getReturnTypes().size() == 1) {
							init.append(Object.class.getCanonicalName());
						} else {
							init.append(Object[].class.getCanonicalName());
						}
						init.append(" " + signature.getName() + "(");

						int index = 0;
						for (Class<?> cl : signature.getParameterTypes()) {
							if (index > 0) {
								init.append(", ");
							}
							init.append(cl.getCanonicalName());
							init.append(" p" + index++);
						}
						init.append(") {" + nl);
						init.append("    " + PluginContext.class.getCanonicalName()
								+ " context = __main_context.createChildContext(\"Result of ");
						init.append(signature.getName() + "\");" + nl);

						init.append("    __plugin_descriptor" + pluginIndex + ".invoke(__plugin_method_index"
								+ pluginIndex + ", context, new " + (Object[].class.getCanonicalName()) + " { ");
						for (int i = 0; i < signature.getParameterTypes().size(); i++) {
							if (i > 0) {
								init.append(", ");
							}
							init.append("p" + i);
						}
						init.append(" });" + nl);

						if (signature.getReturnTypes().size() > 1) {
							init.append("    context.getResult().synchronize();" + nl);
							init.append("    " + Object[].class.getCanonicalName() + " result = new "
									+ Object.class.getCanonicalName() + "[context.getResult().getSize()];" + nl);
							init
									.append("    for (int i = 0; i < result.length; i++) { result[i] = context.getResult().getResult(i); }"
											+ nl);
							init.append("    return result;" + nl);
						} else {
							init.append("    return context.getFutureResult(0).get();" + nl);
						}
						init.append("}" + nl);
					}
				}
			}
			// }
			// System.out.println(init);
			interpreter.eval(init.toString());
		} catch (EvalError e) {
			throw new ScriptExecutionException(e);
		}
	}

	private Signature getSignature(PluginDescriptor plugin, int index) {
		String name;

		if (plugin.hasAnnotation(CLI.class)) {
			name = plugin.getAnnotation(CLI.class).functionName();
		} else {
			name = plugin.getName();
		}
		return new Signature(plugin.getReturnTypes(), StringUtils.getJavaIdentifier(name), plugin
				.getParameterTypes(index));
	}

	public void bind(String name, Object value) throws ScriptExecutionException {
		try {
			interpreter.set(name, value);
		} catch (EvalError e) {
			throw new ScriptExecutionException(e);
		}
	}

	public List<Signature> getAvailablePlugins() {
		List<Signature> result = new ArrayList<Signature>(availablePlugins);

		Collections.sort(result, new Comparator<Signature>() {
			public int compare(Signature a, Signature b) {
				return a.getName().compareTo(b.getName());
			}
		});
		return result;
	}

}
