package org.metalscraps.eso.lang.kr.config;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Created by 안병길 on 2018-01-20.
 * Whya5448@gmail.com
 */
public enum FileNames {

	EsoUI_Client("00_EsoUI_Client", "UI_C"),
	EsoUI_Pregame("00_EsoUI_Pregame", "UI_P"),
	achievement("achievement", "achi"),
	book("book", "bk"),
	bookOther("book-other", "bk-ot"),
	chat("chat", "ch"),
	color("color", "co"),
	countryOrRegion("country-or-region", "CoR"),
	emote("emote", "emo"),
	greeting("greeting", "gret"),
	interactAction("interact-action", "in-act"),
	interactWin("interact-win", "in-win"),
	item("item", "it"),
	itemCrate("item-crate", "i-cra"),
	itemCrown("item-crown", "i-cro"),
	itemCrownOther("item-crown-other", "i-cr-ot"),
	itemCrownPack("item-crown-pack", "i-cr-pa"),
	itemCrownPackOther("item-crown-pack-other", "i-cr-pa-ot"),
	itemOther("item-other", "i-ot"),
	itemQuest("item-quest", "i-qu"),
	itemQuestOther("item-quest-other", "it-qu-ot"),
	itemType("item-type", "it-ty"),
	journey("journey", "jo"),
	journeyDetail("journey-detail", "jo-de"),
	journeyOther("journey-other", "jo-ot"),
	letter("letter", "let"),
	loadscreen("loadscreen", "lod"),
	loadscreenOther("loadscreen-other", "lod-o"),
	locationAndObject("location-and-object", "loc-A-o"),
	locationObject("location-object", "loc-o"),
	moreDesc("more-desc", "mo-dec"),
	moreUi("more-ui", "mo-ui"),
	npcName("npc-name", "n-nm"),
	npcOther("npc-other", "n-ot"),
	npcTalk("npc-talk", "n-tk"),
	other("other", "ot"),
	popupTip("popup-tip", "p-ti"),
	popupTipOther("popup-tip-other", "p-t-o"),
	questEnd("quest-end", "q-ed"),
	questMain1("quest-main-1", "q-m-1"),
	questMain2("quest-main-2", "q-m-2"),
	questMain3("quest-main-3", "q-m-3"),
	questMain4("quest-main-4", "q-m-4"),
	questMain5("quest-main-5", "q-m-5"),
	questMain6("quest-main-6", "q-m-6"),
	questObj("quest-obj", "q-ob"),
	questStart("quest-start", "q-st"),
	questSub("quest-sub", "q-sb"),
	questSubObj("quest-sub-obj", "q-sb-ob"),
	set("set"),
	skill("skill", "ski"),
	skillOther("skill-other", "ski-o"),
	subtitle("subtitle", "subt"),
	threeAlliance("three-alliance", "th-ali"),
	tip("tip"),
	title("title", "tit"),
	trap("trap", "tr"),
	treasureMap("treasure-map", "tre-map");

	@Getter(AccessLevel.PUBLIC)
	private String name, shortName;

	FileNames(String name) {
		this.name = name;
		this.shortName = name;
	}

	FileNames(String name, String shortName) {
		this.name = name;
		this.shortName = shortName;
	}

	public static FileNames fromString(String text) {
		for (FileNames b : FileNames.values()) if (b.name.equalsIgnoreCase(text)) return b;
		return null;
	}

	@Override
	public String toString() { return this.name; }
	public String toStringPO() { return this.name + ".po"; }
	public String toStringPO2() { return this.name + ".po2"; }
}
