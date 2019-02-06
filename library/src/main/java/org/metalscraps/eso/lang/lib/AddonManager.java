package org.metalscraps.eso.lang.lib;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.metalscraps.eso.lang.lib.bean.PO;
import org.metalscraps.eso.lang.lib.config.AppConfig;
import org.metalscraps.eso.lang.lib.config.AppWorkConfig;
import org.metalscraps.eso.lang.lib.config.SourceToMapConfig;
import org.metalscraps.eso.lang.lib.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 안병길 on 2019-02-06.
 * Whya5448@gmail.com
 */
@AllArgsConstructor
public class AddonManager {

	private final Logger logger = LoggerFactory.getLogger(AddonManager.class);
	AppWorkConfig appWorkConfig;

	public void destination() {

		@Data
		@AllArgsConstructor
		class Quests {
			String id;
			String quest;
		}

		@AllArgsConstructor
		class Runner {
			Path en, ko;
			boolean nameCheck;
			String fileName, key;
			int id;

			void process() {
				try {

					// 영문 소스 불러옴
					var enText = new StringBuilder(Files.readString(appWorkConfig.getBaseDirectoryToPath().resolve(en)));

					// 한글 소스맵 불러옴
					var koText = new HashMap<String, PO>();
					Utils.listFiles(appWorkConfig.getPODirectoryToPath(), "po2")
							.stream()
							.filter(e-> (nameCheck ? e.getFileName().toString().equals(fileName):e.getFileName().toString().contains(fileName)))
							.forEach(e-> koText.putAll(Utils.sourceToMap(new SourceToMapConfig().setPath(e))) );
					koText.values().removeIf(x->!x.getId1().equals(id));

					// 영문 소스 객체-맵화
					var enQuests = new ArrayList<Quests>();
					var questsMatcher = AppConfig.PATTERN_DESTINATION.matcher(enText);
					while (questsMatcher.find()) enQuests.add(new Quests(questsMatcher.group(2), questsMatcher.group(4)));

					// 최종본 생성용 빌더
					var builder = new StringBuilder(key+"\n");

					// 영문 맵에 있는 객체 ID로 한글맵에서 데이터 가져와 빌더에 붙힘.
					for(var x : enQuests) {
						var xid = id+"-0-"+x.id;
						if(koText.containsKey(xid) && !koText.get(xid).isFuzzy()) builder.append("\t[").append(x.id).append("]").append(" = {\"").append(koText.get(xid).getTarget()).append("\"},\n");
						else {
							builder.append("\t[").append(x.id).append("]").append(" = {\"").append(x.quest).append("\"},\n");
							if(!koText.containsKey(xid)) logger.warn("Missing Data? "+xid);
						}
					}
					builder.append("}\n");

					// 불러왔던 원본 소스 replace
					enText.replace(enText.indexOf(key), enText.length(), builder.toString());

					// 한글 결과물 있을 시 삭제 후 재작성
					Files.deleteIfExists(ko);
					Files.writeString(ko, enText, AppConfig.CHARSET);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}


		var runner = new Runner(
				appWorkConfig.getBaseDirectoryToPath().resolve("DestinationsQuests_en.lua"),
				appWorkConfig.getBaseDirectoryToPath().resolve("DestinationsQuests_kr.lua"),
				true,
				"journey.po2",
				"QuestTableStore = {",
				52420949
		);
		runner.process();

		runner = new Runner(
				appWorkConfig.getBaseDirectoryToPath().resolve("DestinationsQuestgivers_en.lua"),
				appWorkConfig.getBaseDirectoryToPath().resolve("DestinationsQuestgivers_kr.lua"),
				false,
				"npc-talk",
				"QuestGiverStore = {",
				8290981
		);
		runner.process();

	}
}
