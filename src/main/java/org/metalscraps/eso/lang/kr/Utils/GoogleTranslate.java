package org.metalscraps.eso.lang.kr.Utils;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import org.apache.commons.text.StringEscapeUtils;
import org.metalscraps.eso.lang.kr.bean.PO;

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
        localPO.setTarget(this.Translate(localPO.getSource(), true));
        this.setPO(localIndex, localPO);
    }


    public void addJob(PO job) {
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

    public synchronized ArrayList<PO> getResult(){
        return this.jobList;
    }

    public String Translate(String origin, boolean addOriginText) {

        Translate translate = TranslateOptions.getDefaultInstance().getService();
        StringBuilder sb = new StringBuilder(StringEscapeUtils.escapeXml11(origin));
        Utils.replaceStringBuilder(sb, GoogleTranslate.SYMBOL, GoogleTranslate.TAG);

        try {
            Translation translation =
                    translate.translate(
                            sb.toString(),
                            TranslateOption.sourceLanguage("en"),
                            TranslateOption.targetLanguage("ko"));
            sb = new StringBuilder(StringEscapeUtils.unescapeXml(translation.getTranslatedText()));
            Utils.replaceStringBuilder(sb, GoogleTranslate.TAG, GoogleTranslate.SYMBOL);
            sb.append(" - G ");
            if(addOriginText) sb.append("(").append(origin).append(")");
        } catch (Exception ex) {
            System.out.println("Google Translate : Exception occur! exception msg [" + ex.getMessage() + "]");
            sb.append("Translate fail(by Google translate)");
            ex.printStackTrace();
        }
        System.out.println("result : "+sb);
        return sb.toString();
    }


    public static void main(String[] args) {
        GoogleTranslate trans = new GoogleTranslate();
        String orig = "No one believed the old ouster Del when he staggered into Vulkhel Guard and claimed he'd found a huge deposit of lapis lazuli in the hills, but it turned out to be true, and now Del's Claim is one of the most profitable mines on Auridon.";
        System.out.println("Convert orign : " + orig);
        System.out.println("translate with original: " + trans.Translate(orig,true));
        System.out.println("translate witout original: " + trans.Translate(orig, false));
    }
}