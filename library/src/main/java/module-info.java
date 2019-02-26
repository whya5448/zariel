module org.metalscraps.eso.lang.library {
    requires static lombok;
    requires java.net.http;
    requires slf4j.api;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.io;
    requires java.desktop;
    requires kotlin.stdlib;
    requires jackson.annotations;
    exports org.metalscraps.eso.lang.lib;
    exports org.metalscraps.eso.lang.lib.util;
    exports org.metalscraps.eso.lang.lib.bean;
    exports org.metalscraps.eso.lang.lib.config;
}