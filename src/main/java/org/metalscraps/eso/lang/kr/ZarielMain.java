package org.metalscraps.eso.lang.kr;

import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 안병길 on 2017-12-17.
 * Whya5448@gmail.com
 */
public class ZarielMain {

	private String sLuaSource;
	private Map<String, String> fontMap = new HashMap<>();

	class LuaClass {

		final String first, second;
		LuaClass(String y, String z) {
			first = y;
			second = z;
		}

		@Override
		public String toString() { return first + "=" + second; }
	}

	private final Charset charset = StandardCharsets.UTF_8;

	private ZarielMain() {
		getFontMapData();
	}

	private void getFontMapData() {
		fontMap.clear();
		char[] cKO, cCN;
		String sKO, sCN;

		for(int i=0; i<11172; i++) {
			cKO = Character.toChars(0xAC00+i); cCN = Character.toChars(0x6E00+i);
			sKO = new String(cKO); sCN = new String(cCN);
			fontMap.put(sKO, sCN);
		}
	}

	private HashMap<String, String> getKRSource(File f) {
		HashMap<String, String> sourceMap = new HashMap<>();
		try {
			String source = FileUtils.readFileToString(f, charset).replaceAll("\\^[\\w]+","");;
			Pattern p = Pattern.compile("msgid \"([\\w\\d\\s,:'\\()-^]+)\"[\r\n]+msgstr \"([ㄱ-ㅎㅏ-ㅣ가-힣\\w\\d\\s,:'\\()-^]+)\"", Pattern.MULTILINE);
			Matcher m = p.matcher(source);
			while(m.find()) sourceMap.put(m.group(1).toLowerCase(), m.group(2).toLowerCase());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sourceMap;
	};

	private HashMap<String, LuaClass> getLua(File f) {
		HashMap<String, LuaClass> sourceMap = new HashMap<>();
		try {
			sLuaSource = FileUtils.readFileToString(f, charset);
			Pattern p = Pattern.compile("(\\[\"([\\w\\d\\s,:'\\()-]+)\"]=\\{\\[(\\d+)]=(\\w+),},)", Pattern.MULTILINE);
			Matcher m = p.matcher(sLuaSource);
			while(m.find()) sourceMap.put(m.group(2).toLowerCase(), new LuaClass(m.group(3), m.group(4)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sourceMap;
	}

	private String replaceFromMap(StringBuilder sb, Map<String, String> replacements) {
		for (Map.Entry<String, String> entry : replacements.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			int start = sb.indexOf(key, 0);
			while (start > -1) {
				int end = start + key.length();
				int nextSearchStart = start + value.length();
				sb.replace(start, end, value);
				start = sb.indexOf(key, nextSearchStart);
			}
		}
		return sb.toString();
	}

	private void process() {

		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setCurrentDirectory(FileUtils.getFile(FileUtils.getUserDirectoryPath() + System.getProperty("file.separator") + "desktop"));
		if (fc.showOpenDialog(null) == JFileChooser.CANCEL_OPTION) System.exit(JFileChooser.CANCEL_OPTION);

		File f = fc.getSelectedFile();
		File fLuaKO = FileUtils.getFile(f.getPath()+"/ItemLookUpTable_KR.lua");
		File fLuaEN = FileUtils.getFile(f.getPath()+"/ItemLookUpTable_EN.lua");

		ArrayList<String> hNotMatch = new ArrayList<>();
		HashMap<String, LuaClass> hMatch = new HashMap<>();

		System.out.println("작업 시작");
		System.out.println("============================");
		// 번역본에서 데이터 추출
		HashMap<String, String> hKR = getKRSource(FileUtils.getFile(f.getPath()+"/item.po"));
		hKR.putAll(getKRSource(FileUtils.getFile(f.getPath()+"/item-other.po")));

		// 룩업 테이블 정보화
		HashMap<String, LuaClass> hEN = getLua(fLuaEN);

		for(Map.Entry<String, String> s : hKR.entrySet()) {
			LuaClass l = hEN.get(s.getKey());
			if(l == null) hNotMatch.add(s.getKey());
			else hMatch.put(s.getValue(), l);
		}
		System.out.println("매칭 실패 아이템 수 : "+hNotMatch.size());
		try {
			StringBuilder x = new StringBuilder();
			for(String xy : hNotMatch) x.append(xy+"\n");
			FileUtils.writeStringToFile(new File("D:/mis.txt"), x.toString(), charset);
		} catch (IOException e) {
			e.printStackTrace();
		}

		StringBuilder result = new StringBuilder();
		for(Map.Entry<String, LuaClass> x : hMatch.entrySet()) result.append("[\"").append(x.getKey()).append("\"]={[").append(x.getValue().first).append("]=").append(x.getValue().second).append(",},");

		// 한자-한글 매핑
		String sResult = replaceFromMap(result, fontMap);

		sLuaSource = sLuaSource.replace("},}", "},"+sResult+"}");
		StringBuilder sssbbb = new StringBuilder(sLuaSource);
		String key = ",},";
		String value = ",},\n";
		int start = sssbbb.indexOf(key, 0);
		while (start > -1) {
			int end = start + key.length();
			int nextSearchStart = start + value.length();
			sssbbb.replace(start, end, value);
			start = sssbbb.indexOf(key, nextSearchStart);
		}

		try {
			FileUtils.writeStringToFile(fLuaKO, sssbbb.toString(), Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("============================");
		System.out.println("작업 종료");
	}


	public static void main(String[] args) throws Exception {
			ZarielMain zarielMain = new ZarielMain();
			zarielMain.process();
		}
}
