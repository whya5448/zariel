package org.metalscraps.eso.lang.kr;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.metalscraps.eso.lang.kr.Utils.SourceToMapConfig;
import org.metalscraps.eso.lang.kr.Utils.Utils;
import org.metalscraps.eso.lang.kr.bean.PO;
import org.metalscraps.eso.lang.kr.config.AppConfig;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by 안병길 on 2017-12-31.
 * Whya5448@gmail.com
 */
public class ToLangMain {

	private final String sep = System.getProperty("file.separator");

	private void start() {
		getSource();
	}

	private void getSource() {

		ArrayList<PO> sourceList = new ArrayList<>();
		ArrayList<PO> sourceWithTitle = new ArrayList<>();

		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setCurrentDirectory(FileUtils.getFile(FileUtils.getUserDirectoryPath() + sep + "desktop" + sep + "po"));
		//fc.setCurrentDirectory(FileUtils.getFile("C:/dev/po"));
		//if (fc.showOpenDialog(null) == JFileChooser.CANCEL_OPTION) System.exit(JFileChooser.CANCEL_OPTION);

		File fx = fc.getSelectedFile();
		fx = FileUtils.getFile("C:\\Users\\admin\\Documents\\Elder Scrolls Online\\live\\works\\EsoExtractData v0.31\\PO_0120");

		Collection<File> fileList = FileUtils.listFiles(fx, new String[]{"po2"}, false);
		for(File ff : fileList) {

			String fileName = FilenameUtils.getBaseName(ff.getName());
			// pregame 쪽 데이터
			if(fileName.equals("00_EsoUI_Client") || fileName.equals("00_EsoUI_Pregame")) continue;

			//41714900-0-345 tip.po "////"
			//249936564-0-5081 quest-sub.po """Captain""
			//265851556-0-4666 journey.po ""Halion of Chrrol."" ~~
			// 41714900-0-345|249936564-0-5081|265851556-0-4666

			sourceList.addAll( Utils.sourceToMap(new SourceToMapConfig().setFile(ff)).values() );
			System.out.println(ff);

		}
		System.out.println(AppConfig.POPattern);

		StringBuilder sb = new StringBuilder("\"Location\",\"Source\",\"Target\"\n");
		Collections.sort(sourceList);

		for(PO p : sourceList) sb.append(p.toCSV(false)).append("\n");
		try {
			FileUtils.writeStringToFile(new File(fx.getAbsolutePath()+sep+"/new.csv"), sb.toString(), AppConfig.CHARSET);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ToLangMain toLangMain = new ToLangMain();
		toLangMain.start();
	}
}
