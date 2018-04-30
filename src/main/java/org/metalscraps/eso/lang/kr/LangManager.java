package org.metalscraps.eso.lang.kr;

import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.metalscraps.eso.lang.kr.Utils.PoConverter;
import org.metalscraps.eso.lang.kr.Utils.SourceToMapConfig;
import org.metalscraps.eso.lang.kr.Utils.Utils;
import org.metalscraps.eso.lang.kr.bean.PO;
import org.metalscraps.eso.lang.kr.bean.ToCSVConfig;
import org.metalscraps.eso.lang.kr.config.AppConfig;
import org.metalscraps.eso.lang.kr.config.FileNames;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 안병길 on 2018-01-24.
 * Whya5448@gmail.com
 */

@AllArgsConstructor
class LangManager {
	private PoConverter PC = new PoConverter();
	private final AppWorkConfig appWorkConfig;

	LangManager(AppWorkConfig appWorkConfig) {
		this.appWorkConfig = appWorkConfig;
	}

	public void CsvToPo() {

		// EsoExtractData.exe depot/eso.mnf export -a 0
		// EsoExtractData.exe -l en_0124.lang -p

		LinkedList<File> fileLinkedList = new LinkedList<>();
		HashMap<String, PO> map = new HashMap<>();
		HashMap<String, PO> map2 = new HashMap<>();
		HashMap<String, String> map3 = new HashMap<>();
		HashMap<Integer, String> map4 = new HashMap<>();

		JFileChooser jFileChooser = new JFileChooser();
		jFileChooser.setMultiSelectionEnabled(false);
		jFileChooser.setCurrentDirectory(appWorkConfig.getBaseDirectory());
		jFileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return FilenameUtils.getExtension(f.getName()).equals("csv") | f.isDirectory();
			}

