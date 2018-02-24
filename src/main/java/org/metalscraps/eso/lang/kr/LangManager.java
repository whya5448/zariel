package org.metalscraps.eso.lang.kr;

import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
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

/**
 * Created by 안병길 on 2018-01-24.
 * Whya5448@gmail.com
 */

@AllArgsConstructor
public class LangManager {
	private PoConverter PC = new PoConverter();
	private final AppWorkConfig appWorkConfig;
	private final ArrayList<PO> sourceList = new ArrayList<>();

	LangManager(AppWorkConfig appWorkConfig) {
		this.appWorkConfig = appWorkConfig;
	}

	public void CsvToPo() {

		// EsoExtractData.exe depot/eso.mnf export -a 0
		// EsoExtractData.exe -l en_0124.lang -p

		LinkedList<File> fileLinkedList = new LinkedList<>();
		HashMap<String, PO> map = new HashMap<>();
		HashMap<String, PO> map2 = new HashMap<>();
		HashMap<String, PO> map3 = new HashMap<>();

		JFileChooser jFileChooser = new JFileChooser();
		jFileChooser.setMultiSelectionEnabled(false);
		jFileChooser.setCurrentDirectory(appWorkConfig.getBaseDirectory());
		jFileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) { return FilenameUtils.getExtension(f.getName()).equals("csv") | f.isDirectory(); }

