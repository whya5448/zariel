package org.metalscraps.eso.lang.kr;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.metalscraps.eso.lang.kr.Utils.CategoryGenerator;
import org.metalscraps.eso.lang.kr.bean.CategoryCSV;
import org.metalscraps.eso.lang.kr.config.CSVmerge;
import org.metalscraps.eso.lang.lib.bean.PO;
import org.metalscraps.eso.lang.lib.config.AppConfig;
import org.metalscraps.eso.lang.lib.config.AppWorkConfig;
import org.metalscraps.eso.lang.lib.config.SourceToMapConfig;
import org.metalscraps.eso.lang.lib.util.Utils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.*;

public class LangManagerTest {
    public static LangManager LMG;
    public static AppWorkConfig appWorkConfig;
    public static CategoryGenerator CG;
    @BeforeClass
    public static void setLang(){
        appWorkConfig = new AppWorkConfig();
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
        appWorkConfig.setZanataCategoryConfigDirectory(new File(appWorkConfig.getBaseDirectory()+"/ZanataCategory"));
        appWorkConfig.setPODirectory(new File(appWorkConfig.getBaseDirectory()+"/PO_"+appWorkConfig.getToday()));

        LMG = new LangManager(appWorkConfig);
        CG = new CategoryGenerator(appWorkConfig);

    }

    @Test
    public void localCSVcountTest() {
        HashMap<String, PO> CSVMap = CG.GetSelectedCSVMap();
        System.out.println("local csv count:"+CSVMap.size());

    }


    @Test
    public void genCatecoryCSV(){
        CategoryGenerator originCG = new CategoryGenerator(appWorkConfig);
        originCG.GenCategoryConfigMap(appWorkConfig.getZanataCategoryConfigDirectory().toString()+"\\IndexMatch.txt");
        originCG.GenCategory();
        HashSet<CategoryCSV> categorizedCSV = originCG.getCategorizedCSV();
        int total = 0;
        for(CategoryCSV oneCsv : categorizedCSV){
            total += oneCsv.getPODataMap().size();
        }
        System.out.println("categorized count :"+total);

        CSVmerge merge = new CSVmerge();
        HashMap<String, PO> targetCSV = new HashMap<>();
        Collection<File> fileList = FileUtils.listFiles(appWorkConfig.getPODirectory(), new String[]{"po"}, false);
        for (File file : fileList) {

            String fileName = FilenameUtils.getBaseName(file.getName());
            // pregame 쪽 데이터
            if (fileName.equals("00_EsoUI_Client") || fileName.equals("00_EsoUI_Pregame")) continue;

            targetCSV.putAll(Utils.sourceToMap(new SourceToMapConfig().setFile(file).setPattern(AppConfig.POPattern)));
            //System.out.println("zanata po parsed ["+file+"] ");
        }

        merge.MergeCSV(categorizedCSV, targetCSV, false);
        int mergedcount = 0;
        for(CategoryCSV oneCsv : categorizedCSV){
            mergedcount += oneCsv.getPODataMap().size();
        }
        System.out.println("merged count :"+mergedcount);



    }


}