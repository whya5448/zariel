package org.metalscraps.eso.lang.kr;

import java.io.File;

class ServerMain {
    void start() {

        System.out.println("서버 모드 작동");
        final AppWorkConfig appWorkConfig = new AppWorkConfig();

        File workDir = new File(".");
        appWorkConfig.setBaseDirectory(workDir);
        appWorkConfig.setPODirectory(new File(appWorkConfig.getBaseDirectory()+"/PO_"+appWorkConfig.getToday()));
        workDir.mkdirs();

        LangManager lm = new LangManager(appWorkConfig);

        // 테스트용 고정값 쓰기
        // lm.getPO();
        lm.enLangToPo();
        lm.Mapping();
        lm.makeServerCSV(new String[]{"kr","tr"});
    }
}
