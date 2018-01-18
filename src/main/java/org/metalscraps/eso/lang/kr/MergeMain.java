package org.metalscraps.eso.lang.kr;

import org.apache.commons.io.FileUtils;
import org.metalscraps.eso.lang.kr.bean.PO;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 안병길 on 2018-01-14.
 * Whya5448@gmail.com
 */
public class MergeMain {


	private final Charset charset = StandardCharsets.UTF_8;
	private final String pattern = "\"([\\d-]+?)\",\"([\\s\\S]*?)\",\"([\\s\\S]*?)\"\n";

	public void start() {
		{
			HashMap<String, PO> map = new HashMap<>();

			File[] ff = {
					new File("C:\\Users\\admin\\Documents\\Elder Scrolls Online\\live\\works\\EsoExtractData v0.31/en_0113.lang.csv"),
					new File("C:\\Users\\admin\\Documents\\Elder Scrolls Online\\live\\works\\EsoExtractData v0.31/kr.lang.csv"),
					new File("C:\\Users\\admin\\Documents\\Elder Scrolls Online\\live\\works\\EsoExtractData v0.31/map.csv")
			};

			for(File f : ff) {
				try {
					String source = FileUtils.readFileToString(f, charset).replaceAll("\\^[\\w]+","");
					Pattern p = Pattern.compile(pattern, Pattern.MULTILINE);
					Matcher m = p.matcher(source);

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
			}

			System.out.println("정렬");
			ArrayList<PO> arrayList = new ArrayList<>(map.values());
			Collections.sort(arrayList);

			System.out.println("빌딩");
			StringBuilder sb = new StringBuilder("\"Location\",\"Source\",\"Target\"\n");
			for(PO p : arrayList) sb.append(p.toCSV(false)).append("\n");

			System.out.println("출력");
			try {
				FileUtils.writeStringToFile(new File("C:\\Users\\admin\\Documents\\Elder Scrolls Online\\live\\works\\EsoExtractData v0.31/final.csv"), sb.toString(), charset);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public static void main(String[] args) {
		new MergeMain().start();
	}
}
