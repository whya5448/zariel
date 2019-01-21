package org.metalscraps.eso.lang.lib.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ID {

    public ID(String head, String body, String tail) {
        setHead(head);
        setBody(body);
        setTail(tail);
    }

    public void setHead(String head) {
        this.head = head;
        isFileNameHead = head.substring(0,1).matches("^[a-zA-Z]");
    }

    @Override
    public String toString() { return head+"-"+body+"-"+tail; }

    private boolean isFileNameHead;

    private String head, body, tail;

    public static class NotFileNameHead extends Exception {}

}
