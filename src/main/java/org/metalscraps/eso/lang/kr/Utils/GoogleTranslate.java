package org.metalscraps.eso.lang.kr.Utils;

import org.json.JSONArray;
import org.metalscraps.eso.lang.kr.bean.PO;

import java.util.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class GoogleTranslate implements Runnable {

    private static final String[] TAG = {"<br>", "<p>"};
    private static final String[] SYMBOL = {"\r", "\n"};
    private ArrayList<PO> jobList = new ArrayList<>();
    private int jobIndex = 0;

    @Override
    public void run() {
        int localIndex = this.getIndex();
        PO localPO = this.getPO(localIndex);
        localPO.setTarget(this.Translate(localPO.getSource(), false));
        localPO.setFuzzy(true);
        this.setPO(localIndex, localPO);
    }


    void addJob(PO job) {
        //System.out.println("job add : "+ job.getTarget());
        this.jobList.add(job);
    }

    public void clearJob(){
        this.jobList.clear();
    }

    private synchronized int getIndex() {
        int ret = this.jobIndex;
        this.jobIndex++;
        return ret;
    }

    private synchronized PO getPO(int index){
        return this.jobList.get(index);
    }

    private synchronized void setPO(int index, PO po){
        this.jobList.set(index, po);
    }

    synchronized ArrayList<PO> getResult(){
        return this.jobList;
    }

    private String Translate(String origin, boolean addOriginText) {
        /*
        Translate translate = TranslateOptions.getDefaultInstance().getService();
        StringBuilder sb = new StringBuilder(StringEscapeUtils.escapeXml11(origin));
        Utils.replaceStringBuilder(sb, GoogleTranslate.SYMBOL, GoogleTranslate.TAG);
*/
        StringBuilder sb = new StringBuilder();
        String translated;
        String[] parsed = origin.split("\n\n");

        try {
            /*
            Translation translation =
                    translate.translate(
                            sb.toString(),
                            TranslateOption.sourceLanguage("en"),
                            TranslateOption.targetLanguage("ko"));
            sb = new StringBuilder(StringEscapeUtils.unescapeXml(translation.getTranslatedText()));
            Utils.replaceStringBuilder(sb, GoogleTranslate.TAG, GoogleTranslate.SYMBOL);
            */
            for(String oneitem : parsed) {
                translated = this.callUrlAndParseResult("en", "ko", oneitem);
                sb.append(translated);
                sb.append("\n\n");
            }
            sb.append("-G-");
            if(addOriginText) sb.append("(").append(origin).append(")");
        } catch (Exception ex) {
            System.out.println("Google Translate : Exception occur! exception msg [" + ex.getMessage() + "]");
            ex.printStackTrace();
        }
        String Translated =  sb.toString();
        String ret = this.ReplaceSpecialChar(Translated);
        System.out.println("result of "+origin+" : "+ret);
        return ret;
    }




    private String callUrlAndParseResult(String langFrom, String langTo, String word)
    {
        String ret = "";
        try {
            String url = "https://translate.googleapis.com/translate_a/single?" +
                    "client=gtx&" +
                    "sl=" + langFrom +
                    "&tl=" + langTo +
                    "&dt=t&q=" + URLEncoder.encode(word, "UTF-8");

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            ret = parseResult(response.toString());
        }catch (Exception ex){
            System.out.println("Google Translate : Exception occur! exception msg [" + ex.getMessage() + "]");
            ex.printStackTrace();
            ret = ret + "Translate fail(by Google translate)";
        } finally {
            return ret;
        }
    }

    private String parseResult(String inputJson) throws Exception
    {
        String ret ="";
        int idx = 0;
        JSONArray jsonArray = new JSONArray(inputJson);

        JSONArray jsonArray2 = (JSONArray) jsonArray.get(0);
        JSONArray oneData;

        while(!jsonArray2.isNull(idx)){
            oneData = (JSONArray) jsonArray2.get(idx);
            ret = ret+ oneData.get(0).toString();
            idx++;
        }

        return ret;
    }

    private String ReplaceSpecialChar(String origin){
        String ret = origin.replace("<< ", "<<");
        ret =  ret.replace(" >>", ">>");
        ret  = ret.replace(" : ", ":");
        ret  = ret.replace(" / ", "/");
        ret  = ret.replace(" | ", "|");
        ret  = ret.replace(" \\ ", "\\");
        ret  = ret.replace(" \\ n", "\\n");
        ret  = ret.replace(": ", ":");
        ret  = ret.replace("/ ", "/");
        ret  = ret.replace("| ", "|");
        ret  = ret.replace("\\ ", "\\");
        ret  = ret.replace("\\n ", "\\n");

        ret  = ret.replace("\\n \\", "\\n\\n");
        ret  = ret.replace("\\n\\nn ", "\\n\\n");
        return ret;
    }


    public static void main(String[] args) {

        GoogleTranslate trans = new GoogleTranslate();

        String orig = "<<1>> <<c:1>>  \\n\\n test \\n testetstest. |\\/:.";
        String booksmaple = "[fragment from the Trials of Saint Alessia]\\n\\nAkatosh made a covenant with Alessia in those days so long ago. He gathered the tangled skeins of Oblivion, and knit them fast with the bloody sinews of his Heart, and gave them to Alessia, saying, \"\"This shall be my token to you, that so long as your blood and oath hold true, yet so shall my blood and oath be true to you. This token shall be the Amulet of Kings, and the Covenant shall be made between us, for I am the King of Spirits, and you are the Queen of Mortals. As you shall stand witness for all Mortal Flesh, so shall I stand witness for all Immortal Spirits.\"\"\\n\\nAnd Akatosh drew from his breast a burning handful of his Heart's blood, and he gave it into Alessia's hand, saying, \"\"This shall also be a token to you of our joined blood and pledged faith. So long as you and your descendants shall wear the Amulet of Kings, then shall this dragonfire burn—an eternal flame—as a sign to all men and gods of our faithfulness. So long as the dragonfires shall burn, to you, and to all generations, I swear that my Heart's blood shall hold fast the Gates of Oblivion.\\n\\n\"\"So long as the Blood of the Dragon runs strong in her rulers, the glory of the Empire shall extend in unbroken years. But should the dragonfires fail, and should no heir of our joined blood wear the Amulet of Kings, then shall the Empire descend into darkness, and the Demon Lords of Misrule shall govern the land.\"\"\\n\\n— from the liturgy of the Re-Kindling of the Dragonfires";
        System.out.println("translate witout original: " + trans.Translate(booksmaple, false));


    }
}