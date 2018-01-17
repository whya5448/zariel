package org.metalscraps.eso.lang.kr;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.metalscraps.eso.lang.kr.config.AppConfig;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 안병길 on 2018-01-13.
 * Whya5448@gmail.com
 */
public class GetPO {
	public static void main(String[] args) throws Exception {

		String[] ss = {
				"00_EsoUI_Client",
				"00_EsoUI_Pregame",
				"achievement",
				"book",
				"book-other",
				"chat",
				"color",
				"country-or-region",
				"emote",
				"greeting",
				"interact-action",
				"interact-win",
				"item",
				"item-crate",
				"item-crown",
				"item-crown-other",
				"item-crown-pack",
				"item-crown-pack-other",
				"item-other",
				"item-quest",
				"item-quest-other",
				"item-type",
				"journey",
				"journey-detail",
				"journey-other",
				"letter",
				"loadscreen",
				"loadscreen-other",
				"location-and-object",
				"location-object",
				"more-desc",
				"more-ui",
				"npc-name",
				"npc-other",
				"npc-talk",
				"other",
				"popup-tip",
				"popup-tip-other",
				"quest-end",
				"quest-main-1",
				"quest-main-2",
				"quest-main-3",
				"quest-main-4",
				"quest-main-5",
				"quest-main-6",
				"quest-obj",
				"quest-start",
				"quest-sub",
				"quest-sub-obj",
				"set",
				"skill",
				"skill-other",
				"subtitle",
				"three-alliance",
				"tip",
				"title",
				"trap",
				"treasure-map"
		};
		final String url = "http://www.dostream.com/zanata/rest/file/translation/esokr/3.2.6.1517120/ko/po?docId=";
		HashMap<String, String> list = new HashMap<>();
		for(String s : ss) {
			Date st = new Date();
			System.out.println("=====================");
			System.out.println(s);
			list.put(s, IOUtils.toString(new URL(url+s), AppConfig.CHARSET));
			Date ed = new Date();
			System.out.println((ed.compareTo(st) / 1000 / 60)+"분");
		}

		for(Map.Entry<String, String> entry : list.entrySet()) {
			File f = new File("C:\\Users\\admin\\Documents\\Elder Scrolls Online\\live\\works\\EsoExtractData v0.31\\PO_0113/"+entry.getKey()+".po");
			FileUtils.writeStringToFile(f, entry.getValue(), AppConfig.CHARSET);
		}

	}
}
