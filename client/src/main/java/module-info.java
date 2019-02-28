open module org.metalscraps.eso.lang.client {
    requires org.metalscraps.eso.lang.library;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires slf4j.api;
    requires java.desktop;
    requires java.net.http;
    requires kotlin.stdlib;
    requires spring.boot;
    requires spring.beans;
    requires spring.context;
    requires spring.boot.autoconfigure;
    requires com.fasterxml.jackson.module.kotlin;

    exports org.metalscraps.eso.lang.client.config;
    exports org.metalscraps.eso.lang.client.gui;
}