package org.metalscraps.eso.lang.kr.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Created by 안병길 on 2018-01-17.
 * Whya5448@gmail.com
 */
public class AppConfig {
	public static final Charset CHARSET = StandardCharsets.UTF_8;
	public static final Pattern POPattern = Pattern.compile("(#, fuzzy\\n)?msgctxt \"([0-9-]+)\"\\n*?msgid \"{1,2}?\\n?([\\s\\S]*?)\"\\n*?msgstr \"{1,2}?\\n?([\\s\\S]*?)\"\\n{2,}", Pattern.MULTILINE);
	public static final Pattern CSVPattern = Pattern.compile("\"()([\\d-]+?)\",\"([\\s\\S]*?)\",\"([\\s\\S]*?)\"\n", Pattern.MULTILINE);
	public static final String englishTitlePattern = "([渀-馤가-힣\\s]+)\\s?\\([\\w\\s]+\\)";
}
