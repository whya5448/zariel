package org.metalscraps.eso.lang.lib.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
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


	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PUBLIC)
	private File baseDirectory, PODirectory;

	@Getter(AccessLevel.PUBLIC)
	private final String today, todayWithYear;

	@Getter(AccessLevel.PUBLIC)
	private LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
}
