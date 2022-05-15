package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.categories.ExternalDB;
import net.sf.persism.dao.so.*;
import org.junit.experimental.categories.Category;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.sql;
import static net.sf.persism.SQL.where;

@Category(ExternalDB.class)
public class TestStackOverflow extends TestCase {

    Connection con;
    Session session;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        String url = "jdbc:sqlserver://localhost;database=StackOverflow2010;integratedSecurity=true;";
        con = DriverManager.getConnection(url);
        session = new Session(con);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testExtendedUsers() {
        long ms = System.currentTimeMillis();
        List<ExtendedUser> users = session.query(ExtendedUser.class, where("Id < ?"), params(1000));
        System.out.println(users.size());
        System.out.println("time: " + (System.currentTimeMillis() - ms));
    }

    public void testExtendedUser() {
        long ms = System.currentTimeMillis();
        ExtendedUser user = session.fetch(ExtendedUser.class, where("Id = ?"), params(4918));
        System.out.println("time: " + (System.currentTimeMillis() - ms));
        System.out.println(user);
        assertNotNull(user);
        System.out.println("testExtendedUser: votes: " + user.getVotes().size() + " posts: " + user.getPosts().size() + " badges: " + user.getBadges().size());
        assertEquals(113, user.getVotes().size());
        assertEquals(442, user.getPosts().size());
        assertEquals(83, user.getBadges().size());
        assertEquals(0, user.getOtherStuff().size()); // because we marked it "transient"
    }

    public void testGetPost() {
        // 12
        long ms = System.currentTimeMillis();
        ExtendedPost post = session.fetch(ExtendedPost.class, where("Id = ?"), params(12));
        assertNotNull(post);
        assertNotNull(post.getParentPost());

        System.out.println(post);
        System.out.println("time: " + (System.currentTimeMillis() - ms));
    }

    public void testBadges() {
        ExtendedUser user = session.fetch(ExtendedUser.class, where("Id = ?"), params(4918));
        System.out.println(user);
        assertNotNull(user);
        assertEquals(83, user.getBadges().size());

        user = session.fetch(ExtendedUser.class, where("Id = ?"), params(-1));
        System.out.println(user);
        assertNotNull(user);
        assertEquals(0, user.getBadges().size());

        Badge badge = new Badge().name("test1").userId(-1).date(Timestamp.valueOf(LocalDateTime.now()));
        session.insert(badge);
        System.out.println(badge);
        assertTrue(badge.id() > 0);
        assertNotNull(badge.date());
        session.delete(badge);

    }

    public void testVoteTypes() {

        VoteType voteType = new VoteType().name("TESTING").id(1);
        System.out.println(voteType);

        System.out.println(session.query(VoteType.class));

        VoteType vt = session.fetch(VoteType.class, where("id=1"));
        assertNotNull(vt);
        System.out.println(vt);
        String org = vt.name();
        vt.name("mooo");
        session.update(vt);

        vt = session.fetch(VoteType.class, where("id=1"));
        assertNotNull(vt);
        System.out.println(vt);

        vt.name(org);
        session.update(vt);

        vt = session.fetch(VoteType.class, where("id=1"));
        System.out.println(vt);

    }

    public void testVotes() {
        var votes = session.query(Vote.class, where(":id < 1000"));
        System.out.println(votes);

        Vote vote = session.fetch(Vote.class, where(":id = 1"));
        assertNotNull(vote);
        System.out.println(vote);

        var org = vote.creationDate();
        vote.setCreationDate(Timestamp.valueOf(LocalDateTime.now()));
        session.update(vote);

        vote = session.fetch(Vote.class, where(":id = 1"));
        System.out.println(vote);
        vote.setCreationDate(org);
        session.update(vote);

        vote = session.fetch(Vote.class, where(":id = 1"));
        System.out.println(vote);
    }

