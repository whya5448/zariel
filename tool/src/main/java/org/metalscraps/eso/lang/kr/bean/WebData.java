package org.metalscraps.eso.lang.kr.bean;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.metalscraps.eso.lang.kr.config.WebPageNames;

@Data
public class WebData{
	public WebData(){

	}

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
}