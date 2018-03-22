package org.metalscraps.eso.lang.kr.bean;

import javafx.util.Pair;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.metalscraps.eso.lang.kr.config.WebPageNames;

import java.util.*;

@Data
public class CategoryCSV {

	@Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC)
	private String Category;

	@Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC)
	ArrayList< Pair<String, String>> data = null;

	public void addPair(String NameIndex, String DescIndex){
		if(this.data == null){
			this.data = new ArrayList<Pair<String,String>>();
		}

	}
}