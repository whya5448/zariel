package org.metalscraps.eso.lang.kr.Utils;

import org.metalscraps.eso.lang.kr.bean.PO;
import org.metalscraps.eso.lang.kr.config.AppConfig;
import org.metalscraps.eso.lang.kr.config.FileNames;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created by 안병길 on 2018-01-20.
 * Whya5448@gmail.com
 */
public class Utils {

	public static String KOToCN(String string) {
		char[] c = string.toCharArray();
		for(int i=0; i < c.length; i++) if (c[i] >= 0xAC00 && c[i] <= 0xEA00) c[i] -= 0x3E00;
		return new String(c);
	}

	public static String CNtoKO(String string) {
		char[] c = string.toCharArray();
		for(int i=0; i < c.length; i++) if (c[i] >= 0x6E00 && c[i] <= 0xAC00) c[i] += 0x3E00;
		return new String(c);
	}

	public static void replaceStringFromMap(StringBuilder stringBuilder, Map<String, ?> map) {
		for (Map.Entry<String, ?> entry : map.entrySet()) {
			String key = entry.getKey();
			Object rawValue = entry.getValue();
			String value = rawValue instanceof PO ? ((PO) rawValue).getTarget() : rawValue instanceof String ? (String) rawValue : key;

			int start = stringBuilder.indexOf(key, 0);
			while (start > -1) {
				int end = start + key.length();
				int nextSearchStart = start + value.length();
				stringBuilder.replace(start, end, value);
				start = stringBuilder.indexOf(key, nextSearchStart);
			}
		}
	}

	public static HashMap<String, PO> sourceToMap(SourceToMapConfig config) {

		HashMap<String, PO> poMap = new HashMap<>();
		String fileName = FilenameUtils.getBaseName(config.getFile().getName());
		String source = sourceToMapParser(config);

		Matcher m = config.getPattern().matcher(source);
		boolean isPOPattern = config.getPattern() == (AppConfig.POPattern);
		while (m.find()) {
			PO po = new PO(m.group(2), m.group(6), m.group(7)).wrap(config.getPrefix(), config.getSuffix(), config.getPoWrapType());
			po.setFileName(FileNames.fromString(fileName));
			if(isPOPattern && m.group(1) != null && m.group(1).equals("#, fuzzy")) po.setFuzzy(true);
			poMap.put(m.group(config.getKeyGroup()), po);
		}

		return poMap;
	}

	private static String sourceToMapParser(SourceToMapConfig config) {

		String source = null;
		try {
			source = FileUtils.readFileToString(config.getFile(), AppConfig.CHARSET);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (config.isToLowerCase()) source = source.toLowerCase();

		if (config.isProcessText()) {
			if (config.isProcessItemName()) source = source.replaceAll("\\^[\\w]+", ""); // 아이템 명 뒤의 기호 수정
			source = source.replaceAll("msgid \"\\\\+\"\n", "msgid \"\"\n") // "//" 이런식으로 되어있는 문장 수정. Extactor 에서 에러남.
					.replaceAll("msgstr \"\\\\+\"\n", "msgstr \"\"\n") // "//" 이런식으로 되어있는 문장 수정. Extactor 에서 에러남.
					.replaceAll("\\\\\"", "\"\"") // \" 로 되어있는 쌍따옴표 이스케이프 변환 "" 더블-더블 쿼테이션으로 이스케이프 시켜야함.
					.replaceAll("\\\\\\\\", "\\\\"); // 백슬래쉬 두번 나오는거 ex) ESOUI\\ABC\\DEF 하나로 고침.

			if (config.isRemoveComment()) source = source.replaceAll(AppConfig.englishTitlePattern, "$1");
		}
		return source;

	}
}
