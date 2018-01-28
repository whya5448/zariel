package org.metalscraps.eso.lang.kr;

import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.metalscraps.eso.lang.kr.Utils.GoogleTranslate;
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
	private final ArrayList<PO> transList = new ArrayList<>();
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
		makeFile(new File(appWorkConfig.getBaseDirectory()+"/kr_"+appWorkConfig.getTodayWithYear()+".csv"), new ToCSVConfig());
		makeFile(new File(appWorkConfig.getBaseDirectory()+"/krWithFileName_"+appWorkConfig.getTodayWithYear()+".csv"), new ToCSVConfig().setWriteFileName(true));
		makeFile(new File(appWorkConfig.getBaseDirectory()+"/krWithOutEnglishTitle_"+appWorkConfig.getTodayWithYear()+".csv"), new ToCSVConfig().setRemoveComment(true));

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

	private void makeFile(File file, ToCSVConfig toCSVConfig, ArrayList<PO> poList) {
		StringBuilder sb = new StringBuilder("\"Location\",\"Source\",\"Target\"\n");
		for(PO p : poList) sb.append(p.toCSV(toCSVConfig));
		try {
			FileUtils.writeStringToFile(file, sb.toString(), AppConfig.CHARSET);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void translateGoogle(){
		try {
			File file = new File("C:\\Users\\my\\Documents\\Elder Scrolls Online\\EsoKR\\PO_0128\\achievement.po");
			ArrayList<PO> fileItems = new ArrayList<>();
			fileItems.addAll(Utils.sourceToMap(new SourceToMapConfig().setFile(file).setPattern(AppConfig.POPattern)).values());
			System.out.println("target : " + file);

			int requestCount = 0;

			ArrayList<PO> skippedItem = new ArrayList<>();
			ArrayList<PO> translatedItem = new ArrayList<>();

			ArrayList<Thread> workerList = new ArrayList<Thread>();
			GoogleTranslate worker = new GoogleTranslate();
			for (PO oneItem : fileItems) {
				if (oneItem.getSource().equals(oneItem.getTarget())) {
					worker.addJob(oneItem);
					Thread transWork = new Thread(worker);
					transWork.start();
					workerList.add(transWork);
					requestCount++;
				} else {
					skippedItem.add(oneItem);
				}

				if(requestCount > 10){
					System.out.println("wait for Google translate....");
					for (Thread t : workerList) {
						t.join();
					}
					requestCount = 0;
				}
			}

			this.transList.addAll(skippedItem);
			this.transList.addAll(worker.getResult());

			System.out.println("Convert job done! file data count ["+fileItems.size()+"] translist cound ["+this.transList.size()+"]");


		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void translateToCSV(){
		makeFile(new File(appWorkConfig.getBaseDirectory()+"/kr_"+appWorkConfig.getTodayWithYear()+".csv"), new ToCSVConfig(), this.transList);
	}

	public void csvMapping(){
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
}
