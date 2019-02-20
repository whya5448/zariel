package org.metalscraps.eso.lang.lib.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

open class ESOConfig(private val configDirPath: Path, private val configPath: Path)  {

    private val property = Properties()
    val logger: Logger = LoggerFactory.getLogger(ESOConfig::class.java)

    fun load(map: Map<ESOConfigOptions, Any>) {

        logger.info("앱 설정 폴더 확인")
        if (Files.notExists(configDirPath)) {
            logger.info("폴더 존재하지 않음 생성.")
            try {
                Files.createDirectories(configDirPath)
                logger.info("$configDirPath 생성 성공")
            } catch (e: IOException) {
                logger.error("설정 폴더 생성 실패. 앱 종료")
                e.printStackTrace()
                System.exit(0)
            }
        }

        logger.info("설정 파일 확인")

        if (Files.notExists(configPath)) {
            logger.info("설정 존재하지 않음 생성.")
            try {
                Files.createFile(configPath)
                if (Files.exists(configPath)) logger.info("$configPath 생성 성공")
                else {
                    logger.error("설정 생성 실패. 앱 종료")
                    System.exit(0)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                logger.error("설정 생성 실패. 앱 종료")
                System.exit(0)
            }
        }

        if (Files.exists(configPath) && Files.size(configPath) > 0) property.load(Files.newInputStream(configPath))
        else logger.info("설정 데이터 없음. 초기화")

        map.forEach { t, u -> property.putIfAbsent(t.toString(), u.toString()) }
        this.store()
    }

    open fun store() {
        try {
            if(!Files.exists(configDirPath)) Files.createDirectories(configDirPath)
            Files.newOutputStream(configPath).use { fos -> property.store(fos, "") }
        } catch (e: Exception) {
            logger.error(e.message)
            e.printStackTrace()
        }
    }

    fun exit(errCode:Number) {
        System.exit(errCode.toInt())
    }

    fun getConf(key: ESOConfigOptions): String {
        return property.getProperty(key.toString())
    }

    fun put(key: ESOConfigOptions, value: Any?): Any? {
        var v = value
        if(value is Boolean || value is Number) v = value.toString()
        return property.put(key.toString(), v)
    }
}
