package org.metalscraps.eso.lang.kr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
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

	private final ArrayList<PO> sourceList = new ArrayList<>();
	private final AppWorkConfig appWorkConfig;

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

			LocalTime totalEd = LocalTime.now();
			System.out.println("총 " + totalSt.until(totalEd, ChronoUnit.SECONDS) + "초");

		} catch (Exception e) { e.printStackTrace(); }

	}

	public void Mapping() {

		HashMap<Integer, StringBuilder> threadResultMap = new HashMap<>();
		int cores = Runtime.getRuntime().availableProcessors();
		Collection<File> fileList = FileUtils.listFiles(appWorkConfig.getPODirectory(), new String[]{"po"}, false);

		try {

			for (File file : fileList) {

				// 파일명
				System.out.println(file);

				// 스레드 결과값 초기화
				threadResultMap.clear();

				// 파일 불러옴.
				StringBuilder source = new StringBuilder(FileUtils.readFileToString(file, AppConfig.CHARSET));

				int length = source.length(), lengthEach = length / cores;

				// 파일별로 매핑하게 바꾼 이상 쓰레드 나눌 필요가 있는지? 테스트 해보기. 언젠간?
				ArrayList<StringBuilder> textList = new ArrayList<>();
				for (int i = 0; i < cores; i++) {
					StringBuilder sb;

					// 마지막 코어 텍스트 끝까지.
					if (i == cores - 1) sb = new StringBuilder(source.substring(lengthEach * i));

						// 그 외 코어 나눈대로 할당.
					else sb = new StringBuilder(source.substring(lengthEach * i, lengthEach * (i + 1)));
					textList.add(sb);
				}

				// 작업물 나눴으므로 초기화.
				source = new StringBuilder(length);

				int i = 0;
				for (StringBuilder sbb : textList) new Thread(new ReplaceMain().setStringBuilder(sbb).setOrder(++i).setMap(threadResultMap)).start();

				// 모든 쓰레드 작업 완료까지 3초씩 대기.
				while (true) {
					if (threadResultMap.size() == cores) break;
					try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
				}

				for (i = 1; i <= cores; i++) source.append(threadResultMap.get(i));

				FileUtils.write(new File(file.getAbsolutePath() + "2"), source, AppConfig.CHARSET);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Created by 안병길 on 2018-01-13.
	 * Whya5448@gmail.com
	 */

	@Data
	@Accessors(chain = true)
	private static class ReplaceMain implements Runnable {

		private StringBuilder stringBuilder;
		private int order;
		private Map<Integer, StringBuilder> map;

		@Override
		public void run() { map.put(order, Utils.replaceStringFromMap(stringBuilder, Utils.koToCnMap)); }
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
		makeFile(new File(appWorkConfig.getBaseDirectory()+"/kr_"+appWorkConfig.getTodayWithYear()+".csv"), new ToCSVConfig());
		makeFile(new File(appWorkConfig.getBaseDirectory()+"/krWithFileName_"+appWorkConfig.getTodayWithYear()+".csv"), new ToCSVConfig().setWriteFileName(true));
		makeFile(new File(appWorkConfig.getBaseDirectory()+"/krWithOutEnglishTitle_"+appWorkConfig.getTodayWithYear()+".csv"), new ToCSVConfig().setRemoveEnglishComment(true));

	}

	private void makeFile(File file, ToCSVConfig toCSVConfig) {
		StringBuilder sb = new StringBuilder("\"Location\",\"Source\",\"Target\"\n");
		for(PO p : sourceList) sb.append(p.toCSV(toCSVConfig));
		try {
			FileUtils.writeStringToFile(file, sb.toString(), AppConfig.CHARSET);
		} catch (IOException e) {
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

		if(fileLinkedList.size() == 0) System.exit(JFileChooser.CANCEL_OPTION);

		for(File file : fileLinkedList) {
			System.out.println(file);
			map.putAll( Utils.sourceToMap(new SourceToMapConfig().setFile(file).setPattern(AppConfig.CSVPattern)) );
		}

		ArrayList<PO> arrayList = new ArrayList<>(map.values());
		Collections.sort(arrayList);

		StringBuilder sb = new StringBuilder("\"Location\",\"Source\",\"Target\"\n");
		for(PO p : arrayList) sb.append(p.toCSV(new ToCSVConfig()));

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

	}
}
