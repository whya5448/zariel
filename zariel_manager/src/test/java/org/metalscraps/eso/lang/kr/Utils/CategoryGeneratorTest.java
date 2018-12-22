package org.metalscraps.eso.lang.kr.Utils;


import org.junit.Test;
import org.metalscraps.eso.lang.kr.AppWorkConfig;
import org.metalscraps.eso.lang.kr.bean.CategoryCSV;
import org.metalscraps.eso.lang.kr.bean.PO;

import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

public class CategoryGeneratorTest {

    @Test
    public void genMainCategory() {
        AppWorkConfig appWorkConfig = new AppWorkConfig();
        JFileChooser jFileChooser = new JFileChooser();
        File workDir = new File(jFileChooser.getCurrentDirectory().getAbsolutePath()+"/Elder Scrolls Online/EsoKR");
        jFileChooser.setCurrentDirectory(workDir);
        appWorkConfig.setBaseDirectory(workDir);
        appWorkConfig.setZanataCategoryConfigDirectory(new File(appWorkConfig.getBaseDirectory()+"/ZanataCategory"));

        CategoryGenerator CG = new CategoryGenerator(appWorkConfig);
        CG.GenMainCategory();
        HashMap<String, CategoryCSV> testMap = CG.getCategoryMap();
        System.out.println("========== Category Map info ===========");
        for(String index : testMap.keySet()){
            CategoryCSV item = testMap.get(index);
            System.out.println("index ["+index+"] filename ["+item.getZanataFileName()+"]");
        }
        System.out.println("========== Category Map info ===========");

        CategoryCSV skillCsv = null;

        System.out.println("========== Category Set info ===========");
        HashSet<CategoryCSV> testSet = CG.getCategorizedCSV();
        int totalPoCount = 0;
        for(CategoryCSV csv : testSet){
            printCategory(csv);
            if("skill".equals(csv.getZanataFileName())){
                skillCsv = csv;
            }
            totalPoCount = totalPoCount + csv.getPODataMap().size();
        }
        System.out.println("========== Category Set info. total po count ["+totalPoCount+"] ===========");


        //CG.GenSubCategory(skillCsv);

    }

    public void printCategory(CategoryCSV item){
        System.out.println("Catecory file name ["+item.getZanataFileName()+"] type ["+item.getType()+"] indexLinkCount ["+item.getLinkCount() +
                "] index ["+item.getPoIndexList().size()+"] po count [" +item.getPODataMap().size()+ "]" );
    }

    @Test
    public void genSkillCategory() {

    }

    @Test
    public void getSelectedCSVMap() {
        AppWorkConfig appWorkConfig = new AppWorkConfig();
        JFileChooser jFileChooser = new JFileChooser();
        File workDir = new File(jFileChooser.getCurrentDirectory().getAbsolutePath()+"/Elder Scrolls Online/EsoKR");
        jFileChooser.setCurrentDirectory(workDir);
        appWorkConfig.setBaseDirectory(workDir);
        appWorkConfig.setZanataCategoryConfigDirectory(new File(appWorkConfig.getBaseDirectory()+"/ZanataCategory"));

        CategoryGenerator CG = new CategoryGenerator(appWorkConfig);
        HashMap<String, PO> CSVMap = CG.GetSelectedCSVMap();

        for(String key : CSVMap.keySet()) {
            PO po = CSVMap.get(key);
            System.out.println("key [" + key+"] , filename ["+po.getFileName()+"] source ["+po.getSource()+"] target ["+po.getTarget()+"]");
        }
    }
}