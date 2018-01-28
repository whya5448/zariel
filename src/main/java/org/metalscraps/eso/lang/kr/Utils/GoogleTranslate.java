package org.metalscraps.eso.lang.kr.Utils;


import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;


public class GoogleTranslate {
    private Translate translate;

    public GoogleTranslate(){
        // Instantiates a client
        translate = TranslateOptions.getDefaultInstance().getService();
    }

    public String Translate(String origin, boolean addOriginText) {
        String ret = "";
        try {
            Translation translation =
                    translate.translate(
                            origin,
                            TranslateOption.sourceLanguage("en"),
                            TranslateOption.targetLanguage("ko"));
            ret = translation.getTranslatedText();
            ret = ret.replaceAll(" &#39;", "'");
            ret = ret+" - G ";
            if(addOriginText){
                ret = ret + "("+origin+")";
            }
        }catch (Exception ex) {
            System.out.println("Google Translate : Exception occur! exception msg [" + ex.getMessage() + "]");
            ret = ret+"Translate fail(by Google translate)";
            ex.printStackTrace();
        }
        return ret;
    }

    public String replaceFormat(String errstr){
        String ret = "";
        //todo : 구글 번역 결과가 뭔가 잘못될경우 이부분에 보정하는 코드 추가해서 사용
        return ret;
    }

    public static void main(String[] args){
        GoogleTranslate trans = new GoogleTranslate();
        String orig = "No one believed the old ouster Del when he staggered into Vulkhel Guard and claimed he'd found a huge deposit of lapis lazuli in the hills, but it turned out to be true, and now Del's Claim is one of the most profitable mines on Auridon.";
        System.out.println("Convert orign : " + orig);
        System.out.println("translate with original: " + trans.Translate(orig,true));
        System.out.println("translate witout original: " + trans.Translate(orig, false));
    }
}