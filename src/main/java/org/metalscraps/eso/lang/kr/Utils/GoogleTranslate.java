package org.metalscraps.eso.lang.kr.Utils;

import org.metalscraps.eso.lang.kr.bean.PO;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.util.ArrayList;


public class GoogleTranslate implements Runnable{
    private ArrayList<PO> jobList = new ArrayList<>();
    private int jobIndex = 0;


    public GoogleTranslate() {
    }

    @Override
    public void run(){
        int localIndex = this.getIndex();
        PO localPO = this.getPO(localIndex);
        localPO.setTarget(this.Translate(localPO.getSource(), false));
        this.setPO(localIndex, localPO);
    }


    public void addJob(PO job){
        //System.out.println("job add : "+ job.getTarget());
        this.jobList.add(job);
    }

    public void clearJob(){
        this.jobList.clear();
    }

    private synchronized int getIndex(){
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

    public synchronized ArrayList<PO> getResult(){
        return this.jobList;
    }

    public String Translate(String origin, boolean addOriginText) {
        Translate translate = TranslateOptions.getDefaultInstance().getService();

        String conv = origin.toString();
        conv = this.replaceTag(origin, "encode");

        String ret = "";
        try {
            Translation translation =
                    translate.translate(
                            conv,
                            TranslateOption.sourceLanguage("en"),
                            TranslateOption.targetLanguage("ko"));
            ret = translation.getTranslatedText();

            ret = this.replaceTag(ret, "decode");

            ret = ret+"-G-";
            if(addOriginText){
                ret = ret + "("+origin+")";
            }
        }catch (Exception ex) {
            System.out.println("Google Translate : Exception occur! exception msg [" + ex.getMessage() + "]");
            ret = ret+"Translate fail(by Google translate)";
            ex.printStackTrace();
        }
        System.out.println("result : "+ret);
        return ret;
    }

    public void HtmlConvertAll(){

        for(PO p : jobList) {
            String orig = p.getSource();
            orig = this.replaceTag(orig, "encode");
            p.setSource(orig);

            String target = p.getSource();
            target = this.replaceTag(target, "encode");
            p.setTarget(target);
        }
    }

    private String ReplaceString(String Expression, String Pattern, String Rep)
    {
        if (Expression==null || Expression.equals("")) return "";

        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();

        while ((e = Expression.indexOf(Pattern, s)) >= 0) {
            result.append(Expression.substring(s, e));
            result.append(Rep);
            s = e + Pattern.length();
        }
        result.append(Expression.substring(s));
        return result.toString();
    }


    private String replaceTag(String Expression, String type){
        String result = "";
        if (Expression==null || Expression.equals("")) return "";

        if (type == "encode") {
            result = ReplaceString(Expression, "&", "&amp;");
            result = ReplaceString(result, "\"", "&quot;");

            result = ReplaceString(result, "'", "&apos;");
            result = ReplaceString(result, "<", "&lt;");
            result = ReplaceString(result, ">", "&gt;");
            result = ReplaceString(result, "\r", "<br>");
            result = ReplaceString(result, "\n", "<p>");
        }
        else if (type == "decode") {
            result = ReplaceString(Expression, "&amp;", "&");
            result = ReplaceString(result, "&quot;", "\"");

            result = ReplaceString(result, "&apos;", "'");
            result = ReplaceString(result, "&lt;", "<");
            result = ReplaceString(result, "&gt;", ">");
            result = ReplaceString(result, "<br>", "\r");
            result = ReplaceString(result, "<p>", "\n");
        }

        return result;
    }


    public static void main(String[] args){
        GoogleTranslate trans = new GoogleTranslate();
        String orig = "No one believed the old ouster Del when he staggered into Vulkhel Guard and claimed he'd found a huge deposit of lapis lazuli in the hills, but it turned out to be true, and now Del's Claim is one of the most profitable mines on Auridon.";
        System.out.println("Convert orign : " + orig);
        System.out.println("translate with original: " + trans.Translate(orig,true));
        System.out.println("translate witout original: " + trans.Translate(orig, false));
    }
}