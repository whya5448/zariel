package org.metalscraps.eso.lang.kr;

import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.metalscraps.eso.lang.kr.Utils.SourceToMapConfig;
import org.metalscraps.eso.lang.kr.Utils.Utils;
import org.metalscraps.eso.lang.kr.bean.PO;
import org.metalscraps.eso.lang.kr.config.AppConfig;
import org.metalscraps.eso.lang.kr.config.FileNames;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 안병길 on 2018-01-15.
 * Whya5448@gmail.com
 */


@AllArgsConstructor
class Destinations {

	private final AppWorkConfig appWorkConfig;

	private void work(FileNames[] fileNames, File target, boolean removeComment){

		HashMap<String, PO> poMap = new HashMap<>();

		try {
			StringBuilder destinationQuestSource = new StringBuilder(FileUtils.readFileToString(new File(target.getPath().replace("kr","en")), AppConfig.CHARSET));
			SourceToMapConfig config = new SourceToMapConfig().setPattern(AppConfig.POPattern).setKeyGroup(6).setPrefix("{\"").setSuffix("\"}").setProcessItemName(false);
			if(removeComment) config.setRemoveComment(removeComment);

			for(FileNames fileName : fileNames) poMap.putAll(Utils.sourceToMap(config.setFile(new File(appWorkConfig.getPODirectory() +"/"+ fileName.toStringPO2()))));

			boolean init = false;
			for(Map.Entry<String, PO> entry : ((Map<String, PO>)poMap.clone()).entrySet()) {
				if(!init) { poMap.clear(); init = true; }
				PO po = entry.getValue();
				if(po.isFuzzy()) po.setTarget(po.getSource());
				poMap.put("{\""+entry.getKey()+"\"}", po);
			}

			// 찾아바꾸기
			Utils.replaceStringFromMap(destinationQuestSource, poMap);

			// 저장
			System.out.println(target.getAbsolutePath() + (removeComment ? ".no.comments":""));
			FileUtils.write(new File(target.getAbsolutePath() + (removeComment ? ".no.comments":"")), destinationQuestSource, AppConfig.CHARSET);

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	void start() {

		//work(new FileNames[] { FileNames.journey, FileNames.journeyOther } , new File(appWorkConfig.getBaseDirectory()+"/Destinations/DestinationsQuests_kr.lua"), true);
		//work(new FileNames[] { FileNames.npcName } , new File(appWorkConfig.getBaseDirectory()+"/Destinations/DestinationsQuestgivers_kr.lua"), true);

		//work(new FileNames[] { FileNames.journey, FileNames.journeyOther } , new File(appWorkConfig.getBaseDirectory()+"/Destinations/DestinationsQuests_kr.lua"), false);
		work(new FileNames[] { FileNames.npcName } , new File(appWorkConfig.getBaseDirectory()+"/Destinations/DestinationsQuestgivers_kr.lua"), false);

	}
}
