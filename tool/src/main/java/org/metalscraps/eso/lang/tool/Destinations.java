package org.metalscraps.eso.lang.tool;

import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.metalscraps.eso.lang.lib.bean.PO;
import org.metalscraps.eso.lang.lib.config.AppConfig;
import org.metalscraps.eso.lang.lib.config.AppWorkConfig;
import org.metalscraps.eso.lang.lib.config.FileNames;
import org.metalscraps.eso.lang.lib.config.SourceToMapConfig;
import org.metalscraps.eso.lang.lib.util.Utils;

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

	private void work(FileNames[] fileNames, File target){

		HashMap<String, PO> poMap = new HashMap<>();

		try {
			StringBuilder destinationQuestSource = new StringBuilder(FileUtils.readFileToString(new File(target.getPath().replace("kr","en")), AppConfig.CHARSET));
			SourceToMapConfig config = new SourceToMapConfig().setPattern(AppConfig.POPattern).setKeyGroup(6).setPrefix("{\"").setSuffix("\"}").setProcessItemName(false);

			for(FileNames fileName : fileNames) poMap.putAll(Utils.sourceToMap(config.setFile(new File(appWorkConfig.getPODirectory() +"/"+ fileName.toStringPO2()))));

			boolean init = false;
			for(Map.Entry<String, PO> entry : new HashMap<>(poMap).entrySet()) {
				if(!init) { poMap.clear(); init = true; }
				PO po = entry.getValue();
				if(po.isFuzzy()) po.setTarget(po.getSource());
				poMap.put("{\""+entry.getKey()+"\"}", po);
			}

			// 찾아바꾸기
			Utils.replaceStringFromMap(destinationQuestSource, poMap);

			// 저장
			System.out.println(target.getAbsolutePath());
			FileUtils.write(new File(target.getAbsolutePath()), destinationQuestSource, AppConfig.CHARSET);

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	void start() {

		work(new FileNames[] { FileNames.journey, FileNames.journeyOther } , new File(appWorkConfig.getBaseDirectory()+"/Destinations/DestinationsQuests_kr.lua"));
		work(new FileNames[] { FileNames.npcName } , new File(appWorkConfig.getBaseDirectory()+"/Destinations/DestinationsQuestgivers_kr.lua"));

	}
}
