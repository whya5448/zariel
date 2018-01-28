package org.metalscraps.eso.lang.kr.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Created by 안병길 on 2018-01-24.
 * Whya5448@gmail.com
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ToCSVConfig {
	boolean writeSource = false, writeFileName = false, removeComment = false;
}
