package org.metalscraps.eso.lang.kr.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.metalscraps.eso.lang.kr.bean.CategoryCSV;
import org.metalscraps.eso.lang.kr.bean.WebData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

//get html table data from url.
public class WebCrawler {

    public boolean GetUSEPWebPage(WebData PageData, String PageName, int PageLoop) throws IOException {
        String url;
        url = "https://esoitem.uesp.net/viewlog.php?record=" + PageName;
        System.out.println(url);
        Document HTMLdoc = Jsoup.connect(url).get();
        Element table = HTMLdoc.select("table").get(0);
        PageData.addWebTable(table);


        for (int pageCount = 1; pageCount < PageLoop; pageCount++) {
            url = "https://esoitem.uesp.net/viewlog.php?start=" + pageCount * 300 + "&record=" + PageName;
            System.out.println(url);
            HTMLdoc = Jsoup.connect(url).get();
            table = HTMLdoc.select("table").get(0);
            PageData.addWebTable(table);
        }
        if(PageData.getWebTables() == null){
            return false;
        }else {
            return true;
        }
    }


    public boolean GetUESPSkillTree(ArrayList<CategoryCSV> SkillCSV){
        boolean ret = false;
        try {
            WebData USEPSkillData = new WebData();
            ret = GetUSEPWebPage(USEPSkillData, "skillTree", 9);
            if(ret) {
                ret = ParseUSEPSkillTable(USEPSkillData, SkillCSV);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }


    private boolean ParseUSEPSkillTable(WebData USEPWebData, ArrayList<CategoryCSV> skillCSV) {
        Set<String> SkillCategory = new HashSet<>();
        CategoryCSV CCSV = null;

        boolean sr;
        String Category;
        for(Element oneTable : USEPWebData.getWebTables()) {
            Elements skills = oneTable.select("tr");
            for(Element skill : skills){
                //System.out.println("--------------------------------------");
                //System.out.println("one row" + skill.html());
                Elements cols = skill.select("td");
                if(cols.size() > 0) {
                    //System.out.println("skill id :" + cols.get(2).text() + " category :" + cols.get(4).text() + " skill name :" + cols.get(6).text() );
                    Category = cols.get(4).text();
                    if(Category.length() >0) {
                        sr = SkillCategory.add(Category);
                    }else{
                        continue;
                    }
                    if(sr){
                        if(CCSV!=null) {
                            //System.out.println("prev Set inserted : "+CCSV.getCategory());
                            skillCSV.add(CCSV);
                        }
                        CCSV = new CategoryCSV();
                        CCSV.setCategory(cols.get(4).text());
                    }
                    //skill name poindex
                    CCSV.addPoIndex("198758357-0-"+cols.get(2).text());
                    //skill desc poindex
                    CCSV.addPoIndex("132143172-0-"+cols.get(2).text());
                }
            }
        }
        //final skill set
        skillCSV.add(CCSV);

        if(skillCSV.size() > 0){
            return true;
        }else{
            return false;
        }
    }

    public boolean GetUESPChampionSkill(ArrayList<CategoryCSV> SkillCSV){
        boolean ret = false;
        try {
            WebData USEPSkillData = new WebData();
            ret = GetUSEPWebPage(USEPSkillData, "cpSkills", 0);
            if(ret) {
                ret = ParseUSEPChampionSkillTable(USEPSkillData, SkillCSV);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private boolean ParseUSEPChampionSkillTable(WebData USEPWebData, ArrayList<CategoryCSV> skillCSV) {
        CategoryCSV CCSV = null;
        CCSV = new CategoryCSV();
        CCSV.setCategory("Champion::Champion Point");
        for(Element oneTable : USEPWebData.getWebTables()) {
            Elements skills = oneTable.select("tr");
            for(Element skill : skills){
                Elements cols = skill.select("td");
                if(cols.size() > 0) {
                    //skill name poindex
                    CCSV.addPoIndex("198758357-0-"+cols.get(2).text());
                    //skill desc poindex
                    CCSV.addPoIndex("132143172-0-"+cols.get(2).text());
                }
            }
        }
        skillCSV.add(CCSV);

        if(skillCSV.size() > 0){
            return true;
        }else{
            return false;
        }
    }




    public static void main(String[] args) {
        boolean skillret;
        ArrayList<CategoryCSV> SkillCSV = new ArrayList<>();

        WebCrawler wc = new WebCrawler();
        skillret = wc.GetUESPChampionSkill(SkillCSV);
        skillret = wc.GetUESPSkillTree(SkillCSV);

        if(skillret){
            System.out.println("SkillCSV Size : "+SkillCSV.size());
            for(CategoryCSV oneCSV : SkillCSV){
                System.out.println("=========================================");
                System.out.println("Category : "+oneCSV.getCategory());
                for(String index: oneCSV.getPoIndexList() ){
                    System.out.println(index);
                }
            }
        }






    }

}


