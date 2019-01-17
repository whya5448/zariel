package org.metalscraps.eso.lang.kr.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.metalscraps.eso.lang.kr.bean.CategoryCSV;
import org.metalscraps.eso.lang.kr.bean.WebData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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


    public void GetUSEPBookWebPage(WebData PageData) throws IOException {
        GetUESPShalidorLibraryWebPage(PageData);
        GetUESPEideticMemoryWebPage(PageData);
    }

    public void GetUESPShalidorLibraryWebPage(WebData PageData){
        String url = "https://en.uesp.net/wiki/Online:Shalidor%27s_Library";
        System.out.println(url);
        Document HTMLdoc = null;
        try {
            HTMLdoc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> CategoryStringList = new ArrayList<>();
        Element Category = HTMLdoc.select("ul").first();
        Elements Categorylist = Category.getElementsByTag("span");
        for(Element oneCategory : Categorylist){
            if("tocnumber".equals(oneCategory.attr("class"))) continue;
            String categoryString = oneCategory.text();
            categoryString = categoryString.replace("%27", "'");
            CategoryStringList.add(categoryString);
        }

        Elements BookTbody = HTMLdoc.select("tbody");
        int cnt = 0;
        for(Element oneBookTable: BookTbody) {
            ArrayList<String> titleList = new ArrayList<>();
            Elements booktr = oneBookTable.getElementsByTag("tr");
            for (Element onetr : booktr) {
                Element td = onetr.getElementsByTag("a").first();
                if (td != null) {
                    String title = td.attr("title");
                    title = title.substring(title.indexOf(":") + 1);
                    if(title.contains("(")){
                        title = title.substring(0,title.indexOf("("));
                    }
                    titleList.add(title);
                }
            }
            PageData.putBookMap(CategoryStringList.get(cnt), titleList);
            cnt++;
        }


    }

    public void GetUESPEideticMemoryWebPage(WebData PageData){
        String baseUrl = "https://en.uesp.net";
        String url;
        url = baseUrl+"/wiki/Online:Eidetic_Memory" ;
        System.out.println(url);
        Document HTMLdoc = null;
        try {
            HTMLdoc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Element Tag = HTMLdoc.select("tbody").first();
        //System.out.println(Tag);
        Elements PageElementlist = Tag.getElementsByTag("a");
        for(Element SubCategoryElement : PageElementlist){
            String linkhref =  SubCategoryElement.attr("href");
            GetUESPEideticMemorySubWebPage(PageData, baseUrl ,linkhref);
        }
    }

    public void GetUESPEideticMemorySubWebPage(WebData PageData, String baseUrl, String subCategory){
        System.out.println(baseUrl+subCategory);
        Document HTMLdoc = null;
        try {
            HTMLdoc = Jsoup.connect(baseUrl+subCategory).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> bookList = new ArrayList<>();

        Element Tag = HTMLdoc.select("tbody").first();
        Elements PageElementlist = Tag.getElementsByTag("tr");
        for(Element SubCategoryElement : PageElementlist){
            Element td = SubCategoryElement.getElementsByTag("a").first();
            if(td != null){
                String title =  td.attr("title");
                title = title.substring(title.indexOf(":")+1);
                bookList.add(title);
            }
        }

        String category = subCategory.substring(subCategory.indexOf("Online:") + 7);
        category = category.replace("%27", "'");
        PageData.putBookMap(category, bookList);
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
                String ItemIndex = linkhref.substring(linkhref.indexOf("=")+1);
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

        CategoryCSV categoryCSV = null;

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
                        if(categoryCSV!=null) {
                            //System.out.println("prev Set inserted : "+categoryCSV.getZanataFileName());
                            skillCSV.add(categoryCSV);
                        }
                        categoryCSV = new CategoryCSV();
                        categoryCSV.setZanataFileName(cols.get(4).text());
                    }
                    //skill name PO Index
                    categoryCSV.addPoIndex("198758357-0-"+cols.get(2).text());
                    //skill desc PO Index
                    categoryCSV.addPoIndex("132143172-0-"+cols.get(2).text());
                }
            }
        }
        //final skill set
        skillCSV.add(categoryCSV);

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

    public HashMap<String, ArrayList<String>> GetUESPBookMap(){
        WebData USEPBookData = new WebData();
        try {
            GetUSEPBookWebPage(USEPBookData);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return USEPBookData.getBookMap();
    }



    private boolean ParseUSEPChampionSkillTable(WebData USEPWebData, ArrayList<CategoryCSV> skillCSV) {
        CategoryCSV categoryCSV = null;
        categoryCSV = new CategoryCSV();
        categoryCSV.setZanataFileName("Champion::Champion Point");
        for(Element oneTable : USEPWebData.getWebTables()) {
            Elements skills = oneTable.select("tr");
            for(Element skill : skills){
                Elements cols = skill.select("td");
                if(cols.size() > 0) {
                    //skill name PO Index
                    categoryCSV.addPoIndex("198758357-0-"+cols.get(2).text());
                    //skill desc PO Index
                    categoryCSV.addPoIndex("132143172-0-"+cols.get(2).text());
                }
            }
        }
        skillCSV.add(categoryCSV);

        return skillCSV.size() > 0;
    }


}


