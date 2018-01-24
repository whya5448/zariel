package org.metalscraps.eso.lang.kr;

import org.apache.commons.io.FileUtils;
import org.metalscraps.eso.lang.kr.Utils.Utils;
import org.metalscraps.eso.lang.kr.bean.PO;
import org.metalscraps.eso.lang.kr.config.AppConfig;
import org.metalscraps.eso.lang.kr.config.FileNames;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;

/**
 * Created by 안병길 on 2018-01-15.
 * Whya5448@gmail.com
 */


public class DestinationsMain {

	private final String pathOfPO = "C:/Users/admin/Documents/Elder Scrolls Online/live/works/EsoExtractData v0.31/PO_0124/";
	private final String pathOfDesti = "C:/Users/admin/Documents/Elder Scrolls Online/live/AddOns/Destinations/data/KR";

	private void work(FileNames[] fileNames, File target){

		try {
			HashMap<String, PO> zanataList = new HashMap<>();

			StringBuilder destinationQuestSource = new StringBuilder(FileUtils.readFileToString(target, AppConfig.CHARSET));

			for (FileNames fn : fileNames) {
				String zanataQuetsSource = FileUtils.readFileToString(new File(pathOfPO + fn.toStringPO2()), AppConfig.CHARSET);
				Matcher m = AppConfig.POPattern.matcher(zanataQuetsSource);
				while (m.find()) {
					PO p = new PO(m.group(1), m.group(2), m.group(3));
					p.wrap("{\"", "\"}");
					zanataList.put(p.getSource(), p);
				}
			}

			// 찾아바꾸기
			Utils.replaceStringFromMap(destinationQuestSource, zanataList);

			// 저장
			FileUtils.write(target.getAbsoluteFile(), destinationQuestSource, AppConfig.CHARSET);

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void start() {

		work(new FileNames[] { FileNames.journey, FileNames.journeyOther } , new File(pathOfDesti+"/DestinationsQuests_kr.lua"));
		work(new FileNames[] { FileNames.npcName } , new File(pathOfDesti+"/DestinationsQuestgivers_kr.lua"));

	}

	public static void main(String[] args) {
		new DestinationsMain().start();
	}
}
