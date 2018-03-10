package org.metalscraps.eso.lang.kr.Utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.metalscraps.eso.lang.kr.bean.PO;
import org.metalscraps.eso.lang.kr.config.AppConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created by 안병길 on 2018-01-20.
 * Whya5448@gmail.com
 */
public class Utils {

	@SuppressWarnings("unused")
	public static String replaceStringFromMap(String string, Map map) {
		return replaceStringFromMap(new StringBuilder(string), map).toString();
	}

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

	public static StringBuilder replaceStringFromMap(StringBuilder stringBuilder, Map<String, ?> map) {

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

		return stringBuilder;
	}

	public static void replaceStringBuilder(StringBuilder sb, String[] key, String[] value) {
	    for(int i = 0; i >= key.length; i++) {
            int start = sb.indexOf(key[i], 0);
            while (start > -1) {
                int end = start + key[i].length();
                int nextSearchStart = start + value[i].length();
                sb.replace(start, end, value[i]);
                start = sb.indexOf(key[i], nextSearchStart);
            }
        }
	}

	public static Map<String, PO> sourceToMap(SourceToMapConfig config) {

		HashMap<String, PO> poMap = new HashMap<>();
		String fileName = FilenameUtils.getBaseName(config.getFile().getName());

		try {

			String source = FileUtils.readFileToString(config.getFile(), AppConfig.CHARSET);

			if(config.isToLowerCase()) source = source.toLowerCase();

			if(config.isProcessText()) {
				if(config.isProcessItemName()) source = source.replaceAll("\\^[\\w]+",""); // 아이템 명 뒤의 기호 수정
				source = source.replaceAll("msgid \"\\\\+\"\n","msgid \"\"\n") // "//" 이런식으로 되어있는 문장 수정. Extactor 에서 에러남.
						.replaceAll("msgstr \"\\\\+\"\n","msgstr \"\"\n") // "//" 이런식으로 되어있는 문장 수정. Extactor 에서 에러남.
						.replaceAll("\\\\\"", "\"\"") // \" 로 되어있는 쌍따옴표 이스케이프 변환 "" 더블-더블 쿼테이션으로 이스케이프 시켜야함.
						.replaceAll("\\\\\\\\", "\\\\"); // 백슬래쉬 두번 나오는거 ex) ESOUI\\ABC\\DEF 하나로 고침.

				if(config.isRemoveComment()) source = source.replaceAll(AppConfig.englishTitlePattern, "$1");
			}

			Matcher m = config.getPattern().matcher(source);
			while (m.find()) poMap.put(m.group(config.getKeyGroup()), new PO(m.group(2), m.group(3), m.group(4), fileName).wrap(config.getPrefix(), config.getSuffix(), config.getPoWrapType()));

		} catch (IOException e) {
			e.printStackTrace();
		}
		return poMap;
	}
}