			@Override
			public String getDescription() { return "*.csv"; }
		});

		while(jFileChooser.showOpenDialog(null) != JFileChooser.CANCEL_OPTION) {
			jFileChooser.setCurrentDirectory(jFileChooser.getSelectedFile());
			fileLinkedList.add(jFileChooser.getSelectedFile());
		}

		if(fileLinkedList.size() == 0) return;

		SourceToMapConfig sourceToMapConfig = new SourceToMapConfig().setPattern(AppConfig.CSVPattern);
		for(File file : fileLinkedList) {
			System.out.println(file);
			map.putAll( Utils.sourceToMap(sourceToMapConfig.setFile(file)));
		}

		Collection<File> fileList = FileUtils.listFiles(appWorkConfig.getPODirectory(), new String[]{"po"}, false);
		for(File file : fileList) {
			String fileName = FilenameUtils.getBaseName(file.getName());

			// pregame 쪽 데이터
			if(fileName.equals("00_EsoUI_Client") || fileName.equals("00_EsoUI_Pregame")) continue;

			//41714900-0-345 tip.po "////"
			//249936564-0-5081 quest-sub.po """Captain""
			//265851556-0-4666 journey.po ""Halion of Chrrol."" ~~
			// 41714900-0-345|249936564-0-5081|265851556-0-4666

			map2.putAll( Utils.sourceToMap(new SourceToMapConfig().setFile(file).setPattern(AppConfig.POPattern)) );
			System.out.println(file);
		}

		for(PO p : map2.values()) map3.put(p.getSource(), p);

		System.out.println("Entry");
		for(Map.Entry<String, PO> entry : map.entrySet()) {
			System.out.println(entry.getValue());
			PO s = entry.getValue();
			PO x = map2.get(entry.getKey());

			if(x != null) s.setFileName(x.getFileName());
			else {
				PO pp = map3.get(s.getSource());
				if(pp!=null) s.setFileName(pp.getFileName());
			}
		}
		System.out.println("Entry End");

		for(PO p : map.values()) {
			if(p.getFileName() == null || p.getFileName().equals("")) p.setFileName("3.3.6.1561871");
			System.out.println(p);
		}

		map2 = null;

		System.out.println("To...");
		HashMap<String, StringBuilder> builderMap = new HashMap<>();
		String fileName;
		for(PO p : map.values()) {
			System.out.println(p);
			fileName = p.getFileName();
			StringBuilder sb = builderMap.get(fileName);
			if(sb == null) {
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

		try {

			for(Map.Entry<String, StringBuilder> entry : builderMap.entrySet()) {
				System.out.println(entry.getKey());
				FileUtils.writeStringToFile(new File(appWorkConfig.getBaseDirectory() + "/temp/" + entry.getKey() + ".pot"), entry.getValue().toString(), AppConfig.CHARSET);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getPO() {

		final String url = "http://www.dostream.com/zanata/rest/file/translation/esokr/3.2.6.1517120/ko/po?docId=";
		final File baseDirectory = appWorkConfig.getBaseDirectory();
		final File PODirectory = new File(baseDirectory.getAbsolutePath()+"/PO_"+appWorkConfig.getToday());
		appWorkConfig.setPODirectory(PODirectory);

		try {

			FileNames[] fileNames = FileNames.values();
			LocalTime totalSt = LocalTime.now();

			for (FileNames fileName : fileNames) {
				LocalTime st = LocalTime.now();
				System.out.print(fileName);

				File po = new File(PODirectory.getAbsolutePath()+"/"+fileName+".po");
				FileUtils.writeStringToFile(po, IOUtils.toString(new URL(url + fileName), AppConfig.CHARSET), AppConfig.CHARSET);

				LocalTime ed = LocalTime.now();
				System.out.println(" "+st.until(ed, ChronoUnit.SECONDS) + "초");
			}

			System.out.println("총 " + totalSt.until(LocalTime.now(), ChronoUnit.SECONDS) + "초");

		} catch (Exception e) { e.printStackTrace(); }

	}

	public void Mapping() {

		Collection<File> fileList = FileUtils.listFiles(appWorkConfig.getPODirectory(), new String[]{"po"}, false);

		try {
			for (File file : fileList) FileUtils.write(new File(file.getAbsolutePath() + "2"), Utils.KOToCN(FileUtils.readFileToString(file, AppConfig.CHARSET)), AppConfig.CHARSET);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void makeCSV() {

		Collection<File> fileList = FileUtils.listFiles(appWorkConfig.getPODirectory(), new String[]{"po2"}, false);
		for(File file : fileList) {

			String fileName = FilenameUtils.getBaseName(file.getName());

			// pregame 쪽 데이터
			if(fileName.equals("00_EsoUI_Client") || fileName.equals("00_EsoUI_Pregame")) continue;

			//41714900-0-345 tip.po "////"
			//249936564-0-5081 quest-sub.po """Captain""
			//265851556-0-4666 journey.po ""Halion of Chrrol."" ~~
			// 41714900-0-345|249936564-0-5081|265851556-0-4666

			sourceList.addAll( Utils.sourceToMap(new SourceToMapConfig().setFile(file).setPattern(AppConfig.POPattern)).values() );
			System.out.println(file);

		}
		System.out.println(AppConfig.POPattern);

		Collections.sort(sourceList);
		makeFile(new File(appWorkConfig.getBaseDirectory()+"/kr_"+appWorkConfig.getTodayWithYear()+".csv"), new ToCSVConfig(), sourceList);
		makeFile(new File(appWorkConfig.getBaseDirectory()+"/krWithFileName_"+appWorkConfig.getTodayWithYear()+".csv"), new ToCSVConfig().setWriteFileName(true), sourceList);
		makeFile(new File(appWorkConfig.getBaseDirectory()+"/krWithOutEnglishTitle_"+appWorkConfig.getTodayWithYear()+".csv"), new ToCSVConfig().setRemoveComment(true), sourceList);

	}

	private void makeFile(File file, ToCSVConfig toCSVConfig, ArrayList<PO> poList) {
		StringBuilder sb = new StringBuilder("\"Location\",\"Source\",\"Target\"\n");
		for(PO p : poList) sb.append(p.toCSV(toCSVConfig));
		try {
			FileUtils.writeStringToFile(file, sb.toString(), AppConfig.CHARSET);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public void csvMapping() {
		File file = new File(appWorkConfig.getBaseDirectory()+"/kr_"+appWorkConfig.getTodayWithYear()+".csv");
		System.out.println(file);

		try {
			FileUtils.write(file, Utils.KOToCN(FileUtils.readFileToString(file, AppConfig.CHARSET)), AppConfig.CHARSET);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void makeLang() {

		// EsoExtractData.exe depot/eso.mnf export -a 0
		// EsoExtractData.exe -l en_0124.lang -p

		LinkedList<File> fileLinkedList = new LinkedList<>();
		HashMap<String, PO> map = new HashMap<>();

		JFileChooser jFileChooser = new JFileChooser();
		jFileChooser.setMultiSelectionEnabled(false);
		jFileChooser.setCurrentDirectory(appWorkConfig.getBaseDirectory());
		jFileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) { return FilenameUtils.getExtension(f.getName()).equals("csv") | f.isDirectory(); }

			@Override
			public String getDescription() { return "*.csv"; }
		});

		while(jFileChooser.showOpenDialog(null) != JFileChooser.CANCEL_OPTION) {
			jFileChooser.setCurrentDirectory(jFileChooser.getSelectedFile());
			fileLinkedList.add(jFileChooser.getSelectedFile());
		}

		if(fileLinkedList.size() == 0) return;

		SourceToMapConfig sourceToMapConfig = new SourceToMapConfig().setPattern(AppConfig.CSVPattern);
		for(File file : fileLinkedList) {
			System.out.println(file);
			map.putAll( Utils.sourceToMap(sourceToMapConfig.setFile(file)));
		}

		ArrayList<PO> arrayList = new ArrayList<>(map.values());
		Collections.sort(arrayList);

		StringBuilder sb = new StringBuilder("\"Location\",\"Source\",\"Target\"\n");
		ToCSVConfig toCSVConfig = new ToCSVConfig();
		for(PO p : arrayList) sb.append(p.toCSV(toCSVConfig));

		try {

			FileUtils.writeStringToFile(new File(appWorkConfig.getBaseDirectory()+"/"+fileLinkedList.getLast().getName()+".merged.csv"), sb.toString(), AppConfig.CHARSET);

			ProcessBuilder pb = new ProcessBuilder()
					.directory(appWorkConfig.getBaseDirectory())
					.command(appWorkConfig.getBaseDirectory()+"/EsoExtractData.exe\" -x "+fileLinkedList.getLast().getName()+".merged.csv -p")
					.redirectError(ProcessBuilder.Redirect.INHERIT)
					.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			pb.start().waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}

		sb = new StringBuilder("\"Location\",\"Source\",\"Target\"\n");
		toCSVConfig.setRemoveComment(true);
		for(PO p : arrayList) sb.append(p.toCSV(toCSVConfig));

		try {

			FileUtils.writeStringToFile(new File(appWorkConfig.getBaseDirectory()+"/"+fileLinkedList.getLast().getName()+".merged.no.comment.csv"), sb.toString(), AppConfig.CHARSET);

			ProcessBuilder pb = new ProcessBuilder()
					.directory(appWorkConfig.getBaseDirectory())
					.command(appWorkConfig.getBaseDirectory()+"/EsoExtractData.exe\" -x "+fileLinkedList.getLast().getName()+".merged.no.comment.csv -p")
					.redirectError(ProcessBuilder.Redirect.INHERIT)
					.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			pb.start().waitFor();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void translateGoogle(){
		this.PC.setAppWorkConfig(this.appWorkConfig);
		this.PC.translateGoogle();
	}


}
