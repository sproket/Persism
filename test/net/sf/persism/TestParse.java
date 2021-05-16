package net.sf.persism;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestParse {

    private static final Pattern CONST_PATTERN
            = Pattern.compile("([^0-9a-zA-Z])((?:[0-9]+(?:\\.[0-9]*)?|[0-9]*\\.[0-9]+)"
            + "(?:[Ee][+-][0-9]+])?"
            + "|(?:\\'[^']*\\')+)", Pattern.CASE_INSENSITIVE);

    private static class ParameterizedQuery {
        final String sql;
        final Parameter[] params;

        ParameterizedQuery(String sql, Parameter[] params) {
            this.sql = sql;
            this.params = params.clone();
        }
    }

    private static class Parameter {
        final int position;
        final String value;

        Parameter(int position, String value) {
            this.position = position;
            this.value = value;
        }
    }

    private static ParameterizedQuery parse(String query) {
        List<Parameter> parms = new ArrayList<>();
        Matcher matcher = CONST_PATTERN.matcher(query);
        int start = 0;
        StringBuilder buf = new StringBuilder();
        while (matcher.find()) {
            int pos = matcher.start();
            buf.append(query, start, pos)
                    .append(matcher.group(1))
                    .append("?");
            parms.add(new Parameter(buf.length()-1,matcher.group(2)));
            start = matcher.end();
        }
        buf.append(query, start, query.length());
        return new ParameterizedQuery(
                buf.toString(), parms.toArray(new Parameter[parms.size()]));
    }

    private static ParameterizedQuery[] simplify(ParameterizedQuery[] queries) {
        if (queries.length == 0) {
            return queries;
        }
        ParameterizedQuery prev = null;
        boolean[] diff = null;
        for (ParameterizedQuery cur: queries) {
            if (prev == null) {
                diff = new boolean[cur.params.length];
            } else {
                if (!cur.sql.equals(prev.sql)) {
                    throw new RuntimeException(
                            "Queries are too different: [" + prev.sql
                                    + "] and [" + cur.sql + "]");
                } else if (cur.params.length != prev.params.length) {
                    throw new RuntimeException(
                            "Different number of parameters: ["
                                    + prev.params.length
                                    + "] and [" + cur.params.length + "]");
                }
                for (int i = 0; i < diff.length; ++i) {
                    if (!cur.params[i].value.equals(prev.params[i].value)) {
                        diff[i] = true;
                    }
                }
            }
            prev = cur;
        }
        if (and(diff)) {
            return queries;
        }
        ParameterizedQuery[] result = new ParameterizedQuery[queries.length];
        result[0] = expandQuery(queries[0].sql, queries[0].params, diff);
        for (int i = 1; i < queries.length; ++i) {
            result[i] = new ParameterizedQuery(result[0].sql,
                    keep(queries[i].params, result[0].params, diff));
        }
        return result;
    }

    private static boolean and(boolean[] arr) {
        for (boolean b: arr) {
            if (!b) {
                return false;
            }
        }
        return true;
    }

    private static ParameterizedQuery expandQuery(String query,
                                                  Parameter[] params, boolean[] diff) {
        int count = 0;
        for (boolean b: diff) {
            if (b) {
                ++count;
            }
        }
        Parameter[] result = new Parameter[count];
        int r = 0;
        int start = 0;
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < diff.length; ++i) {
            Parameter parm = params[i];
            if (!diff[i]) {
                // expand param
                buf.append(query, start, parm.position);
                buf.append(parm.value);
                start = parm.position+1;
            } else {
                buf.append(query, start, parm.position);
                result[r++] = new Parameter(buf.length(), parm.value);
                start = parm.position;
            }
        }
        buf.append(query, start, query.length());
        return new ParameterizedQuery(buf.toString(), result);
    }

    private static Parameter[] keep(Parameter[] params, Parameter[] ref,
                                    boolean[] diff) {
        Parameter[] result = new Parameter[ref.length];
        int j = 0;
        for (int i = 0; i < params.length; ++i) {
            if (diff[i]) {
                result[j] = new Parameter(ref[j].position, params[i].value);
                ++j;
            }
        }
        return result;
    }

    private static final String[] QUERIES = {
//            "select * from tableName as t1 where t1.tableColumnId=4 and t1.tableColumnName='test' inner join tableName2 as t2 on t1.tableColumnId=t2.tableColumnId",
//            "select * from tableName as t1 where t1.tableColumnId=6 and t1.tableColumnName='test' inner join tableName2 as t2 on t1.tableColumnId=t2.tableColumnId",
            "WHERE [IDENTITY]=?",
    };

    @Test
    public void testParse() {
        ParameterizedQuery[] queries = new ParameterizedQuery[QUERIES.length];
        for (int i = 0; i < QUERIES.length; ++i) {
            queries[i] = parse(QUERIES[i]);
        }
        for (ParameterizedQuery cur: queries) {
            System.out.println(cur.sql);
            int i = 0;
            for (Parameter parm: cur.params) {
                System.out.println("    " + (++i) + ": " + parm.value);
            }
        }
        queries = simplify(queries);
        for (ParameterizedQuery cur: queries) {
            System.out.println(cur.sql);
            int i = 0;
            for (Parameter parm: cur.params) {
                System.out.println("    " + (++i) + ": " + parm.value);
            }
        }
    }

}
