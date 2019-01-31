package org.metalscraps.eso.lang.client


enum class AppErrorCode(val errCode: kotlin.Int, @Suppress("UNUSED_PARAMETER") msg: kotlin.String) {
    CANNOT_CREATE_CONFIG_PATH(100, "앱 설정폴더 생성 실패"),
    CANNOT_CREATE_CONFIG_FILE(101, "앱 설정파일 생성 실패"),
    CANNOT_FIND_SERVER_VERSION(102, "서버 접속 실패"),
    CANNOT_DOWNLOAD_TOOL(103, "툴 다운로드 실패"),
    CANNOT_DECOMPRESS_TOOL(104, "툴 압축해제 실패"),
    CANNOT_DOWNLOAD_LANG(105, "언어파일 다운 실패"),
    CANNOT_CREATE_LANG_USING_TOOL(106, "툴 통한 LANG 생성 실패");
}
