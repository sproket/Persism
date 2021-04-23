package net.sf.persism;

import java.sql.ResultSet;
import java.sql.Statement;

/**
* Class used to wrap Statement and ResultSet together when querying so we can clean them up together.
*
* @author Dan Howard
* @since 6/9/12 9:59 AM
*/
final class JDBCResult {
    Statement st = null;
    ResultSet rs = null;
}
