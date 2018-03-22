package org.metalscraps.eso.lang.kr.Utils;

import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Date;



import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.metalscraps.eso.lang.kr.bean.WebData;

//get html table data from url.
public class WebCrawler {

    public static String getCurrentData(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        return sdf.format(new Date());

    }

    public boolean GetUESPSkillTree(WebData outputData){
        boolean ret = false;
        try {
            Document HTMLdoc = Jsoup.connect("https://esoitem.uesp.net/viewlog.php?record=skillTree").get();
            // https://esoitem.uesp.net/viewlog.php?start=300&record=skillTree
            Element table = HTMLdoc.select("table").get(0);
            outputData.addWebTable(table);

            String url;
            for(int pageCount = 1; pageCount < 9 ; pageCount++) {
                url = "https://esoitem.uesp.net/viewlog.php?start="+pageCount*300+"&record=skillTree";
                System.out.println(url);
                HTMLdoc = Jsoup.connect(url).get();
                // https://esoitem.uesp.net/viewlog.php?start=300&record=skillTree
                table = HTMLdoc.select("table").get(0);
                outputData.addWebTable(table);
            }
            ret = true;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("size of WebTable:"+outputData.getWebTables().size());
        }

        return ret;
    }

    public static void main(String[] args) throws ClientProtocolException, IOException {

        WebCrawler wc = new WebCrawler();

        // 1. 가져오기전 시간 찍기
        System.out.println(" Start Date : " + getCurrentData());
        WebData data = new WebData();
        wc.GetUESPSkillTree(data);
        /*
        Document doc2 = Jsoup.connect("https://esoitem.uesp.net/viewlog.php?sort=skillTypeName&sortorder=d&record=skillTree").get();
        //System.out.println(doc2.html());
        System.out.println("======================================");
        Element table = doc2.select("table").get(0);
        System.out.println(table.html());
        System.out.println("======================================");

        Elements skills = table.select("tr");

        for(Element skill : skills){
            System.out.println("--------------------------------------");
            //System.out.println("one row" + skill.html());
            Elements cols = skill.select("td");
            if(cols.size() > 0) {
                System.out.println("skill id :" + cols.get(2).text() + " category :" + cols.get(4).text() + " skill name :" + cols.get(6).text() );
            }
        }
        */

        System.out.println(" End Date : " + getCurrentData());

    }

}