			@Override
			public String getDescription() {
				return "*.csv";
			}
		});

		while (jFileChooser.showOpenDialog(null) != JFileChooser.CANCEL_OPTION) {
			jFileChooser.setCurrentDirectory(jFileChooser.getSelectedFile());
			fileLinkedList.add(jFileChooser.getSelectedFile());
		}

		if (fileLinkedList.size() == 0) return;

		SourceToMapConfig sourceToMapConfig = new SourceToMapConfig().setPattern(AppConfig.CSVPattern);
		for (File file : fileLinkedList) {
			System.out.println(file);
			map.putAll(Utils.sourceToMap(sourceToMapConfig.setFile(file)));
		}

		Collection<File> fileList = FileUtils.listFiles(appWorkConfig.getPODirectory(), new String[]{"po"}, false);
		for (File file : fileList) {
			String fileName = FilenameUtils.getBaseName(file.getName());

			// pregame 쪽 데이터
			if (fileName.equals("00_EsoUI_Client") || fileName.equals("00_EsoUI_Pregame")) continue;

			//41714900-0-345 tip.po "////"
			//249936564-0-5081 quest-sub.po """Captain""
			//265851556-0-4666 journey.po ""Halion of Chrrol."" ~~
			// 41714900-0-345|249936564-0-5081|265851556-0-4666

			map2.putAll(Utils.sourceToMap(new SourceToMapConfig().setFile(file).setPattern(AppConfig.POPattern)));
		}

		for (PO p : map2.values()) {
			map3.put(p.getSource(), p.getFileName().getName());
			map4.put(p.getId1(), p.getFileName().getName());
		}

		for (Map.Entry<String, PO> entry : map.entrySet()) {
			PO s = entry.getValue();
			PO x = map2.get(entry.getKey());

			if (x != null) s.setFileName(x.getFileName());
			else {
				String pp = map4.get(s.getId1());
				if (pp != null) s.setFileName(FileNames.fromString(pp));
				else {
					pp = map3.get(s.getSource());
					if (pp != null) s.setFileName(FileNames.fromString(pp));
					else {

						System.out.println(entry);
						s.setFileName(null);

					}
				}
			}
		}

		HashMap<String, StringBuilder> builderMap = new HashMap<>();
		String fileName;

		ArrayList<PO> sort = new ArrayList<>(map.values());
		Collections.sort(sort);
		for (PO p : sort) {
			fileName = p.getFileName().getName();
			StringBuilder sb = builderMap.get(fileName);
			if (sb == null) {
				sb = new StringBuilder(
						"# Administrator <admin@the.gg>, 2017. #zanata\n" +
								"msgid \"\"\n" +
								"msgstr \"\"\n" +
								"\"MIME-Version: 1.0\\n\"\n" +
								"\"Content-Transfer-Encoding: 8bit\\n\"\n" +
								"\"Content-Type: text/plain; charset=UTF-8\\n\"\n" +
								"\"PO-Revision-Date: 2018-01-24 02:12+0900\\n\"\n" +
								"\"Last-Translator: Administrator <admin@the.gg>\\n\"\n" +
								"\"Language-Team: Korean\\n\"\n" +
								"\"Language: ko\\n\"\n" +
								"\"X-Generator: Zanata 4.2.4\\n\"\n" +
								"\"Plural-Forms: nplurals=1; plural=0\\n\""
				);
				builderMap.put(fileName, sb);
			}
			sb.append(p.toPO());
		}

		for (StringBuilder sb : builderMap.values()) {
			Pattern p = Pattern.compile("\\\\(?!n)");
			Matcher m = p.matcher(sb);
			String x = m.replaceAll("\\\\$0");
			sb.delete(0, sb.length());
			sb.append(x);
		}


		try {

			for (Map.Entry<String, StringBuilder> entry : builderMap.entrySet()) {
				FileUtils.writeStringToFile(new File(appWorkConfig.getBaseDirectory() + "/temp14/" + entry.getKey() + ".pot"), entry.getValue().toString(), AppConfig.CHARSET);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getPO() {

		final String url = "http://www.dostream.com/zanata/rest/file/translation/esokr/3.3.8.1567568/ko/po?docId=";
		final File baseDirectory = appWorkConfig.getBaseDirectory();
		final File PODirectory = new File(baseDirectory.getAbsolutePath() + "/PO_" + appWorkConfig.getToday());
		appWorkConfig.setPODirectory(PODirectory);

		try {

			FileNames[] fileNames = FileNames.values();
			LocalTime totalSt = LocalTime.now();

			for (FileNames fileName : fileNames) {
				LocalTime st = LocalTime.now();
				System.out.print(fileName);

				File po = new File(PODirectory.getAbsolutePath() + "/" + fileName + ".po");
				FileUtils.writeStringToFile(po, IOUtils.toString(new URL(url + fileName), AppConfig.CHARSET), AppConfig.CHARSET);

				LocalTime ed = LocalTime.now();
				System.out.println(" " + st.until(ed, ChronoUnit.SECONDS) + "초");
			}

			System.out.println("총 " + totalSt.until(LocalTime.now(), ChronoUnit.SECONDS) + "초");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void Mapping() {

		Collection<File> fileList = FileUtils.listFiles(appWorkConfig.getPODirectory(), new String[]{"po"}, false);

		try {
			for (File file : fileList) FileUtils.write(new File(file.getAbsolutePath() + "2"), Utils.KOToCN(FileUtils.readFileToString(file, AppConfig.CHARSET)), AppConfig.CHARSET);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	void makeCSV(boolean usePO2) {

		ArrayList<PO> sourceList = new ArrayList<>();

		Collection<File> fileList = FileUtils.listFiles(appWorkConfig.getPODirectory(), new String[]{usePO2?"po2":"po"}, false);
		for (File file : fileList) {

			String fileName = FilenameUtils.getBaseName(file.getName());

			// pregame 쪽 데이터
			if (fileName.equals("00_EsoUI_Client") || fileName.equals("00_EsoUI_Pregame")) continue;

			//41714900-0-345 tip.po "////"
			//249936564-0-5081 quest-sub.po """Captain""
			//265851556-0-4666 journey.po ""Halion of Chrrol."" ~~
			// 41714900-0-345|249936564-0-5081|265851556-0-4666

			sourceList.addAll(Utils.sourceToMap(new SourceToMapConfig().setFile(file).setPattern(AppConfig.POPattern)).values());
			System.out.println(file);

		}

		ToCSVConfig csvConfig = new ToCSVConfig();
		csvConfig.setWriteSource(true);
		Collections.sort(sourceList);

		makeFile(new File(appWorkConfig.getBaseDirectory() + "/kr_" + appWorkConfig.getTodayWithYear() + (usePO2?".po2":".po") + ".csv"), csvConfig, sourceList);
		makeFile(new File(appWorkConfig.getBaseDirectory() + "/krWithFileName_" + appWorkConfig.getTodayWithYear() + (usePO2?".po2":".po") + ".csv"), csvConfig.setWriteFileName(true), sourceList);
		//makeFile(new File(appWorkConfig.getBaseDirectory() + "/krWithOutEnglishTitle_" + appWorkConfig.getTodayWithYear() + (usePO2?".po2":".po") + ".csv"), csvConfig.setRemoveComment(true), sourceList);

	}


	private void makeFile(File file, ToCSVConfig toCSVConfig, ArrayList<PO> poList) {
		StringBuilder sb = new StringBuilder("\"Location\",\"Source\",\"Target\"\n");
		for (PO p : poList) sb.append(p.toCSV(toCSVConfig));
		try {
			FileUtils.writeStringToFile(file, sb.toString(), AppConfig.CHARSET);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void makeLangToJSON() {

		// EsoExtractData.exe depot/eso.mnf export -a 0
		// EsoExtractData.exe -l en_0124.lang -p

		LinkedList<File> fileLinkedList = new LinkedList<>();
		HashMap<String, PO> map = new HashMap<>();

		JFileChooser jFileChooser = new JFileChooser();
		jFileChooser.setMultiSelectionEnabled(false);
		jFileChooser.setCurrentDirectory(appWorkConfig.getBaseDirectory());
		jFileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return FilenameUtils.getExtension(f.getName()).equals("csv") | f.isDirectory();
			}

			@Override
			public String getDescription() {
				return "*.csv";
			}
		});

		while (jFileChooser.showOpenDialog(null) != JFileChooser.CANCEL_OPTION) {
			jFileChooser.setCurrentDirectory(jFileChooser.getSelectedFile());
			fileLinkedList.add(jFileChooser.getSelectedFile());
		}

		if (fileLinkedList.size() == 0) return;

		SourceToMapConfig sourceToMapConfig = new SourceToMapConfig().setPattern(AppConfig.CSVPattern);
		for (File file : fileLinkedList) {
			System.out.println(file);
			map.putAll(Utils.sourceToMap(sourceToMapConfig.setFile(file)));
		}

		JSONObject jsonObject = new JSONObject();
		for (PO p : map.values()) jsonObject.put(p.getId(), p.getFileName() + "_" + Utils.CNtoKO((p.getTarget())));
		String x = jsonObject.toString();

		System.out.println(x);

		try {
			FileUtils.writeStringToFile(new File(appWorkConfig.getBaseDirectory() + "/json.json"), x, AppConfig.CHARSET);
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	void makeLang() {

		// EsoExtractData.exe depot/eso.mnf export -a 0
		// EsoExtractData.exe -l en_0124.lang -p

		LinkedList<File> fileLinkedList = new LinkedList<>();
		ArrayList<PO> sourceList = new ArrayList<>();
		HashMap<String, PO> ko;

		JFileChooser jFileChooser = new JFileChooser();
		jFileChooser.setMultiSelectionEnabled(false);
		jFileChooser.setCurrentDirectory(appWorkConfig.getBaseDirectory());
		jFileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return FilenameUtils.getExtension(f.getName()).equals("csv") | f.isDirectory();
			}

			@Override
			public String getDescription() {
				return "*.csv";
			}
		});

		while (jFileChooser.showOpenDialog(null) != JFileChooser.CANCEL_OPTION) {
			jFileChooser.setCurrentDirectory(jFileChooser.getSelectedFile());
			fileLinkedList.add(jFileChooser.getSelectedFile());
		}

		if (fileLinkedList.size() != 2) return;

		SourceToMapConfig sourceToMapConfig = new SourceToMapConfig().setPattern(AppConfig.CSVPattern);

		ko = Utils.sourceToMap(sourceToMapConfig.setFile(fileLinkedList.get(0)));
		ko.putAll(Utils.sourceToMap(sourceToMapConfig.setFile(fileLinkedList.get(1))));

		ko.get("242841733-0-54340").setTarget(Utils.KOToCN("매지카 물약"));

		HashMap<FileNames, HashMap<String, ArrayList<PO>>> motherMap = new HashMap<>();
		for(PO p : ko.values()) {
			HashMap<String, ArrayList<PO>> childMap;
			ArrayList<PO> childList;

			if(motherMap.containsKey(p.getFileName())) childMap = motherMap.get(p.getFileName());
			else motherMap.put(p.getFileName(), childMap = new HashMap<>());

			if(childMap.containsKey(p.getSource())) childList = childMap.get(p.getSource());
			else childMap.put(p.getSource(), childList = new ArrayList<>());

			childList.add(p);
		}

		for(HashMap<String, ArrayList<PO>> childMap : motherMap.values()) {
			for(ArrayList<PO> childList : childMap.values()) {
				PO p = null;
				for(PO x : childList) if(!x.getSource().equals(x.getTarget())) p = x;
				if(p != null) for(PO x : childList) if(x.getSource().equals(x.getTarget())) x.setTarget(p.getTarget());
			}
		}


		for(Map<String, ArrayList<PO>> childMap : motherMap.values()) for(List childList : childMap.values()) sourceList.addAll(childList);
		Collections.sort(sourceList);


		StringBuilder sb = new StringBuilder("\"Location\",\"Source\",\"Target\"\n");
		ToCSVConfig toCSVConfig = new ToCSVConfig();
		toCSVConfig.setWriteSource(true);
		for (PO p : sourceList) sb.append(p.toCSV(toCSVConfig));

		if(fileLinkedList.getLast().getName().contains(".po.")) return;

		try {

			FileUtils.writeStringToFile(new File(appWorkConfig.getBaseDirectory() + "/" + fileLinkedList.getLast().getName() + ".merged.csv"), sb.toString(), AppConfig.CHARSET);

			ProcessBuilder pb = new ProcessBuilder()
					.directory(appWorkConfig.getBaseDirectory())
					.command(appWorkConfig.getBaseDirectory() + "/EsoExtractData.exe\" -x " + fileLinkedList.getLast().getName() + ".merged.csv -p")
					.redirectError(ProcessBuilder.Redirect.INHERIT)
					.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			pb.start().waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}

/*
		sb = new StringBuilder("\"Location\",\"Source\",\"Target\"\n");
		toCSVConfig.setRemoveComment(true);

		for (PO p : sourceList) sb.append(p.toCSV(toCSVConfig));

		try {

			FileUtils.writeStringToFile(new File(appWorkConfig.getBaseDirectory() + "/" + fileLinkedList.getLast().getName() + ".merged.no.comment.csv"), sb.toString(), AppConfig.CHARSET);

			ProcessBuilder pb = new ProcessBuilder()
					.directory(appWorkConfig.getBaseDirectory())
					.command(appWorkConfig.getBaseDirectory() + "/EsoExtractData.exe\" -x " + fileLinkedList.getLast().getName() + ".merged.no.comment.csv -p")
					.redirectError(ProcessBuilder.Redirect.INHERIT)
					.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			pb.start().waitFor();

		} catch (Exception e) {
			e.printStackTrace();
		}
*/

	}

	public void translateGoogle() {
		this.PC.setAppWorkConfig(this.appWorkConfig);
		this.PC.translateGoogle();
	}
}