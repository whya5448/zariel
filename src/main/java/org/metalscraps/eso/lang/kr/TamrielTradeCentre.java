package org.metalscraps.eso.lang.kr;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.metalscraps.eso.lang.kr.Utils.SourceToMapConfig;
import org.metalscraps.eso.lang.kr.Utils.Utils;
import org.metalscraps.eso.lang.kr.bean.PO;
import org.metalscraps.eso.lang.kr.config.AppConfig;
import org.metalscraps.eso.lang.kr.config.FileNames;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 안병길 on 2017-12-17.
 * Whya5448@gmail.com
 */

@AllArgsConstructor
class TamrielTradeCentre {

	@Data
	@AllArgsConstructor
	class LuaClass { final String first, second; }

	private final AppWorkConfig appWorkConfig;

	void start() {

		try {

			FileNames[] fileNames = new FileNames[]{FileNames.item, FileNames.itemOther};

			File koreanTable = new File(appWorkConfig.getBaseDirectory().getAbsolutePath() + "/TTC/ItemLookUpTable_KR.lua");
			File englishTable = new File(appWorkConfig.getBaseDirectory().getAbsolutePath() + "/TTC/ItemLookUpTable_EN.lua");

			HashMap<String, PO> poHashMap = new HashMap<>();

			// 번역본에서 데이터 추출
			for (FileNames fileName : fileNames)
				poHashMap.putAll(
						Utils.sourceToMap(
								new SourceToMapConfig()
										.setKeyGroup(6)
										.setToLowerCase(true)
										.setFile(new File(appWorkConfig.getPODirectory() + "/" + fileName.toStringPO2()))
										.setPattern(AppConfig.POPattern)));

			// 룩업 테이블 정보화
			StringBuilder englishSource = new StringBuilder(FileUtils.readFileToString(englishTable, AppConfig.CHARSET).toLowerCase().replaceAll("},}\\s*end\\s*", "},"));
			HashMap<String, LuaClass> englishMap = new HashMap<>();
			Pattern p = Pattern.compile("(\\[\"([\\w\\d\\s,:'()-]+)\"]=\\{\\[(\\d+)]=(\\w+),},)", Pattern.MULTILINE);
			Matcher m = p.matcher(englishSource);
			while (m.find()) englishMap.put(m.group(2), new LuaClass(m.group(3), m.group(4)));

			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, PO> entry : poHashMap.entrySet()) {

				LuaClass luaClass = englishMap.get(entry.getKey());
				if(luaClass!=null) sb.append("[\"")
									.append(entry.getValue().getTarget())
									.append("\"]={[")
									.append(luaClass.getFirst())
									.append("]=")
									.append(luaClass.getSecond())
									.append(",},");
			}

			englishSource.append(Utils.KOToCN(sb.toString())).append("}\nend");

			String key = ",},";
			String value = ",},\n";
			int start = englishSource.indexOf(key, 0);
			while (start > -1) {
				int end = start + key.length();
				int nextSearchStart = start + value.length();
				englishSource.replace(start, end, value);
				start = englishSource.indexOf(key, nextSearchStart);
			}

			englishSource.replace(0, 71, "function TamrielTradeCentre:LoadItemLookUpTable()\nself.ItemLookUpTable");

			FileUtils.writeStringToFile(koreanTable, englishSource.toString(), Charset.forName("UTF-8"));
			FileUtils.writeStringToFile(new File(koreanTable.getPath()+".no.comment"), englishSource.toString().replaceAll(AppConfig.englishTitlePattern, "$1"), Charset.forName("UTF-8"));

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
