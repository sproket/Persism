/**
 * Persism 2.2.0
 */
module sproket.github.io.persism {
    requires java.sql;
    requires java.desktop;
    requires java.logging;

    requires static org.apache.logging.log4j;
    requires static log4j;
    requires static org.slf4j;

    exports net.sf.persism;
    exports net.sf.persism.annotations;
}