package org.metalscraps.eso.lang.tool;

import org.jsoup.helper.StringUtil;
import org.metalscraps.eso.lang.lib.config.AppWorkConfig;
import org.metalscraps.eso.lang.lib.util.Utils;
import org.metalscraps.eso.lang.tool.Utils.CategoryGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by 안병길 on 2018-01-17.
 * Whya5448@gmail.com
 */
class ToolMain {

	private final AppWorkConfig appWorkConfig = new AppWorkConfig();
	private static final Logger logger = LoggerFactory.getLogger(ToolMain.class);

	public static void main(String[] args) {
		var tool = new ToolMain();
		var config = tool.appWorkConfig;

		config.setBaseDirectoryToPath(FileSystemView.getFileSystemView().getDefaultDirectory().toPath().toAbsolutePath().resolve("Elder Scrolls Online/EsoKR"));
		config.setPODirectoryToPath(config.getBaseDirectoryToPath().resolve("PO_"+config.getToday()));
		config.setZanataCategoryConfigDirectoryToPath(config.getBaseDirectoryToPath().resolve("ZanataCategory"));
		try {  Files.createDirectories(config.getBaseDirectoryToPath()); }
		catch (IOException e) {
			logger.error("작업 폴더 생성 실패" + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}

		String command = "";
		logger.info(StringUtil.join(args, " "));
		for(var x : args) {
			if(x.startsWith("-opt")) command = x.substring(x.indexOf('=')+1);
			else if(x.startsWith("-base")) config.setBaseDirectoryToPath(Paths.get(x.substring(x.indexOf('=') + 1)));
			else if(x.startsWith("-po")) config.setPODirectoryToPath(Paths.get(x.substring(x.indexOf('=')+1)));
		}
		tool.start(command);
	}

	private void showMessage() {
		logger.info("baseDir : "+appWorkConfig.getBaseDirectoryToPath());
		logger.info("PODir : "+appWorkConfig.getPODirectoryToPath());
		logger.info("0. CSV To PO");
		logger.info("1. Zanata PO 다운로드");
		logger.info("2. PO 폰트 매핑/변환");
		logger.info("3. CSV 생성");
		logger.info("4. 기존 번역물 합치기");
		logger.info("44. 기존 번역물 합치기 => JSON");
		logger.info("5. 다!");

		logger.info("9. 종료");
		logger.info("11. TTC");
		logger.info("12. Destinations");
		logger.info("100. PO -> 구글 번역 (beta)");
		logger.info("300. Zanata upload용 csv category 생성");
	}

	private void start(String command) {

		JFileChooser jFileChooser = new JFileChooser();
		jFileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) { return f.isDirectory(); }
			@Override
			public String getDescription() { return "작업 폴더 설정"; }
		});
		jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jFileChooser.setMultiSelectionEnabled(false);
		jFileChooser.setCurrentDirectory(appWorkConfig.getBaseDirectoryToPath().toFile());
		CategoryGenerator CG = new CategoryGenerator(appWorkConfig);
		LangManager lm = new LangManager(appWorkConfig);
		switch (command) {
			case "help": showMessage(); break;
			case "0": lm.CsvToPo(); break;
			case "1": Utils.downloadPOs(appWorkConfig); break;
			case "2": Utils.convertKO_PO_to_CN(appWorkConfig); break;
			case "3": lm.makeCSVs(); break;
			case "4": lm.makeLang(); break;
			case "44":lm.makeLangToJSON(); break;
			case "5":
				Utils.downloadPOs(appWorkConfig);
				Utils.convertKO_PO_to_CN(appWorkConfig);
				lm.makeCSVs();
				lm.makeLang();
				break;
			case "9": System.exit(0);
			case "11": new TamrielTradeCentre(appWorkConfig).start(); break;
			case "12": new Destinations(appWorkConfig).start(); break;
			case "100": lm.translateGoogle(); break;
			case "200": CG.GenCategory(); break;
			case "300": lm.GenZanataUploadSet(); break;
		}

	}

}
