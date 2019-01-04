package org.metalscraps.eso.lang.kr.bean;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.metalscraps.eso.lang.kr.config.WebPageNames;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class WebData{
	public WebData(){

	}
	@Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC)
	private String ItemFileName, ItemURL;

	@Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC)
	private WebPageNames PageName;

	@Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC)
	private Document HTML;

	@Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC)
	private Elements WebTables = null;

	public void addWebTable(Element table){
		if(this.WebTables == null){
			this.WebTables = new Elements();
		}
		this.WebTables.add(table);
	}

	@Getter
	private HashMap<String, ArrayList<String>> bookMap = null;

	public void putBookMap(String category, ArrayList<String> titles){
		if(this.bookMap == null){
			this.bookMap = new HashMap<>();
		}
		bookMap.put(category, titles);
	}
}