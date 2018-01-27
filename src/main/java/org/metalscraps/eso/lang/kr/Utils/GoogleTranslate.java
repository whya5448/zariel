package org.metalscraps.eso.lang.kr.Utils;


import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;


public class GoogleTranslate {
    private Translate translate;

    GoogleTranslate(){
        // Instantiates a client
        translate = TranslateOptions.getDefaultInstance().getService();
    }

    public String Translate(String origin) {
        String ret = "";
        try {
            Translation translation =
                    translate.translate(
                            origin,
                            TranslateOption.sourceLanguage("en"),
                            TranslateOption.targetLanguage("ko"));
            ret = translation.getTranslatedText();
        }catch (Exception ex) {
            System.out.println("Google Translate : Exception occur! exception msg [" + ex.getMessage() + "]");
        }

        return ret;
    }
}