package org.metalscraps.eso.lang.kr;

import lombok.AllArgsConstructor;
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


@AllArgsConstructor
public class Destinations {

	private final AppWorkConfig appWorkConfig;

	private void work(FileNames[] fileNames, File target){

		try {
			HashMap<String, PO> zanataList = new HashMap<>();

			StringBuilder destinationQuestSource = new StringBuilder(FileUtils.readFileToString(target, AppConfig.CHARSET));

			for (FileNames fn : fileNames) {
				String zanataQuetsSource = FileUtils.readFileToString(new File(appWorkConfig.getPODirectory() +"/"+ fn.toStringPO2()), AppConfig.CHARSET);
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

	void start() {

		work(new FileNames[] { FileNames.journey, FileNames.journeyOther } , new File(appWorkConfig.getBaseDirectory()+"/Destinations/DestinationsQuests_kr.lua"));
		work(new FileNames[] { FileNames.npcName } , new File(appWorkConfig.getBaseDirectory()+"/Destinations/DestinationsQuestgivers_kr.lua"));

	}
}
