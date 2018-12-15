package org.metalscraps.eso.lang.kr.Utils;

import lombok.AllArgsConstructor;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
public class CategoryGenerator {
    private PoConverter PC = new PoConverter();
    private final AppWorkConfig appWorkConfig;
    private final ArrayList<PO> sourceList = new ArrayList<>();
    private HashMap<String, CategoryCSV> CategoryMap = null;

    public CategoryGenerator(AppWorkConfig appWorkConfig) {
        this.appWorkConfig = appWorkConfig;
    }


    public void GenCategory(){
        if(CategoryMap == null){
            CategoryMap = new HashMap<>();
        }
        GenCategoryFromFile();


    }

    public void GenCategoryFromFile(){
        String indexFileName = appWorkConfig.getZanataCategoryConfigDirectory().toString()+"\\IndexMatch.txt";
        System.out.println("config file name : "+indexFileName);
        File file = new File(indexFileName);
        String fileString = "";

        try {
            fileString =  FileUtils.readFileToString(file, AppConfig.CHARSET);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Matcher matcher = AppConfig.CategoryConfig.matcher(fileString);
        while(matcher.find()){
            System.out.println("---------------------------");
            System.out.println("FileName:"+matcher.group(1) + " isDuplicate:"+ matcher.group(5) + " type:"+ matcher.group(9) + " indexLinkCount:" + matcher.group(13) + " index:" +  matcher.group(17));
        }

    }

    public void GenCategoryFromUESP(){

    }

    public void GenSkillCategory(){
        System.out.println("Select Csv file for generate category.");
        HashMap<String, PO> CSVMap = GetSelectedCSVMap();
        HashMap<String, String> PoMap = new HashMap<>();

        for(PO po : CSVMap.values()) {
            PoMap.put(po.getId(), po.getSource());
        }


        boolean skillret;
        ArrayList<CategoryCSV> SkillCSV = new ArrayList<>();

        WebCrawler wc = new WebCrawler();
        skillret = wc.GetUESPChampionSkill(SkillCSV);
        skillret = wc.GetUESPSkillTree(SkillCSV);

        if(skillret){
            System.out.println("SkillCSV Size : "+SkillCSV.size());
            for(CategoryCSV oneCSV : SkillCSV){
                System.out.println("=========================================");
                System.out.println("Category : "+oneCSV.getZanataFileName());
                for(String index: oneCSV.getPoIndexList() ){
                    System.out.println(index+" "+PoMap.get(index));
                }
            }
        }

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

}