    public void testExistVSIN() {
        String sql;
        int rowsIn;
        int rowsExists;

        // EXISTS performs better

        // User. Same both cases.
        sql = """
                SELECT [Id], [AboutMe], [Age], [CreationDate], [DisplayName], [DownVotes], [EmailHash], [LastAccessDate],
                [Location], [Reputation], [UpVotes], [Views], [WebsiteUrl], [AccountId] FROM [dbo].[Users] WHERE Id < ?
                    """;
        exec(User.class, sql, 1000);

        // Posts IN
        sql = """
                SELECT [Id], [AcceptedAnswerId], [AnswerCount], [Body], [ClosedDate], [CommentCount], [CommunityOwnedDate],
                 [CreationDate], [FavoriteCount], [LastActivityDate], [LastEditDate], [LastEditorDisplayName], [LastEditorUserId], 
                 [OwnerUserId], [ParentId], [PostTypeId], [Score], [Tags], [Title], [ViewCount] 
                 FROM [dbo].[Posts] 
                 WHERE [OwnerUserId] IN (SELECT [Id] FROM [Users] WHERE Id < ?)                 
                    """;
        rowsIn = exec(Post.class, sql, 1000);

        // Posts EXISTS
        sql = """
                SELECT [Id], [AcceptedAnswerId], [AnswerCount], [Body], [ClosedDate], [CommentCount], [CommunityOwnedDate], 
                 [CreationDate], [FavoriteCount], [LastActivityDate], [LastEditDate], [LastEditorDisplayName], [LastEditorUserId], 
                 [OwnerUserId], [ParentId], [PostTypeId], [Score], [Tags], [Title], [ViewCount] 
                 FROM [dbo].[Posts] 
                 WHERE EXISTS (SELECT [Id] FROM [Users] WHERE Id < ?  AND [Users].[Id] = [Posts].[OwnerUserId])                 
                    """;
        rowsExists = exec(Post.class, sql, 1000);
        assertEquals(rowsIn, rowsExists);

        // Comments IN
        sql = """
                SELECT [Id], [CreationDate], [PostId], [Score], [Text], [UserId] 
                 FROM [dbo].[Comments] 
                 WHERE [PostId] IN (SELECT [Id] FROM [Posts] 
                    WHERE [OwnerUserId] IN (SELECT [Id] FROM [Users] WHERE Id < ?))                 
                    """;
        rowsIn = exec(Comment.class, sql, 1000);

        // Comments EXISTS
        sql = """
                SELECT [Id], [CreationDate], [PostId], [Score], [Text], [UserId] 
                FROM [dbo].[Comments] 
                 WHERE EXISTS (SELECT [Id] FROM [Posts] 
                    WHERE EXISTS (SELECT [Id] FROM [Users] WHERE Id < ?  AND [Users].[Id] = [Posts].[OwnerUserId])  AND [Posts].[Id] = [Comments].[PostId])                 
                    """;
        rowsExists = exec(Comment.class, sql, 1000);
        assertEquals(rowsIn, rowsExists);

        // Users IN (from Comments)
        sql = """
                SELECT [Id], [AboutMe], [Age], [CreationDate], [DisplayName], [DownVotes], [EmailHash], [LastAccessDate], 
                 [Location], [Reputation], [UpVotes], [Views], [WebsiteUrl], [AccountId] 
                 FROM [dbo].[Users] 
                 WHERE [Id] IN (SELECT [UserId] FROM [Comments] 
                  WHERE [PostId] IN (SELECT [Id] FROM [Posts] 
                      WHERE [OwnerUserId] IN (SELECT [Id] FROM [Users] WHERE Id < ?)))   
                  """;
        rowsIn = exec(User.class, sql, 1000);

        // Users EXISTS (from Comments)
        sql = """
                SELECT [Id], [AboutMe], [Age], [CreationDate], [DisplayName], [DownVotes], [EmailHash], [LastAccessDate], 
                 [Location], [Reputation], [UpVotes], [Views], [WebsiteUrl], [AccountId] 
                 FROM [dbo].[Users] 
                 WHERE EXISTS (SELECT [UserId] FROM [Comments] 
                    WHERE EXISTS (SELECT [Id] FROM [Posts] 
                        WHERE EXISTS (SELECT [Id] FROM [Users] WHERE Id < ?  AND [Users].[Id] = [Posts].[OwnerUserId])  AND [Posts].[Id] = [Comments].[PostId])  AND [Comments].[UserId] = [Users].[Id]) 
                """;
        rowsExists = exec(User.class, sql, 1000);
        assertEquals(rowsIn, rowsExists);

        // Comment for post user IN
        sql = """
                SELECT [Id], [CreationDate], [PostId], [Score], [Text], [UserId] 
                 FROM [dbo].[Comments] 
                 WHERE [PostId] IN (SELECT [Id] FROM [Posts] 
                    WHERE [OwnerUserId] IN (SELECT [Id] FROM [Users] WHERE Id < ?))  AND [UserId] IN (SELECT [OwnerUserId] FROM [Posts] 
                        WHERE [OwnerUserId] IN (SELECT [Id] FROM [Users] WHERE Id < ?))        
                """;
        rowsIn = exec(Comment.class, sql, 1000, 1000);

        // comment for post user EXISTS
        sql = """
                SELECT [Id], [CreationDate], [PostId], [Score], [Text], [UserId] 
                 FROM [dbo].[Comments] 
                 WHERE EXISTS (SELECT [Id],[OwnerUserId] FROM [Posts] 
                    WHERE EXISTS (SELECT [Id] FROM [Users] WHERE Id < ?  AND [Users].[Id] = [Posts].[OwnerUserId])  AND [Posts].[Id] = [Comments].[PostId] AND [Posts].[OwnerUserId] = [Comments].[UserId])                 
                """;
        rowsExists = exec(Comment.class, sql, 1000);
        // these are NOT equal. IN with multiple rows returns EXTRA WE WONT WANT
        assertTrue(rowsIn > rowsExists);

        //  user comment for post user IN
        sql = """
                SELECT [Id], [AboutMe], [Age], [CreationDate], [DisplayName], [DownVotes], [EmailHash], [LastAccessDate], 
                 [Location], [Reputation], [UpVotes], [Views], [WebsiteUrl], [AccountId] 
                 FROM [dbo].[Users] 
                 WHERE [Id] IN (SELECT [UserId] FROM [Comments] 
                    WHERE [PostId] IN (SELECT [Id] FROM [Posts] 
                        WHERE [OwnerUserId] IN (SELECT [Id] FROM [Users] WHERE Id < ?))  AND [UserId] IN (SELECT [OwnerUserId] FROM [Posts] 
                            WHERE [OwnerUserId] IN (SELECT [Id] FROM [Users] WHERE Id < ?)))                 
                    """;
        rowsIn = exec(User.class, sql, 1000, 1000);

        //  user comment for post user EXISTS
        sql = """
                SELECT [Id], [AboutMe], [Age], [CreationDate], [DisplayName], [DownVotes], [EmailHash], [LastAccessDate], 
                 [Location], [Reputation], [UpVotes], [Views], [WebsiteUrl], [AccountId] 
                 FROM [dbo].[Users] 
                 WHERE EXISTS (SELECT [UserId] FROM [Comments] 
                    WHERE EXISTS (SELECT [Id],[OwnerUserId] FROM [Posts] 
                        WHERE EXISTS (SELECT [Id] FROM [Users] WHERE Id < ?  AND [Users].[Id] = [Posts].[OwnerUserId])  AND [Posts].[Id] = [Comments].[PostId] AND [Posts].[OwnerUserId] = [Comments].[UserId])  AND [Comments].[UserId] = [Users].[Id])                 
                    """;
        rowsExists = exec(User.class, sql, 1000);
        // Same with comment query
        assertTrue(rowsIn > rowsExists);

    }

    private int exec(Class<?> objectClass, String sql, Object... params) {
        long ms = System.currentTimeMillis();
        List<?> result = session.query(objectClass, sql(sql), params(params));
        System.out.println(sql.trim());
        System.out.println(objectClass.getSimpleName() + " time: " + (System.currentTimeMillis() - ms) + " rows: " + result.size());
        System.out.println("**************************************");
        return result.size();
    }

}
