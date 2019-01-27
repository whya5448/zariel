package org.metalscraps.eso.lang.kr;

import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.metalscraps.eso.lang.kr.Utils.CategoryGenerator;
import org.metalscraps.eso.lang.kr.Utils.PoConverter;
import org.metalscraps.eso.lang.kr.bean.CategoryCSV;
import org.metalscraps.eso.lang.kr.config.CSVmerge;
import org.metalscraps.eso.lang.lib.bean.PO;
import org.metalscraps.eso.lang.lib.bean.ToCSVConfig;
import org.metalscraps.eso.lang.lib.config.AppConfig;
import org.metalscraps.eso.lang.lib.config.AppWorkConfig;
import org.metalscraps.eso.lang.lib.config.FileNames;
import org.metalscraps.eso.lang.lib.config.SourceToMapConfig;
import org.metalscraps.eso.lang.lib.util.Utils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
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

	void CsvToPo() {

		// EsoExtractData.exe depot/eso.mnf export -a 000 -s 1472 -e 1472
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
					else s.setFileName(null);

				}
			}
		}

		ArrayList<PO> poList = new ArrayList<>(map.values());
		makePotFile(poList, false);
	}


	void GenZanataUploadSet(){
		CategoryGenerator originCG = new CategoryGenerator(appWorkConfig);
		originCG.GenCategoryConfigMap(appWorkConfig.getZanataCategoryConfigDirectory().toString()+"\\IndexMatch.txt");
		originCG.GenCategory();
		HashSet<CategoryCSV> categorizedCSV = originCG.getCategorizedCSV();

		CSVmerge merge = new CSVmerge();
		HashMap<String, PO> targetCSV = new HashMap<>();
		Collection<File> fileList = FileUtils.listFiles(appWorkConfig.getPODirectory(), new String[]{"po"}, false);
		for (File file : fileList) {

			String fileName = FilenameUtils.getBaseName(file.getName());
			// pregame 쪽 데이터
			if (fileName.equals("00_EsoUI_Client") || fileName.equals("00_EsoUI_Pregame")) continue;

			targetCSV.putAll(Utils.sourceToMap(new SourceToMapConfig().setFile(file).setPattern(AppConfig.POPattern)));
			System.out.println("zanata po parsed ["+file+"] ");
		}

		merge.MergeCSV(categorizedCSV, targetCSV, false);

		for(CategoryCSV oneCSV : categorizedCSV){
			CustomPOmodify(oneCSV);
			HashMap<String, PO> mergedPO = oneCSV.getPODataMap();
			ArrayList<PO> poList = new ArrayList<>(mergedPO.values());
			makePotFile(poList, false, oneCSV.getZanataFileName(), oneCSV.getType(), "src", "ko", "pot");
			makePotFile(poList, true, oneCSV.getZanataFileName(), oneCSV.getType(), "trs", "ko", "po");
		}

		System.out.println("Select Csv file for generate ja-JP locale");
		targetCSV = originCG.GetSelectedCSVMap();
		merge.MergeCSV(categorizedCSV, targetCSV, true);
		for(CategoryCSV oneCSV : categorizedCSV){
			HashMap<String, PO> mergedPO = oneCSV.getPODataMap();
			ArrayList<PO> poList = new ArrayList<>(mergedPO.values());
			makePotFile(poList, true, oneCSV.getZanataFileName(), oneCSV.getType(), "trs", "ja-JP", "po");
		}

	}

	private void CustomPOmodify(CategoryCSV targetCSV){

		HashMap<String, PO> targetPO = targetCSV.getPODataMap();

		for(PO po : targetPO.values()){
			if(po.getSource().equals(po.getTarget())){
				po.setTarget("");
			}
			po.setTarget(po.getTarget().replace("\"\"", "\"") );
			po.setSource(po.getSource().replace("\"\"", "\"") );
		}

		if("book".equals(targetCSV.getType())){
			for(PO po : targetPO.values()){
				if(po.getSource().equals(po.getTarget())){
					po.setTarget("");
				}
			}
		} else if ("skill".equals(targetCSV.getType())){
			for(PO po : targetPO.values()){
				if(po.getId1() == 198758357){
					po.setTarget(po.getSource());
				}
			}
		}


	}



	ArrayList<PO> reOrderAsMatchFirst(ArrayList<PO> poArrayList){
		poArrayList.sort(null);
		ArrayList<PO> Match = new ArrayList<>();
		ArrayList<PO> NonMatch = new ArrayList<>();
		ArrayList<PO> Reordered = new ArrayList<>();
		if(poArrayList.size() < 1) {
            return Reordered;
        }
        PO checkPO = poArrayList.get(0);

		boolean isChecked = false;
		for(PO TargetPo : poArrayList){
			String checkidx = Integer.toString(checkPO.getId2()) + checkPO.getId3();
			String targetidx = Integer.toString(TargetPo.getId2()) + TargetPo.getId3();
			if(checkidx.equals(targetidx)){
				Match.add(TargetPo);
				isChecked = true;
			}else{
				if(isChecked){
					Match.add(TargetPo);
				}else {
					NonMatch.add(TargetPo);
				}
				isChecked = false;
			}
			checkPO = TargetPo;
		}
		Reordered.addAll(Match);
		Reordered.addAll(NonMatch);
		//Reordered.remove(0);
		return Reordered;
	}

	void makePotFile(ArrayList<PO> origin, boolean outputTargetData , String fileName, String type, String folder, String language, String fileExtension) {
		HashMap<String, StringBuilder> builderMap = new HashMap<>();
		ArrayList<PO> sort =  reOrderAsMatchFirst(origin);
		int splitLimit = 0;
		if("item".equals(type)){
			splitLimit = 5000;
		} else if ("skill".equals(type)){
			splitLimit = 10000;
		} else if ("story".equals(type)){
			splitLimit = 6000;
		} else if ("book".equals(type)){
			splitLimit = 500;
		} else if ("system".equals(type)){
			splitLimit = 4000;
		}
		int fileCount = 0;
		int appendCount = 0;
		String splitFile = fileName;
		StringBuilder sb = new StringBuilder();

		for (PO p : sort) {
			sb = builderMap.get(splitFile);
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
								"\"Language: "+language+"\\n\"\n" +
								"\"X-Generator: Zanata 4.2.4\\n\"\n" +
								"\"Plural-Forms: nplurals=1; plural=0\\n\""
				);
				builderMap.put(splitFile, sb);
			}
			if(appendCount > splitLimit) {
				fileCount++;
				splitFile = fileName + fileCount;
				appendCount = 0;
			}
			if (outputTargetData) {
				sb.append(p.toTranslatedPO());
			} else {
				sb.append(p.toPOT());
			}
			appendCount++;
		}

		for (StringBuilder Onesb : builderMap.values()) {
			Pattern p = Pattern.compile("\\\\(?!n)");
			Matcher m = p.matcher(Onesb);
			String x = m.replaceAll("\\\\$0");
			Onesb.delete(0, Onesb.length());
			Onesb.append(x);
		}

		try {
			for (Map.Entry<String, StringBuilder> entry : builderMap.entrySet()) {
				if("trs".equals(folder)) {
					FileUtils.writeStringToFile(new File(appWorkConfig.getBaseDirectory() + "/" + folder + "/" + type + "/" + language + "/" + entry.getKey() + "." + fileExtension), entry.getValue().toString(), AppConfig.CHARSET);
				}else {
					FileUtils.writeStringToFile(new File(appWorkConfig.getBaseDirectory() + "/" + folder + "/" + type + "/" + entry.getKey() + "." + fileExtension), entry.getValue().toString(), AppConfig.CHARSET);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	void makePotFile(ArrayList<PO> sort, boolean outputTargetData ){
		HashMap<String, StringBuilder> builderMap = new HashMap<>();
		String fileName;
		sort.sort(null);


		for (PO p : sort) {
			fileName = p.getFileName().getName();
			boolean isLargeFile = true;
			int fileCount = 0;
			StringBuilder sb = new StringBuilder();
			String splitFile = fileName;
			while(isLargeFile){
				sb = builderMap.get(splitFile);
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
					builderMap.put(splitFile, sb);
					break;
				} else if(sb.length() > 1024*1024){
					fileCount++;
					splitFile = fileName + fileCount;
				} else {
					break;
				}
			}
			if(outputTargetData){
				sb.append(p.toTranslatedPO());
			}else {
				sb.append(p.toPO());
			}
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

	void makeCSVs() {

		Collection<File> fileList = FileUtils.listFiles(appWorkConfig.getPODirectory(), new String[]{"po2"}, false);
		ArrayList<PO> sourceList = Utils.getMergedPO(fileList);
		ToCSVConfig csvConfig = new ToCSVConfig().setWriteSource(false);

		Utils.makeCSVwithLog(new File(appWorkConfig.getBaseDirectory() + "/kr_" + appWorkConfig.getTodayWithYear() + ".po2.csv"), csvConfig, sourceList);
		Utils.makeCSVwithLog(new File(appWorkConfig.getBaseDirectory() + "/kr_beta_" + appWorkConfig.getTodayWithYear() + ".po2.csv"), csvConfig.setBeta(true), sourceList);
		Utils.makeCSVwithLog(new File(appWorkConfig.getBaseDirectory() + "/tr_" + appWorkConfig.getTodayWithYear() + ".po2.csv"), csvConfig.setWriteFileName(true).setBeta(false), sourceList);

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

	void makeNewLang(){
		CategoryGenerator CG = new CategoryGenerator(appWorkConfig);
		System.out.println("select eso client csv file");
		HashMap<String, PO> originCSVMap = CG.GetSelectedCSVMap();
		System.out.println("select csv file to merge");
		HashMap<String, PO> zanataCSVMap = CG.GetSelectedCSVMap();


	}

    void makeLang() {

		// EsoExtractData.exe depot/eso.mnf export -a 0
		// EsoExtractData.exe -l en_0124.lang -p

		LinkedList<File> fileLinkedList = new LinkedList<>();
		ArrayList<PO> sourceList = new ArrayList<>();
		HashMap<String, PO> ko;
		ArrayList<PO> originList;
		HashMap<String, PO> zanataPO;

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
			fileLinkedList.add(jFileChooser.getSelectedFile());
			if (fileLinkedList.size() == 2) break;
		}
		if (fileLinkedList.size() != 2) return;


		SourceToMapConfig sourceToMapConfig = new SourceToMapConfig().setPattern(AppConfig.CSVPattern);
		//ko = Utils.sourceToMap(sourceToMapConfig.setFile(fileLinkedList.get(0)));



		//ko.putAll(Utils.sourceToMap(sourceToMapConfig.setFile(fileLinkedList.get(1))));
		//ko.get("242841733-0-54340").setTarget(Utils.KOToCN("매지카 물약"));

		/*
		HashMap<FileNames, HashMap<String, ArrayList<PO>>> motherMap = new HashMap<>();
		for (PO p : ko.values()) {
			HashMap<String, ArrayList<PO>> childMap;
			ArrayList<PO> childList;

			if (motherMap.containsKey(p.getFileName())) childMap = motherMap.get(p.getFileName());
			else motherMap.put(p.getFileName(), childMap = new HashMap<>());

			if (childMap.containsKey(p.getSource())) childList = childMap.get(p.getSource());
			else childMap.put(p.getSource(), childList = new ArrayList<>());

			childList.add(p);
		}

		for (HashMap<String, ArrayList<PO>> childMap : motherMap.values()) {
			for (ArrayList<PO> childList : childMap.values()) {
				PO p = null;
				for (PO x : childList) if (!x.getSource().equals(x.getTarget())) p = x;
				if (p != null)
					for (PO x : childList) if (x.getSource().equals(x.getTarget())) x.setTarget(p.getTarget());
			}
		}


		for (Map<String, ArrayList<PO>> childMap : motherMap.values())
			for (List childList : childMap.values()) sourceList.addAll(childList);
		sourceList.sort(null);
*/

		originList = Utils.sourceToArray(sourceToMapConfig.setFile(fileLinkedList.get(0)));
		zanataPO = Utils.sourceToMap(sourceToMapConfig.setFile(fileLinkedList.get(1)));
		zanataPO.get("242841733-0-54340").setTarget(Utils.KOToCN("매지카 물약"));

		zanataPO.remove("41714900-0-307");
		zanataPO.remove("41714900-0-337");
		zanataPO.remove("41714900-0-339");
		zanataPO.remove("41714900-0-340");
		zanataPO.remove("41714900-0-342");
		zanataPO.remove("41714900-0-343");
		zanataPO.remove("41714900-0-345");
		zanataPO.remove("41714900-0-346");
		zanataPO.remove("41714900-0-348");
		zanataPO.remove("41714900-0-349");
		zanataPO.remove("41714900-0-351");
		zanataPO.remove("41714900-0-352");
		zanataPO.remove("41714900-0-354");
		zanataPO.remove("41714900-0-355");
		zanataPO.remove("41714900-0-357");
		zanataPO.remove("41714900-0-358");
		zanataPO.remove("41714900-0-360");
		zanataPO.remove("41714900-0-361");
		zanataPO.remove("41714900-0-363");
		zanataPO.remove("41714900-0-364");




		for(PO mergedPO : originList){
			PO target = zanataPO.get(mergedPO.getId());
			if(target != null){
				mergedPO.setSource(target.getSource());
				mergedPO.setTarget(target.getTarget());
			}
		}



		StringBuilder sb = new StringBuilder("\"Location\",\"Source\",\"Target\"\n");
		ToCSVConfig toCSVConfig = new ToCSVConfig();
		toCSVConfig.setWriteSource(true);
		for (PO p : originList) {
			sb.append(p.toCSV(toCSVConfig));
		}

		if (fileLinkedList.getLast().getName().contains(".po.")) return;

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

    }

	void translateGoogle() {
		this.PC.setAppWorkConfig(this.appWorkConfig);
		//this.PC.translateGoogle();
		//this.PC.filterNewPO();
		this.PC.setFuzzyNbyG();
	}

}