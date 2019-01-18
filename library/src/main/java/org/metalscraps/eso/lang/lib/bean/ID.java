package org.metalscraps.eso.lang.lib.bean;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class ID {

    public ID(String id) { this(id.split("[-_]")); }
    private ID(String... args) {
        setHead(args[0]);
        setBody(args[1]);
        setTail(args[2]);
    }

    public ID(String head, String body, String tail) {
        setHead(head);
        setBody(body);
        setTail(tail);
    }

    private void setHead(String head) {
        this.head = head;
        isHeadFileName = head.substring(0,1).matches("^[a-zA-Z]");
    }

    @Override
    public String toString() { return head+"-"+body+"-"+tail; }

    @Getter
    private boolean isHeadFileName;

    @Getter
    private String head;

    @Getter @Setter(AccessLevel.PRIVATE)
    private String body, tail;

    public static class NotFileNameHead extends Exception {}

}
