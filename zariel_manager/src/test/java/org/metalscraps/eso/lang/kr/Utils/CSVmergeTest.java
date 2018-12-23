package org.metalscraps.eso.lang.kr.Utils;

import org.junit.BeforeClass;
import org.junit.Test;
import org.metalscraps.eso.lang.kr.AppWorkConfig;
import org.metalscraps.eso.lang.kr.bean.CategoryCSV;
import org.metalscraps.eso.lang.kr.bean.PO;

import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.*;

public class CSVmergeTest {

    @Test
    public void genMergedCSV() {
        AppWorkConfig appWorkConfig = new AppWorkConfig();
        JFileChooser jFileChooser = new JFileChooser();
        File workDir = new File(jFileChooser.getCurrentDirectory().getAbsolutePath()+"/Elder Scrolls Online/EsoKR");
        jFileChooser.setCurrentDirectory(workDir);
        appWorkConfig.setBaseDirectory(workDir);
        appWorkConfig.setZanataCategoryConfigDirectory(new File(appWorkConfig.getBaseDirectory()+"/ZanataCategory"));

        CategoryGenerator originCG = new CategoryGenerator(appWorkConfig);
        originCG.GenCategoryConfigMap(appWorkConfig.getZanataCategoryConfigDirectory().toString()+"\\IndexMatch.txt");

        CategoryGenerator zanataCG = new CategoryGenerator(appWorkConfig);
        originCG.GenCategoryConfigMap(appWorkConfig.getZanataCategoryConfigDirectory().toString()+"\\IndexMatch.txt");

        //select origin csv
        originCG.GenMainCategory();
        HashSet<CategoryCSV> origin = originCG.getCategorizedCSV();


        //select zanata csv
        zanataCG.GenMainCategory();
        HashSet<CategoryCSV> zanata = zanataCG.getCategorizedCSV();

        CSVmerge merge = new CSVmerge();
        merge.GenMergedCSV(origin, zanata);

        HashMap<String, CategoryCSV> mergedMap = merge.getMergedCSV();

        for(String name : mergedMap.keySet()){
            CategoryCSV oneCSV = mergedMap.get(name);
            System.out.println(" Name ["+name+"] changed count ["+oneCSV.getPODataMap().size()+"]");
            if("set".equals(name)) {
                for (PO po : oneCSV.getPODataMap().values()) {
                    System.out.println("changed idx ["+po.getId()+"] text ["+po.getSource()+"]");
                }
            }
        }

    }
}