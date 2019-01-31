package org.metalscraps.eso.lang.tool.config;

public enum WebPageNames {
	ChampionPointDisciplines("ChampionPointDisciplines"),
	ChampionPointSkillDescriptions("ChampionPointSkillDescriptions"),
	ChampionPointSkills("ChampionPointSkills"),
	QuestItems("QuestItems"),
	QuestSteps("QuestSteps"),
	Quests("Quests"),
	Recipes("Recipes"),
	SetSummaries("SetSummaries"),
	SkillTree("SkillTree");

	public String name;

	WebPageNames(String name) {
		this.name = name;
	}

	@Override
	public String toString() { return this.name; }
}
