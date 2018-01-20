package org.metalscraps.eso.lang.kr;

import org.apache.commons.io.FileUtils;
import org.metalscraps.eso.lang.kr.Utils.SourceToMapConfig;
import org.metalscraps.eso.lang.kr.Utils.Utils;
import org.metalscraps.eso.lang.kr.bean.PO;
import org.metalscraps.eso.lang.kr.config.AppConfig;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Created by 안병길 on 2018-01-14.
 * Whya5448@gmail.com
 */
public class MergeMain {

	public void start() {
		{
			HashMap<String, PO> map = new HashMap<>();

			File[] ff = {
					new File("C:\\Users\\admin\\Documents\\Elder Scrolls Online\\live\\works\\EsoExtractData v0.31/en_0113.lang.csv"),
					new File("C:\\Users\\admin\\Documents\\Elder Scrolls Online\\live\\works\\EsoExtractData v0.31/kr.lang.csv"),
					new File("C:\\Users\\admin\\Documents\\Elder Scrolls Online\\live\\works\\EsoExtractData v0.31/new.csv")
			};

			for(File f : ff) {
				map.putAll(
						Utils.sourceToMap(new SourceToMapConfig().setFile(f).setPattern(AppConfig.CSVPattern))
				);
				/*

				try {

					String source = FileUtils.readFileToString(f, AppConfig.CHARSET).replaceAll("\\^[\\w]+","");
					Matcher m = AppConfig.CSVPattern.matcher(source);

					while(m.find()) {
						if(m.group(1).equals("265851556-0-4666")) {
							System.out.println("====================");
							System.out.println(m.group(0));
							System.out.println(m.group(1));
							System.out.println(m.group(2));
							System.out.println(m.group(3));
						}
						map.put(m.group(1), new PO(m.group(1), m.group(2), m.group(3)));
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
				*/
			}

			System.out.println("정렬");
			ArrayList<PO> arrayList = new ArrayList<>(map.values());
			Collections.sort(arrayList);

			System.out.println("빌딩");
			StringBuilder sb = new StringBuilder("\"Location\",\"Source\",\"Target\"\n");
			for(PO p : arrayList) sb.append(p.toCSV(false)).append("\n");

			System.out.println("출력");
			try {
				FileUtils.writeStringToFile(new File("C:\\Users\\admin\\Documents\\Elder Scrolls Online\\live\\works\\EsoExtractData v0.31/final.csv"), sb.toString(), AppConfig.CHARSET);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public static void main(String[] args) {
		new MergeMain().start();
	}
}
