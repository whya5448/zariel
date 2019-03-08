open module org.metalscraps.eso.lang.tool {
    requires static lombok;
    requires org.metalscraps.eso.lang.library;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.module.kotlin;
    requires java.desktop;
    requires slf4j.api;
	requires kotlin.stdlib;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
}