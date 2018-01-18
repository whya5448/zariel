package org.metalscraps.eso.lang.kr;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.metalscraps.eso.lang.kr.bean.PO;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 안병길 on 2017-12-31.
 * Whya5448@gmail.com
 */
public class ToLangMain {

	private final Charset charset = StandardCharsets.UTF_8;
	private final String sep = System.getProperty("file.separator");
	//private final String pattern = "msgctxt \"([0-9-]+)\"\\n*?msgid \"{1,2}?\\n?([\\s\\S]*?)\"\\n*?msgstr \"{1,2}?\\n?([\\s\\S]*?)\"\\n*?";
	private final String pattern = "msgctxt \"([0-9-]+)\"\\n*?msgid \"{1,2}?\\n?([\\s\\S]*?)\"\\n*?msgstr \"{1,2}?\\n?([\\s\\S]*?)\"\\n{2,}";


	private int countWord(String s, String word) {

		StringBuilder sb = new StringBuilder(s);
		String value = "v";
		int i = 0;

		int start = sb.indexOf(word, 0);
		while (start > -1) {
			int end = start + word.length();
			int nextSearchStart = start + value.length();
			sb.replace(start, end, value);
			start = sb.indexOf(word, nextSearchStart);
			i++;
		}

		return i;
	}

	private void start() {
		getSource();
	}

	private void getSource() {

		HashMap<String, Integer> count = new HashMap<>();
		ArrayList<PO> sourceList = new ArrayList<>();
		ArrayList<PO> sourceWithTitle = new ArrayList<>();

		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setCurrentDirectory(FileUtils.getFile(FileUtils.getUserDirectoryPath() + sep + "desktop" + sep + "po"));
		//fc.setCurrentDirectory(FileUtils.getFile("C:/dev/po"));
		//if (fc.showOpenDialog(null) == JFileChooser.CANCEL_OPTION) System.exit(JFileChooser.CANCEL_OPTION);

		File fx = fc.getSelectedFile();
		fx = FileUtils.getFile("C:\\Users\\admin\\Documents\\Elder Scrolls Online\\live\\works\\EsoExtractData v0.31\\PO_0113");

		Collection<File> fileList = FileUtils.listFiles(fx, new String[]{"po2"}, false);
		int total = 0;
		for(File ff : fileList) {

			String fileName = FilenameUtils.getBaseName(ff.getName());
			if(fileName.equals("00_EsoUI_Client") || fileName.equals("00_EsoUI_Pregame")) continue;

			if(
			!fileName.equals("quest-sub") &&
			!fileName.equals("journey") &&
			!fileName.equals("tip")
			) ; //continue;

			LinkedList<String> listId = new LinkedList<>();

			try {
				String source = FileUtils.readFileToString(ff, charset)
						.replaceAll("\\^[\\w]+","") // 아이템명 뒤에 붙는 이상한 문자열 제거
						.replaceAll("msgid \"\\\\+\"\n","msgid \"\"") // "//" 이런식으로 되어있는 문장 수정. Extactor 에서 에러남.
						.replaceAll("\\\\\"", "\"\"") // \" 로 되어있는 쌍따옴표 이스케이프 변환 "" 더블-더블 쿼테이션으로 이스케이프 시켜야함.
						.replaceAll("\\\\\\\\", "\\\\") // 백슬래쉬 두번 나오는거 ex) ESOUI\\ABC\\DEF 하나로 고침.
						;

				Pattern pp = Pattern.compile("msgctxt \"([0-9-]+)", Pattern.MULTILINE);
				Matcher mm = pp.matcher(source);
				while(mm.find()) listId.add(mm.group(1));

				Pattern p = Pattern.compile(pattern, Pattern.MULTILINE);
				Matcher m = p.matcher(source);

				//41714900-0-345 tip.po "////"
				//249936564-0-5081 quest-sub.po """Captain""
				//265851556-0-4666 journey.po ""Halion of Chrrol."" ~~

				while(m.find()) {

					if(m.group(1).equals("41714900-0-345")) {
						System.out.println(m.group(0));
						System.out.println(m.group(1));
						System.out.println(m.group(2));
						System.out.println(m.group(3));
					}

					String x = m.group(2).equals("") ? "" : ("["+ fileName+"]" + m.group(2));
					String y = m.group(3).equals("") ? "" : ("["+ fileName+"]" + m.group(3));
					sourceList.add(new PO(m.group(1), x, y));
				}

				System.out.println("================");
				System.out.println(ff);
				System.out.println(listId.size());
				total += listId.size();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        System.out.println("Total : "+total+" Lines");
		System.out.println(pattern);

		StringBuilder sb = new StringBuilder("\"Location\",\"Source\",\"Target\"\n");
		Collections.sort(sourceList);

		for(PO p : sourceList) sb.append(p.toCSV(false)).append("\n");
		try {
			FileUtils.writeStringToFile(new File(fx.getAbsolutePath()+sep+"/new.csv"), sb.toString(), charset);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ToLangMain toLangMain = new ToLangMain();
		toLangMain.start();
	}
}
