package org.metalscraps.eso.lang.kr.Utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.metalscraps.eso.lang.kr.AppWorkConfig;
import org.metalscraps.eso.lang.kr.bean.CategoryCSV;
import org.metalscraps.eso.lang.kr.bean.PO;
import org.metalscraps.eso.lang.kr.config.AppConfig;
import org.metalscraps.eso.lang.kr.config.FileNames;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

@AllArgsConstructor
public class CategoryGenerator {
    private PoConverter PC = new PoConverter();
    private final AppWorkConfig appWorkConfig;
    private final ArrayList<PO> sourceList = new ArrayList<>();

    @Getter(AccessLevel.PUBLIC)
    private HashMap<String, CategoryCSV> CategoryMap = new HashMap<>();

    @Getter(AccessLevel.PUBLIC)
    private HashSet<CategoryCSV> CategorizedCSV = new HashSet<>();

    public CategoryGenerator(AppWorkConfig appWorkConfig) {
        this.appWorkConfig = appWorkConfig;
    }

    public void GenCategory(){
        if(CategoryMap .size() ==0){
            GenCategoryConfigMap(appWorkConfig.getZanataCategoryConfigDirectory().toString()+"\\IndexMatch.txt");
        }

        System.out.println("Select Csv file for generate category.");
        HashMap<String, PO> CSVMap = GetSelectedCSVMap();
        GenSubCategory(CSVMap);
        GenMainCategory(CSVMap);
    }

    public void GenMainCategory(HashMap<String, PO> CSVMap){

        ParseMainCategorizedCSV(CSVMap);
    }

    public void GenCategoryConfigMap(String indexFileName){
        File file = new File(indexFileName);
        String fileString = "";
        String localIndex = "";

        try {
            fileString =  FileUtils.readFileToString(file, AppConfig.CHARSET);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Matcher matcher = AppConfig.CategoryConfig.matcher(fileString);
        while(matcher.find()){
            localIndex = matcher.group(17);
            String[] localIndexList  = localIndex.split(",");

            CategoryCSV categoryCSV = new CategoryCSV();
            categoryCSV.setZanataFileName(matcher.group(1));
            categoryCSV.setType(matcher.group(9));
            categoryCSV.setLinkCount(Integer.parseInt(matcher.group(13)));
            for(String index : localIndexList) {
                categoryCSV.addPoIndex(index);
                CategoryMap.put(index, categoryCSV);
            }
        }

        CategoryCSV UndefinedCategoryCSV = new CategoryCSV();
        UndefinedCategoryCSV.setZanataFileName("Undefined");
        UndefinedCategoryCSV.setType("system");
        UndefinedCategoryCSV.setLinkCount(0);
        CategoryMap.put("Undefined", UndefinedCategoryCSV);
    }


    public HashMap<String, PO> GetSelectedCSVMap() {
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

        if (fileLinkedList.size() == 0){
            System.out.println("no file selected!");
            return map;
        }

        SourceToMapConfig sourceToMapConfig = new SourceToMapConfig().setPattern(AppConfig.CSVPattern);
        for (File file : fileLinkedList) {
            System.out.println(file);
            map.putAll(Utils.sourceToMap(sourceToMapConfig.setFile(file)));
        }
        return map;
    }

    public void ParseMainCategorizedCSV(HashMap<String, PO> CSVMap){
        for(PO onePO : CSVMap.values()){
            CategoryCSV categoryCSV = this.getCategoryMap().get(onePO.getId1().toString());
            if(categoryCSV == null){
                categoryCSV = this.getCategoryMap().get("Undefined");
            }
            onePO.setFileName(FileNames.fromString(categoryCSV.getZanataFileName()));
            categoryCSV.putPoData(onePO.getId(), onePO);
        }

        for(CategoryCSV categoryCSV : this.getCategoryMap().values()){
            this.CategorizedCSV.add(categoryCSV);
        }
    }

    public void GenSubCategory(HashMap<String, PO> MainPoMap){
        ArrayList<CategoryCSV> subCategorizedCsvList = new ArrayList<>();

        boolean genRet = GenSkillCategory(subCategorizedCsvList);

        if(genRet){
            System.out.println("SkillCSV Size : "+subCategorizedCsvList.size());
            for(CategoryCSV oneCSV : subCategorizedCsvList){
                for(String index: oneCSV.getPoIndexList() ){
                    PO podata = MainPoMap.get(index);
                    if(podata != null) {
                        podata.setFileName(FileNames.fromString(oneCSV.getZanataFileName()));
                        oneCSV.putPoData(index, podata);
                        MainPoMap.remove(index);
                    }
                }
            }
        }

        for(CategoryCSV oneCSV : subCategorizedCsvList) {
            oneCSV.setType("system");
            CategorizedCSV.add(oneCSV);
        }
    }

    private boolean GenSkillCategory(ArrayList<CategoryCSV> SkillCSV){
        boolean wbCrawlRet;
        WebCrawler wc = new WebCrawler();
        try {
            if(!wc.GetUESPChampionSkill(SkillCSV)){
                throw new Exception("ChampionSkill gen fail");
            }
            if(!wc.GetUESPSkillTree(SkillCSV)){
                throw new Exception("Skill tree gen fail");
            }
            wbCrawlRet = true;
        }catch(Exception ex){
            wbCrawlRet = false;
        }
        return wbCrawlRet;
    }



}


