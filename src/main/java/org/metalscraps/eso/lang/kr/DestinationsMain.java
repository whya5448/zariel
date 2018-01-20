package org.metalscraps.eso.lang.kr;

import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.metalscraps.eso.lang.kr.bean.PO;
import org.metalscraps.eso.lang.kr.config.AppConfig;
import org.metalscraps.eso.lang.kr.config.FileNames;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 안병길 on 2018-01-15.
 * Whya5448@gmail.com
 */


public class DestinationsMain {

	@Data
	private class Destinations implements Comparable {

		Destinations(int id, String questName) {
			this.id = id;
			this.questName = questName;
		}

		private Integer id;
		private String questName;


		@Override
		public int compareTo(Object o) {
			return this.id.compareTo(((Destinations)o).getId());
		}
	}


	private void start() {

		final String pathOfPO = "C:/Users/admin/Documents/Elder Scrolls Online/live/works/EsoExtractData v0.31/PO_0113";
		final String pathOfDesti = "C:/Users/admin/Documents/Elder Scrolls Online/live/AddOns/Destinations/data/KR";

		HashMap<String, Destinations> sourceList = new HashMap<>();
		HashMap<String, PO> zanataList = new HashMap<>();

		try {

			// 자나타 매핑된 PO.
			FileNames[] fileNames = {
					FileNames.journey,
					FileNames.journeyOther
			};

			// 데스티네이션 EN 복사해서 KR 폴더 생성.
			File s_questName = new File(pathOfDesti+"/DestinationsQuests_kr.lua");

			String destinationQuestSource = FileUtils.readFileToString(s_questName, AppConfig.CHARSET);

			String destiPattern = "\\[(\\d+)] = \\{\"(.*)\"}";

			// Desti
			Pattern p = Pattern.compile(destiPattern, Pattern.MULTILINE);
			Matcher m = p.matcher(destinationQuestSource);
			while (m.find()) sourceList.put(m.group(1), this.new Destinations(Integer.parseInt(m.group(1)), m.group(2)));

			// Zanata
			for(FileNames fn : fileNames) {
				String zanataQuetsSource = FileUtils.readFileToString(new File(pathOfPO+fn.toStringPO()), AppConfig.CHARSET);
				m = AppConfig.POPattern.matcher(zanataQuetsSource);
				while (m.find()) zanataList.put(m.group(2), new PO(m.group(1), m.group(2), m.group(3)));
			}

			for(Destinations desti : sourceList.values()) {
				PO po = zanataList.get(desti.getQuestName());
				if (po != null) desti.setQuestName(po.getTarget());
			}

			ArrayList<Destinations> list = new ArrayList<>(sourceList.values());
			Collections.sort(list);
			StringBuilder sb = new StringBuilder();
			for(Destinations desti : list) sb.append("\t[").append(desti.getId()).append("] = {\"").append(desti.getQuestName()).append("\"},\n");

			String index = "QuestTableStore = {";
			String res = destinationQuestSource.substring(0, destinationQuestSource.lastIndexOf(index)) + index + "\n" + sb.toString() + "}";
			FileUtils.write(s_questName.getAbsoluteFile(), res, AppConfig.CHARSET);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		new DestinationsMain().start();

	}
}
