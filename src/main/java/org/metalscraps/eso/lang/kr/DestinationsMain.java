package org.metalscraps.eso.lang.kr;

import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.metalscraps.eso.lang.kr.bean.PO;
import org.metalscraps.eso.lang.kr.config.AppConfig;

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

		HashMap<String, Destinations> sourceList = new HashMap<>();
		HashMap<String, PO> zanataList = new HashMap<>();

		try {

			// 자나타 매핑된 PO.
			File t_questName = new File("C:\\Users\\admin\\Documents\\Elder Scrolls Online\\live\\works\\EsoExtractData v0.31\\PO_0113/journey.po2");

			// 데스티네이션 EN 복사해서 KR 폴더 생성.
			File s_questName = new File("C:\\Users\\admin\\Documents\\Elder Scrolls Online\\live\\AddOns\\Destinations\\data\\KR/DestinationsQuests_kr.lua");

			String destinationQuetsSource = FileUtils.readFileToString(s_questName, AppConfig.CHARSET);
			String zanataQuetsSource = FileUtils.readFileToString(t_questName, AppConfig.CHARSET);

			String destiPattern = "\\[(\\d+)] = \\{\"(.*)\"}";

			// Desti
			Pattern p = Pattern.compile(destiPattern, Pattern.MULTILINE);
			Matcher m = p.matcher(destinationQuetsSource);
			while (m.find()) sourceList.put(m.group(1), this.new Destinations(Integer.parseInt(m.group(1)), m.group(2)));

			// Zanata
			p = Pattern.compile(AppConfig.POPattern, Pattern.MULTILINE);
			m = p.matcher(zanataQuetsSource);
			while (m.find()) zanataList.put(m.group(2), new PO(m.group(1), m.group(2), m.group(3)));

			for(Destinations desti : sourceList.values()) {
				PO po = zanataList.get(desti.getQuestName());
				if (po != null) desti.setQuestName(po.getTarget());
			}

			ArrayList<Destinations> list = new ArrayList<>(sourceList.values());
			Collections.sort(list);
			StringBuilder sb = new StringBuilder();
			for(Destinations desti : list) sb.append("\t[").append(desti.getId()).append("] = {\"").append(desti.getQuestName()).append("\"},\n");

			String index = "QuestTableStore = {";
			String res = destinationQuetsSource.substring(0, destinationQuetsSource.lastIndexOf(index)) + index + "\n" + sb.toString() + "}";
			FileUtils.write(s_questName.getAbsoluteFile(), res, AppConfig.CHARSET);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		new DestinationsMain().start();

	}
}
