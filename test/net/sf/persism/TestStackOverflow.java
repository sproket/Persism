package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.dao.so.Comment;
import net.sf.persism.dao.so.ExtendedUser;
import net.sf.persism.dao.so.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.where;

public class TestStackOverflow extends TestCase {

    Connection con;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        String url = "jdbc:sqlserver://localhost;database=StackOverflow2010;integratedSecurity=true;";
        con = DriverManager.getConnection(url);
    }

    public void testExtendedUsers() {
        long ms = System.currentTimeMillis();
        Session session = new Session(con);
        List<ExtendedUser> users = session.query(ExtendedUser.class, where("Id < ?"), params(1000));

        System.out.println(users.size());
        System.out.println("time: " + (System.currentTimeMillis() - ms));

//        ms = System.currentTimeMillis();
//        List<Comment> comments = session.query(Comment.class, where("id < ?"), params(100000));
//        System.out.println("COMMENTS: " + comments.size());
//        System.out.println("time: " + (System.currentTimeMillis() - ms));
//
//        String w1 = """
//                EXISTS (SELECT [UserId]\s
//                FROM [Comments]\s
//                	WHERE EXISTS (SELECT [Id] FROM [Posts]\s
//                		WHERE EXISTS (SELECT [Id] FROM [Users] WHERE Id < 1000  AND [Users].[Id] = [Posts].[OwnerUserId])  AND [Posts].[Id] = [Comments].[PostId])  AND [Comments].[UserId] = [Users].[Id])
//                		""";
//
//        String w2 = """
//                EXISTS (SELECT [UserId] FROM [Comments] WHERE id < 500000  AND [Comments].[UserId] = [Users].[Id])
//                                """;
//        List<User> results;
//        ms = System.currentTimeMillis();
//        results = session.query(User.class, where(w1));
//        System.out.println(results.size());
//        System.out.println("time: " + (System.currentTimeMillis() - ms));
//
//        ms = System.currentTimeMillis();
//        results = session.query(User.class, where(w2));
//        System.out.println(results.size());
//        System.out.println("time: " + (System.currentTimeMillis() - ms));
    }

//    public void testExtendedUsers2() {
//        long ms = System.currentTimeMillis();
//        Session session = new Session(con);
//        List<ExtendedUser> users = session.query(ExtendedUser.class, where("Id < 1000"));
//        System.out.println(users.size());
//        System.out.println("time: " + (System.currentTimeMillis() - ms));
//    }
}
