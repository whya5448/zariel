open module org.metalscraps.eso.lang.client {
    requires org.metalscraps.eso.lang.library;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.module.kotlin;
    requires slf4j.api;
    requires java.desktop;
    requires java.net.http;
    requires java.sql;
    requires kotlin.stdlib;
    requires spring.boot;
    requires spring.beans;
    requires spring.context;
    requires spring.boot.autoconfigure;

    exports org.metalscraps.eso.lang.client.config;
    exports org.metalscraps.eso.lang.client.gui;
}