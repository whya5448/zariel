package org.metalscraps.eso.lang.lib.bean;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.metalscraps.eso.lang.lib.config.FileNames;

import java.util.Comparator;
import java.util.Objects;

/**
 * Created by 안병길 on 2018-01-18.
 * Whya5448@gmail.com
 */

@Data
public class PO implements Comparable<PO> {

    public static Comparator<PO> comparator = (o1, o2) -> {
        if(!Objects.equals(o1.getId1(), o2.getId1())) return o1.getId1() - o2.getId1();
        if(!Objects.equals(o1.getId2(), o2.getId2())) return o1.getId2() - o2.getId2();
        return o1.getId3() - o2.getId3();
    };

	public enum POWrapType {
		WRAP_ALL,
		WRAP_SOURCE,
		WRAP_TARGET
	}

	public PO(String id, String source, String target) {
		source = source.replaceAll("\"\n\"", "");
		target = target.replaceAll("\"\n\"", "");

		this.id = id;
		this.source = source;
		this.target = target;

		String[] ids = id.split("-");
		id1 = Integer.parseInt(ids[0]);
		id2 = Integer.parseInt(ids[1]);
		id3 = Integer.parseInt(ids[2]);

		if(target.equals("")) this.target = source;
		else if(source.equals("")) this.source = target;
	}

	private Integer id1, id2, id3;

	@Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC)
	private String id, source, target, stringFileName;
	private boolean fuzzy = false;
	private FileNames fileName;


	public boolean modifyDoubleQuart(){
		this.source  =source.replace("\"\"", "\"");
		this.target  =target.replace("\"\"", "\"");
		return true;
	}

	public PO wrap(String prefix, String suffix, POWrapType wrapType) {

		if (prefix == null) prefix = "";
		if (suffix == null) suffix = "";

		if(wrapType == POWrapType.WRAP_ALL) {
			if(!source.equals("")) source = prefix + source + suffix;
			if(!target.equals("")) target = prefix + target + suffix;
		} else if(wrapType == POWrapType.WRAP_SOURCE && !source.equals("")) source = prefix + source + suffix;
		else if(wrapType == POWrapType.WRAP_TARGET && !target.equals("")) target = prefix + target + suffix;

		return this;
	}

	public String toStringDefault() { return toCSV(new ToCSVConfig()); }

	public String toCSV(ToCSVConfig toCSVConfig) {
		String translatedMsg = "";

		if (toCSVConfig.isWriteFileName()) {
			translatedMsg = (stringFileName + "_" + id2 + "_" + id3 + "_" + target);
		} else if (toCSVConfig.isBeta()){
			translatedMsg = target;
		} else {
			translatedMsg = target;
			if(isFuzzy() || target.contains("-G-")){
				translatedMsg = source;
			}
		}

		return "\""+id+"\",\""+(toCSVConfig.isWriteSource()?source:"")+"\",\""+translatedMsg+"\"\n";

	}

	public StringBuilder toPO() {
		StringBuilder sb = new StringBuilder("\n\n#: ").append(getId());
		if(isFuzzy()) sb.append("\n#, fuzzy");
		sb
				.append("\nmsgctxt \"").append(getId()).append("\"")
				.append("\nmsgid \"").append(getSource()).append("\"")
				.append("\nmsgstr \"\"");
		return sb;
	}

	public StringBuilder toPOT() {
		StringBuilder sb = new StringBuilder("\n\n#: ").append(getId());
		sb
				.append("\nmsgctxt \"").append(getId()).append("\"")
				.append("\nmsgid \"").append(getSource()).append("\"")
				.append("\nmsgstr \"\"");
		return sb;
	}

	public StringBuilder toTranslatedPO() {
		StringBuilder sb = new StringBuilder("\n\n#: ").append(getId());
		if (isFuzzy()) sb.append("\n#, fuzzy");
		sb
				.append("\nmsgctxt \"").append(getId()).append("\"")
				.append("\nmsgid \"").append(getSource()).append("\"")
				.append("\nmsgstr \"").append(getTarget()).append("\"");
		return sb;
	}

	@Override
	public int compareTo(PO o) {
		String src = Integer.toString(o.id2) + o.id3;
		String trg = Integer.toString(this.id2) + this.id3;
		if (src.equals(trg))
			return this.id1.compareTo(o.id1);
		else
			return src.compareTo(trg);
	}
}