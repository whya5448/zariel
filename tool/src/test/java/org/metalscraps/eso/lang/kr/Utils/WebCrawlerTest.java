package org.metalscraps.eso.lang.kr.Utils;

import org.junit.Test;
import org.metalscraps.eso.lang.kr.bean.CategoryCSV;
import org.metalscraps.eso.lang.kr.bean.WebData;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

public class WebCrawlerTest {

    @Test
    public void SkillWebCrawlerTest() {
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


    @Test
    public void ItemWebCrawlerTest() {
        boolean itemret;
        ArrayList<CategoryCSV> ItemCSV = new ArrayList<>();

        WebCrawler wc = new WebCrawler();
        itemret = wc.GetUESPItemCategory(ItemCSV);
        if(itemret){
            System.out.println("SkillCSV Size : "+ItemCSV.size());
            for(CategoryCSV oneCSV : ItemCSV){
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

    @Test
    public void BookWebCrawlerTest(){
        boolean bookret;
        ArrayList<CategoryCSV> BookCSV = new ArrayList<>();
        WebCrawler wc = new WebCrawler();
        HashMap<String, ArrayList<String>> bookMap = wc.GetUESPBookMap();
        for(String category : bookMap.keySet()){
            ArrayList<String> titles = bookMap.get(category);
            System.out.println("============");
            System.out.println("category ["+category+"]");
            System.out.println("------------");
            System.out.println(titles);
        }

    }

}