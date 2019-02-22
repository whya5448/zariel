package org.metalscraps.eso.lang.kr;

import org.apache.commons.io.FilenameUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.metalscraps.eso.lang.lib.bean.PO;
import org.metalscraps.eso.lang.lib.config.AppConfig;
import org.metalscraps.eso.lang.lib.config.AppWorkConfig;
import org.metalscraps.eso.lang.lib.config.SourceToMapConfig;
import org.metalscraps.eso.lang.lib.util.Utils;
import org.metalscraps.eso.lang.tool.LangManager;
import org.metalscraps.eso.lang.tool.Utils.CategoryGenerator;
import org.metalscraps.eso.lang.tool.bean.CategoryCSV;
import org.metalscraps.eso.lang.tool.config.CSVmerge;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class LangManagerTest {
    public static LangManager LMG;
    public static AppWorkConfig appWorkConfig;
    public static CategoryGenerator CG;
    @BeforeClass
    public static void setLang(){
        appWorkConfig = new AppWorkConfig();
        JFileChooser jFileChooser = new JFileChooser();
        var workDir = Utils.getESODir().resolve("EsoKR");

        jFileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) { return f.isDirectory(); }
            @Override
            public String getDescription() { return "작업 폴더 설정"; }
        });
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jFileChooser.setMultiSelectionEnabled(false);
        jFileChooser.setCurrentDirectory(workDir.toFile());
        appWorkConfig.setBaseDirectoryToPath(workDir);
        appWorkConfig.setZanataCategoryConfigDirectoryToPath(appWorkConfig.getBaseDirectoryToPath().resolve("ZanataCategory"));
        appWorkConfig.setPODirectoryToPath(appWorkConfig.getBaseDirectoryToPath().resolve("PO_"+appWorkConfig.getToday()));

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
        originCG.GenCategoryConfigMap(appWorkConfig.getZanataCategoryConfigDirectoryToPath().resolve("IndexMatch.txt").toString());
        originCG.GenCategory();
        HashSet<CategoryCSV> categorizedCSV = originCG.getCategorizedCSV();
        int total = 0;
        for(CategoryCSV oneCsv : categorizedCSV){
            total += oneCsv.getPODataMap().size();
            if(oneCsv.getZanataFileName().contains("Dragonknight")){
                for(String key : oneCsv.getPODataMap().keySet()){
                    System.out.println("key [" +key +"] data ["+oneCsv.getPODataMap().get(key).getTarget());
                }
            }
        }
        System.out.println("categorized count :"+total);

        CSVmerge merge = new CSVmerge();
        HashMap<String, PO> targetCSV = new HashMap<>();
        Collection<Path> fileList = Utils.listFiles(appWorkConfig.getBaseDirectoryToPath(), "po");
        for (var file : fileList) {

            String fileName = FilenameUtils.getBaseName(file.getFileName().toString());
            // pregame 쪽 데이터
            if (fileName.equals("00_EsoUI_Client") || fileName.equals("00_EsoUI_Pregame")) continue;

            targetCSV.putAll(Utils.sourceToMap(new SourceToMapConfig().setPath(file).setPattern(AppConfig.INSTANCE.getPOPattern())));
            //System.out.println("zanata po parsed ["+file+"] ");
        }

        merge.MergeCSV(categorizedCSV, targetCSV, false);
        int mergedcount = 0;
        for(CategoryCSV oneCsv : categorizedCSV){
            mergedcount += oneCsv.getPODataMap().size();

            if(oneCsv.getZanataFileName().contains("Dragonknight") && oneCsv.getZanataFileName().contains("Flame") ){
                for(String key : oneCsv.getPODataMap().keySet()){
                    System.out.println("key [" +key +"] data src["+oneCsv.getPODataMap().get(key).getSource()+"] trg["+oneCsv.getPODataMap().get(key).getTarget());
                }
            }
        }
        System.out.println("merged count :"+mergedcount);



    }


}