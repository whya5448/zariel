package org.metalscraps.eso.lang.kr.Utils;

import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Date;



import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class WebCrawler {



    public static String getCurrentData(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        return sdf.format(new Date());

    }



    public static void main(String[] args) throws ClientProtocolException, IOException {



        // 1. 가져오기전 시간 찍기
        System.out.println(" Start Date : " + getCurrentData());




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




        System.out.println(" End Date : " + getCurrentData());

    }

}


