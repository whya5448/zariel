package org.metalscraps.eso.lang.kr;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.metalscraps.eso.lang.kr.config.AppConfig;
import org.metalscraps.eso.lang.kr.config.FileNames;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static org.metalscraps.eso.lang.kr.config.FileNames.*;

/**
 * Created by 안병길 on 2018-01-13.
 * Whya5448@gmail.com
 */
public class GetPO {

	private final String url = "http://www.dostream.com/zanata/rest/file/translation/esokr/3.2.6.1517120/ko/po?docId=";

	public void start() {

		try {

			FileNames[] ss = FileNames.values();

			HashMap<String, String> list = new HashMap<>();
			LocalTime totalSt = LocalTime.now();
			for (FileNames s : ss) {
				LocalTime st = LocalTime.now();
				System.out.println("=====================");
				System.out.println(s);
				list.put(s.toString(), IOUtils.toString(new URL(url + s), AppConfig.CHARSET));
				LocalTime ed = LocalTime.now();
				System.out.println(st.until(ed, ChronoUnit.SECONDS) + "초");
			}
			LocalTime totalEd = LocalTime.now();
			System.out.println("총 " + totalSt.until(totalEd, ChronoUnit.SECONDS) + "초");
			LocalDate today = LocalDate.now();
			for (Map.Entry<String, String> entry : list.entrySet()) {
				File f = new File("C:\\Users\\admin\\Documents\\Elder Scrolls Online\\live\\works\\EsoExtractData v0.31\\PO_" + today.format(DateTimeFormatter.ofPattern("MMdd")) + "/" + entry.getKey() + ".po");
				FileUtils.writeStringToFile(f, entry.getValue(), AppConfig.CHARSET);
			}

		} catch (Exception e) { e.printStackTrace(); }
	}

	public static void main(String[] args) {
		new GetPO().start();
	}
}
