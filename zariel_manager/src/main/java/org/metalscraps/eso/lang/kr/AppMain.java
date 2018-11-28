package org.metalscraps.eso.lang.kr;

import org.metalscraps.eso.lang.kr.Utils.CategoryGenerator;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Scanner;

/**
 * Created by 안병길 on 2018-01-17.
 * Whya5448@gmail.com
 */
class AppMain {

	private final Scanner sc;
	private final AppWorkConfig appWorkConfig = new AppWorkConfig();
	private AppMain() { sc = new Scanner(System.in); }

	private AppWorkConfig getAppWorkConfig(){
		return this.appWorkConfig;
	}

	public static void main(String[] args) {

        // 서버용 자동화..
	    if(args.length > 0 && args[0].equals("-auto")) new ServerMain().start(args);
	    // 수동
	    else new AppMain().start();
	}

	private void showMessage() {
		System.out.println("0. CSV To PO");
		System.out.println("1. Zanata PO 다운로드");
		System.out.println("2. PO 폰트 매핑/변환");
		System.out.println("3. CSV 생성 (PO2)");
		System.out.println("33. CSV 생성 (PO)");
		System.out.println("4. 기존 번역물 합치기");
		System.out.println("44. 기존 번역물 합치기 => JSON");
		System.out.println("5. 다!");
		System.out.print("6. 작업폴더 변경 ");
		System.out.println(appWorkConfig.getBaseDirectory());
		System.out.print("7. PO 폴더 변경 ");
		System.out.println(appWorkConfig.getPODirectory());
		System.out.println("9. 종료");
		System.out.println("11. TTC");
		System.out.println("12. Destinations");
		System.out.println("100. PO -> 구글 번역 (beta)");
		System.out.println("200. USEP 사이트를 이용해 Po context id 인덱스 파일 생성");


	}

	private void workLangManager(JFileChooser jFileChooser) {
		CategoryGenerator CG = new CategoryGenerator(this.getAppWorkConfig());

		LangManager lm = new LangManager(appWorkConfig);

		switch(this.getCommand()) {
			case 0: lm.CsvToPo(); break;
			case 1: lm.getPO(); break;
			case 2: lm.Mapping(); break;
			case 3: lm.makeCSV(true); break;
			case 33: lm.makeCSV(false); break;
			case 4: lm.makeLang(); break;
			case 44: lm.makeLangToJSON(); break;
			case 5:
				lm.getPO();
				lm.Mapping();
				lm.makeCSV(true);
				lm.makeLang();
				break;
			case 6:
				if (jFileChooser.showOpenDialog(null) == JFileChooser.CANCEL_OPTION) break;
				appWorkConfig.setBaseDirectory(jFileChooser.getSelectedFile());
				appWorkConfig.setPODirectory(new File(appWorkConfig.getBaseDirectory()+"/PO_"+appWorkConfig.getToday()));
				break;
			case 7:
				if (jFileChooser.showOpenDialog(null) == JFileChooser.CANCEL_OPTION) break;
				appWorkConfig.setPODirectory(jFileChooser.getSelectedFile());
				break;
			case 8:
				System.gc();
                Runtime.getRuntime().gc();
                break;
			case 9: System.exit(0);
			case 11: new TamrielTradeCentre(appWorkConfig).start(); break;
			case 12: new Destinations(appWorkConfig).start(); break;
			case 100: lm.translateGoogle(); break;
			case 200: CG.GenSkillCategory();

		}
	}

	private void start() {

		JFileChooser jFileChooser = new JFileChooser();
		File workDir = new File(jFileChooser.getCurrentDirectory().getAbsolutePath()+"/Elder Scrolls Online/EsoKR");

		jFileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) { return f.isDirectory(); }
			@Override
			public String getDescription() { return "작업 폴더 설정"; }
		});
		jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jFileChooser.setMultiSelectionEnabled(false);
		jFileChooser.setCurrentDirectory(workDir);
		appWorkConfig.setBaseDirectory(workDir);
		appWorkConfig.setPODirectory(new File(appWorkConfig.getBaseDirectory()+"/PO_"+appWorkConfig.getToday()));
		//noinspection ResultOfMethodCallIgnored
		workDir.mkdirs();

		while(true) {
			showMessage();
			workLangManager(jFileChooser);
		}


	}

	private int getCommand() {
		System.out.print("명령:");
		String comm = sc.nextLine();
		try {
			return Integer.parseInt(comm);
		} catch (Exception e) {
			System.out.println("올바르지 않은 명령입니다.");
			System.err.println(e.getMessage());
			return getCommand();
		}
	}

}
