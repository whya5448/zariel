package org.metalscraps.eso.lang.kr;

import org.apache.commons.io.FileUtils;

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
	private final String sep = System.getProperty("file.separator");
	private final String chars = "-가-힣  \\w\\s\\d\"/\\\\!.,'[]\\\\(\\\\)<>?~…:;â€";
	private final String pattern = "\"([\\d-]+?)\",\"([\\s\\S]*?)\",\"([\\s\\S]*?)\"\n";

	private class PO implements Comparable {
		PO(String id, String source, String target) {


			source = source.replaceAll("\"\n\"", "");
			target = target.replaceAll("\"\n\"", "");

			this.id = id;
			this.source = source;
			this.target = target;

			String[] ids = id.split("-");
			id1 = Integer.parseInt(ids[0]);
			id2 = Integer.parseInt(ids[1]);
			id3 = Integer.parseInt(ids[2]);

			if(target.equals("")) this.target = source;
			if(source.equals("")) this.source = target;

		}
		String id, source, target;
		Integer id1, id2, id3;

		@Override
		public String toString() { return toCSV(true); }
		String toCSV(boolean t) { return "\""+id+"\",\""+(t?source:"")+"\",\""+target+"\""; }

		@Override
		public int compareTo(Object o) {
			PO x = (PO) o;
			PO t = this;
			if(t.id1.equals(x.id1)) {
				if(t.id2.equals(x.id2)) return t.id3.compareTo(x.id3);
				else return t.id2.compareTo(x.id2);
			} else return t.id1.compareTo(x.id1);

		}
	}

	MergeMain() throws Exception {
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

		//for(PO p : arrayList) System.out.println(p);


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

	public static void main(String[] args) throws Exception {
		new MergeMain();
	}
}
