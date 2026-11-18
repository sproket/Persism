/**
 * Persism 3.0
 * Breaking change of module name from sproket.github.io.persism
 * Sorry about that.
 */
module io.persism {
    requires java.sql;
    requires java.logging;

    requires static org.apache.logging.log4j;
    requires static log4j;
    requires static org.slf4j;

    exports net.sf.persism;
    exports net.sf.persism.annotations;
}