package org.metalscraps.eso.lang.kr.Utils;

import org.junit.Test;
import org.metalscraps.eso.lang.kr.bean.CategoryCSV;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class WebCrawlerTest {

    @Test
    public void WebCrawlerTest() {
        boolean skillret;
        ArrayList<CategoryCSV> SkillCSV = new ArrayList<>();

        WebCrawler wc = new WebCrawler();
        skillret = wc.GetUESPChampionSkill(SkillCSV);
        skillret = wc.GetUESPSkillTree(SkillCSV);

        if(skillret){
            System.out.println("SkillCSV Size : "+SkillCSV.size());
            for(CategoryCSV oneCSV : SkillCSV){
                System.out.println("=========================================");
                System.out.println("Category ["+oneCSV.getZanataFileName()+"] index count ["+oneCSV.getPoIndexList().size()+"]");
                /*
                for(String index: oneCSV.getPoIndexList() ){
                    System.out.println(index);
                }
                */
            }
        }


    }

}