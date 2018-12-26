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

    public ArrayList<WebData> GetUSEPItemWebPage(WebData PageData) throws IOException {
        String url;
        url = "https://esoitem.uesp.net/viewMinedItems.php";
        System.out.println(url);
        ArrayList<WebData> WebList = getItemWebData(url, "");
        return WebList;
    }

    public ArrayList<WebData> getItemWebData(String rootURL, String rootName){
        ArrayList<WebData> WebList = new ArrayList<>();
        Document HTMLdoc = null;
        try {
            HTMLdoc = Jsoup.connect(rootURL).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(HTMLdoc);
        Element Tag = HTMLdoc.select("ol").first();
        String TagId = Tag.attr("id");
        Elements PageElementlist = Tag.getElementsByTag("a");

        if("esovmi_list".equals(TagId)){
            for(Element MainElement : PageElementlist){
                String linkhref =  MainElement.attr("href");
                String name = rootName + MainElement.text();
                System.out.println("name ["+name+"]");
                name = name.substring(0, name.indexOf("("));
                WebList.addAll( getItemWebData(rootURL+linkhref, name) );
            }
        } else if ("esovmi_itemlist".equals(TagId)){
            WebData webData = new WebData();
            webData.setItemFileName(rootName.strip());
            webData.setItemURL(rootURL);
            WebList.add(webData);
        }

        return WebList;
    }


    public boolean GetUSEPCpWebPage(WebData PageData, String PageName) throws IOException {
        String url;
        url = "https://esoitem.uesp.net/viewlog.php?record=" + PageName;
        System.out.println(url);
        Document HTMLdoc = Jsoup.connect(url).get();
        Element table = HTMLdoc.select("table").get(0);
        PageData.addWebTable(table);
        return PageData.getWebTables() != null;
    }

    public boolean GetUSEPSkillWebPage(WebData PageData, String PageName) throws IOException {
        String url = "";
        Document HTMLdoc = null;
        Element table = null;


        int pageCount = 1;
        while(true){
            url = "https://esoitem.uesp.net/viewlog.php?start=" + pageCount * 300 + "&record=" + PageName;
            System.out.println(url);
            HTMLdoc = Jsoup.connect(url).get();
            table = HTMLdoc.select("table").get(0);
            Elements skills = table.select("tr");
            if(skills.size() <= 1){
                break;
            }
            PageData.addWebTable(table);
            pageCount++;
        }
        return PageData.getWebTables() != null;
    }

    public boolean ParseUSEPItemPage(ArrayList<WebData> ItemWebList, ArrayList<CategoryCSV> ItemCSV){
        for(WebData oneWeb : ItemWebList){
            System.out.println("name ["+oneWeb.getItemFileName()+"] url ["+oneWeb.getItemURL()+"]");
            CategoryCSV oneCategory = new CategoryCSV();
            oneCategory.setZanataFileName(oneWeb.getItemFileName());

            Document HTMLdoc = null;
            try {
                HTMLdoc = Jsoup.connect(oneWeb.getItemURL()).get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Element Tag = HTMLdoc.select("ol").first();
            Elements PageElementlist = Tag.getElementsByTag("a");
            for(Element MainElement : PageElementlist){
                String linkhref =  MainElement.attr("href");
                String ItemIndex = linkhref.substring(linkhref.indexOf("=")+1, linkhref.length());
                //System.out.println("ItemIndex ["+ItemIndex+"]");
                oneCategory.addPoIndex("242841733-0-"+ItemIndex);
                oneCategory.addPoIndex("228378404-0-"+ItemIndex);
            }

            ItemCSV.add(oneCategory);
        }
        return ItemCSV.size() > 0;
    }


    public boolean GetUESPItemCategory(ArrayList<CategoryCSV> ItemCSV){
        boolean ret = false;
        ArrayList<WebData> WebList;
        try {
            WebData USEPItemData = new WebData();
            WebList = GetUSEPItemWebPage(USEPItemData);
            System.out.println("web size : "+WebList.size());
            if(WebList.size() > 0) {
                ret = ParseUSEPItemPage(WebList, ItemCSV);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }


    public boolean GetUESPSkillTree(ArrayList<CategoryCSV> SkillCSV){
        boolean ret = false;
        try {
            WebData USEPSkillData = new WebData();
            ret = GetUSEPSkillWebPage(USEPSkillData, "skillTree");
            if(ret) {
                ret = ParseUSEPSkillTable(USEPSkillData, SkillCSV);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(CategoryCSV oneCSV : SkillCSV){
            String originFilename = oneCSV.getZanataFileName();
            String convFilename = originFilename.replace("::", "-");
            oneCSV.setZanataFileName(convFilename);
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
                    if(Category.length() >0 ) {
                        sr = SkillCategory.add(Category);
                    }else{
                        continue;
                    }
                    if(sr){
                        if(CCSV!=null) {
                            //System.out.println("prev Set inserted : "+CCSV.getZanataFileName());
                            skillCSV.add(CCSV);
                        }
                        CCSV = new CategoryCSV();
                        CCSV.setZanataFileName(cols.get(4).text());
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

        return skillCSV.size() > 0;
    }

    public boolean GetUESPChampionSkill(ArrayList<CategoryCSV> SkillCSV){
        boolean ret = false;
        try {
            WebData USEPSkillData = new WebData();
            ret = GetUSEPCpWebPage(USEPSkillData, "cpSkills");
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
        CCSV.setZanataFileName("Champion::Champion Point");
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

        return skillCSV.size() > 0;
    }


}


