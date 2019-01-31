package org.metalscraps.eso.lang.tool;

import org.apache.commons.io.FileUtils;
import org.metalscraps.eso.lang.lib.bean.PO;
import org.metalscraps.eso.lang.lib.config.AppConfig;
import org.metalscraps.eso.lang.lib.config.AppWorkConfig;
import org.metalscraps.eso.lang.lib.config.FileNames;
import org.metalscraps.eso.lang.lib.config.SourceToMapConfig;
import org.metalscraps.eso.lang.lib.util.Utils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 안병길 on 2017-12-17.
 * Whya5448@gmail.com
 */

class TamrielTradeCentre {

	@java.beans.ConstructorProperties({"appWorkConfig"})
	public TamrielTradeCentre(AppWorkConfig appWorkConfig) {
		this.appWorkConfig = appWorkConfig;
	}

	class LuaClass { final String first, second;

		@java.beans.ConstructorProperties({"first", "second"})
		public LuaClass(String first, String second) {
			this.first = first;
			this.second = second;
		}

		public String getFirst() {
			return this.first;
		}

		public String getSecond() {
			return this.second;
		}

		public boolean equals(final Object o) {
			if (o == this) return true;
			if (!(o instanceof LuaClass)) return false;
			final LuaClass other = (LuaClass) o;
			if (!other.canEqual(this)) return false;
			if (!Objects.equals(this.first, other.first)) return false;
			return Objects.equals(this.second, other.second);
		}

		protected boolean canEqual(final Object other) {
			return other instanceof LuaClass;
		}

		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			final Object $first = this.first;
			result = result * PRIME + ($first == null ? 43 : $first.hashCode());
			final Object $second = this.second;
			result = result * PRIME + ($second == null ? 43 : $second.hashCode());
			return result;
		}

		public String toString() {
			return "TamrielTradeCentre.LuaClass(first=" + this.first + ", second=" + this.second + ")";
		}
	}

	private final AppWorkConfig appWorkConfig;

	void start() {

		try {

			FileNames[] fileNames = new FileNames[]{FileNames.item, FileNames.itemOther};

			File koreanTable = new File(appWorkConfig.getBaseDirectory().getAbsolutePath() + "/TTC/ItemLookUpTable_KR.lua");
			File englishTable = new File(appWorkConfig.getBaseDirectory().getAbsolutePath() + "/TTC/ItemLookUpTable_EN.lua");

			HashMap<String, PO> poHashMap = new HashMap<>(), poHashMapWithTitle;

            SourceToMapConfig sourceToMapConfig = new SourceToMapConfig()
                    .setKeyGroup(6)
                    .setToLowerCase(true)
                    .setPattern(AppConfig.POPattern);

			// 번역본에서 데이터 추출
            for (FileNames fileName : fileNames) {
                sourceToMapConfig.setFile(new File(appWorkConfig.getPODirectory() + "/" + fileName.toStringPO2()));
                poHashMap.putAll(Utils.sourceToMap(sourceToMapConfig));
            }

            // 타이틀 버전
            poHashMapWithTitle = new HashMap<>(poHashMap);
            for(PO p : poHashMapWithTitle.values()) p.setTarget(p.getFileName().getShortName()+"_"+p.getId3()+"_"+p.getTarget());

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

            for (Map.Entry<String, PO> entry : poHashMapWithTitle.entrySet()) {
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
            FileUtils.copyFile(koreanTable, new File(koreanTable.getAbsolutePath().replace("_KR","_TR")));
            //FileUtils.writeStringToFile(new File(koreanTable.getPath()+".no.comment"), englishSource.toString().replaceAll(AppConfig.englishTitlePattern, "$1"), Charset.forName("UTF-8"));

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
