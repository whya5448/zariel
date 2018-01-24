package org.metalscraps.eso.lang.kr.config;

/**
 * Created by 안병길 on 2018-01-20.
 * Whya5448@gmail.com
 */
public enum FileNames {

	EsoUI_Client("00_EsoUI_Client"),
	EsoUI_Pregame("00_EsoUI_Pregame"),
	achievement("achievement"),
	book("book"),
	bookOther("book-other"),
	chat("chat"),
	color("color"),
	countryOrRegion("country-or-region"),
	emote("emote"),
	greeting("greeting"),
	interactAction("interact-action"),
	interactWin("interact-win"),
	item("item"),
	itemCrate("item-crate"),
	itemCrown("item-crown"),
	itemCrownOther("item-crown-other"),
	itemCrownPack("item-crown-pack"),
	itemCrownPackOther("item-crown-pack-other"),
	itemOther("item-other"),
	itemQuest("item-quest"),
	itemQuestOther("item-quest-other"),
	itemType("item-type"),
	journey("journey"),
	journeyDetail("journey-detail"),
	journeyOther("journey-other"),
	letter("letter"),
	loadscreen("loadscreen"),
	loadscreenOther("loadscreen-other"),
	locationAndObject("location-and-object"),
	locationObject("location-object"),
	moreDesc("more-desc"),
	moreUi("more-ui"),
	npcName("npc-name"),
	npcOther("npc-other"),
	npcTalk("npc-talk"),
	other("other"),
	popupTip("popup-tip"),
	popupTipOther("popup-tip-other"),
	questEnd("quest-end"),
	questMain1("quest-main-1"),
	questMain2("quest-main-2"),
	questMain3("quest-main-3"),
	questMain4("quest-main-4"),
	questMain5("quest-main-5"),
	questMain6("quest-main-6"),
	questObj("quest-obj"),
	questStart("quest-start"),
	questSub("quest-sub"),
	questSubObj("quest-sub-obj"),
	set("set"),
	skill("skill"),
	skillOther("skill-other"),
	subtitle("subtitle"),
	threeAlliance("three-alliance"),
	tip("tip"),
	title("title"),
	trap("trap"),
	treasureMap("treasure-map");

	public String name;

	FileNames(String name) {
		this.name = name;
	}

	@Override
	public String toString() { return this.name; }
	public String toStringPO() { return this.name + ".po"; }
	public String toStringPO2() { return this.name + ".po2"; }
}
