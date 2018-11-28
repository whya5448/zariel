package org.metalscraps.eso.lang.kr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

class ServerMain {

    private static final Logger logger = LoggerFactory.getLogger("org.metalscraps.eso.lang.kr.ServerMain");
    private boolean useCache = false;

    void start(String[] args) {

        for(var s : args) {
            if(s.equals("-cache")) useCache = true;
        }

        System.out.println("서버 모드 작동");
        final AppWorkConfig appWorkConfig = new AppWorkConfig();

        File workDir = new File(".");
        appWorkConfig.setBaseDirectory(workDir);
        appWorkConfig.setPODirectory(new File(appWorkConfig.getBaseDirectory()+"/PO_"+appWorkConfig.getToday()));
        workDir.mkdirs();

        LangManager lm = new LangManager(appWorkConfig);

        // 테스트용 고정값 쓰기
        if(!useCache) lm.getPO();
        else appWorkConfig.setPODirectory(new File((appWorkConfig.getBaseDirectory()+"/PO_CACHE")));

        lm.Mapping();
        lm.serverWork();
    }
}
