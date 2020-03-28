open module org.metalscraps.eso.lang.library {
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.io;
    requires java.desktop;
    requires kotlin.stdlib;
    requires kotlin.stdlib.jdk8;
    exports org.metalscraps.eso.lang.lib;
    exports org.metalscraps.eso.lang.lib.util;
    exports org.metalscraps.eso.lang.lib.bean;
    exports org.metalscraps.eso.lang.lib.config;

}