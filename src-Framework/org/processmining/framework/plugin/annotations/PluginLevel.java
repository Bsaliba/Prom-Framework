package org.processmining.framework.plugin.annotations;

public enum PluginLevel {
	Default( //
			"Default", //
			1), //
	WorksForDeveloperOnSomeCase( //
			"WorksForDeveloperOnSomeCase", //
			2), //
	WorksForDeveloperOnDeveloperCases( //
			"WorksForDeveloperOnDeveloperCases", //
			3), //
	WorksForDeveloperOnSupervisorCases( //
			"WorksForDeveloperOnSupervisorCases", //
			4), //
	WorksForSupervisorOnSupervisorCases( //
			"WorksForSupervisorOnSupervisorCases", //
			5), //
	WorksForColleaguesOnColleagueCases( //
			"WorksForColleaguesOnColleagueCases", //
			5), //
	BulletProof( //
			"BulletProof", //
			6);

	private final String name;
	private final int value;

	private PluginLevel(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}

}
