package org.metalscraps.eso.lang.lib.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Created by 안병길 on 2018-01-24.
 * Whya5448@gmail.com
 */

public class AppWorkConfig {

	public AppWorkConfig() {
		this.today = dateTime.format(DateTimeFormatter.ofPattern("MMdd"));
		this.todayWithYear = dateTime.format(DateTimeFormatter.ofPattern("yyMMdd"));
	}

	private Path baseDirectory;
	private Path PODirectory;
	private Path ZanataCategoryConfigDirectory;

	@Getter(AccessLevel.PUBLIC)
	private final String today, todayWithYear;

	@Getter(AccessLevel.PUBLIC)
	private LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

	public Path getBaseDirectory2() {
		return baseDirectory;
	}

	public void setBaseDirectory2(Path baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public Path getPODirectory2() {
		return PODirectory;
	}

	public void setPODirectory2(Path PODirectory) {
		this.PODirectory = PODirectory;
	}

	public Path getZanataCategoryConfigDirectory2() {
		return ZanataCategoryConfigDirectory;
	}

	public void setZanataCategoryConfigDirectory2(Path zanataCategoryConfigDirectory) {
		ZanataCategoryConfigDirectory = zanataCategoryConfigDirectory;
	}

	@Deprecated public File getBaseDirectory() {
		return baseDirectory.toFile();
	}

	@Deprecated public void setBaseDirectory(File baseDirectory) {
		this.baseDirectory = baseDirectory.toPath();
	}

	@Deprecated public File getPODirectory() {
		return PODirectory.toFile();
	}

	@Deprecated public void setPODirectory(File PODirectory) {
		this.PODirectory = PODirectory.toPath();
	}

	@Deprecated public File getZanataCategoryConfigDirectory() {
		return ZanataCategoryConfigDirectory.toFile();
	}

	@Deprecated public void setZanataCategoryConfigDirectory(File zanataCategoryConfigDirectory) {
		ZanataCategoryConfigDirectory = zanataCategoryConfigDirectory.toPath();
	}

}
