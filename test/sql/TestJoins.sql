/*

-- Extended User
SELECT [Id], [AboutMe], [Age], [CreationDate], [DisplayName], [DownVotes], [EmailHash], [LastAccessDate], [Location], [Reputation], [UpVotes], [Views], [WebsiteUrl], [AccountId] 
FROM [dbo].[Users] WHERE Id = 4918 

-- User -> Posts
-- PPN=[id], CPN=[ownerUserId], PC=ExtendedUser, CC=ExtendedPost, CS=false, JP=posts, PQ=false}
SELECT [Id], [AcceptedAnswerId], [AnswerCount], [Body], [ClosedDate], [CommentCount], [CommunityOwnedDate], [CreationDate], [FavoriteCount], [LastActivityDate], 
	[LastEditDate], [LastEditorDisplayName], [LastEditorUserId], [OwnerUserId], [ParentId], [PostTypeId], [Score], [Tags], [Title], [ViewCount] 
FROM [dbo].[Posts] 
	WHERE EXISTS (SELECT [Id] FROM [dbo].[Users] WHERE Id = 4918  AND [dbo].[Users].[Id] = [dbo].[Posts].[OwnerUserId])

-- Posts -> Comments (USER ? comments for posts)
-- PPN=[id], CPN=[postId], PC=ExtendedPost, CC=Comment, CS=false, JP=allComments, PQ=true}
SELECT [Id], [CreationDate], [PostId], [Score], [Text], [UserId] 
FROM [dbo].[Comments] 
	WHERE EXISTS (SELECT [Id] FROM [dbo].[Posts] 
		WHERE EXISTS (SELECT [Id] FROM [dbo].[Users] WHERE Id = 4918 AND [dbo].[Users].[Id] = [dbo].[Posts].[OwnerUserId])  AND [dbo].[Posts].[Id] = [dbo].[Comments].[PostId]) 
--AND Comments.UserId IS null

SELECT * FROM Comments where userId is null
-- Comment -> User
-- PPN=[userId], CPN=[id], PC=Comment, CC=User, CS=false, JP=user, PQ=true}
SELECT [Id], [AboutMe], [Age], [CreationDate], [DisplayName], [DownVotes], [EmailHash], [LastAccessDate], [Location], [Reputation], [UpVotes], [Views], [WebsiteUrl], [AccountId] 
FROM [dbo].[Users] 
	WHERE EXISTS (SELECT [UserId] FROM [dbo].[Comments] 
		WHERE EXISTS (SELECT [Id] FROM [dbo].[Posts] 
			WHERE EXISTS (SELECT [Id] FROM [dbo].[Users] WHERE Id = 4918 AND [dbo].[Users].[Id] = [dbo].[Posts].[OwnerUserId])  AND [dbo].[Posts].[Id] = [dbo].[Comments].[PostId])  AND [dbo].[Comments].[UserId] = [dbo].[Users].[Id]) 

-- PPN=[id, ownerUserId], CPN=[postId, userId], PC=ExtendedPost, CC=Comment, CS=false, JP=myComments, PQ=true}
SELECT [Id], [CreationDate], [PostId], [Score], [Text], [UserId] 
FROM [dbo].[Comments] 
	WHERE EXISTS (SELECT [Id],[OwnerUserId] FROM [dbo].[Posts] 
		WHERE EXISTS (SELECT [Id] FROM [dbo].[Users] WHERE Id = 4918  AND [dbo].[Users].[Id] = [dbo].[Posts].[OwnerUserId])  AND [dbo].[Posts].[Id] = [dbo].[Comments].[PostId] AND [dbo].[Posts].[OwnerUserId] = [dbo].[Comments].[UserId]) 

-- PPN=[userId], CPN=[id], PC=Comment, CC=User, CS=false, JP=user, PQ=true}
SELECT [Id], [AboutMe], [Age], [CreationDate], [DisplayName], [DownVotes], [EmailHash], [LastAccessDate], [Location], [Reputation], 
	[UpVotes], [Views], [WebsiteUrl], [AccountId] 
FROM [dbo].[Users] 
	WHERE EXISTS (SELECT [UserId] FROM [dbo].[Comments] 
		WHERE EXISTS (SELECT [Id],[OwnerUserId] FROM [dbo].[Posts] WHERE EXISTS (SELECT [Id] FROM [dbo].[Users] WHERE Id = 4918  AND [dbo].[Users].[Id] = [dbo].[Posts].[OwnerUserId])  AND [dbo].[Posts].[Id] = [dbo].[Comments].[PostId] AND [dbo].[Posts].[OwnerUserId] = [dbo].[Comments].[UserId])  AND [dbo].[Comments].[UserId] = [dbo].[Users].[Id]) 

-- PPN=[postTypeId], CPN=[id], PC=ExtendedPost, CC=PostType, CS=false, JP=postType, PQ=true}
SELECT [Id], [Type] 
FROM [dbo].[PostTypes] 
	WHERE EXISTS (SELECT [PostTypeId] FROM [dbo].[Posts] 
		WHERE EXISTS (SELECT [Id] FROM [dbo].[Users] WHERE Id = 4918  AND [dbo].[Users].[Id] = [dbo].[Posts].[OwnerUserId])  AND [dbo].[Posts].[PostTypeId] = [dbo].[PostTypes].[Id]) 

-- PPN=[ownerUserId], CPN=[id], PC=ExtendedPost, CC=User, CS=false, JP=user, PQ=true}
SELECT [Id], [AboutMe], [Age], [CreationDate], [DisplayName], [DownVotes], [EmailHash], [LastAccessDate], [Location], [Reputation], 
	[UpVotes], [Views], [WebsiteUrl], [AccountId] 
FROM [dbo].[Users] 
	WHERE EXISTS (SELECT [OwnerUserId] FROM [dbo].[Posts] 
		WHERE EXISTS (SELECT [Id] FROM [dbo].[Users] WHERE Id = 4918 AND [dbo].[Users].[Id] = [dbo].[Posts].[OwnerUserId])  AND [dbo].[Posts].[OwnerUserId] = [dbo].[Users].[Id]) 

-- PPN=[parentId], CPN=[id], PC=ExtendedPost, CC=Post, CS=false, JP=parentPost, PQ=true}
SELECT [Id], [OwnerUserId], [ParentId], [AcceptedAnswerId], [AnswerCount], [Body], [ClosedDate], [CommentCount], [CommunityOwnedDate], [CreationDate], [FavoriteCount], 
	[LastActivityDate], [LastEditDate], [LastEditorDisplayName], [LastEditorUserId],  [PostTypeId], [Score], [Tags], [Title], [ViewCount] 
FROM [dbo].[Posts] 
	WHERE EXISTS (SELECT [ParentId] FROM [dbo].[Posts] P 
		WHERE EXISTS (SELECT [Id] FROM [dbo].[Users] WHERE Id = 4918  AND [dbo].[Users].[Id] = [dbo].[Posts].[OwnerUserId])  AND P.[ParentId] = [dbo].[Posts].[Id]) 

-- PPN=[id], CPN=[userId], PC=ExtendedUser, CC=Badge, CS=false, JP=badges, PQ=false}
SELECT [Id], [Name], [UserId], [Date] 
FROM [dbo].[Badges] 
	WHERE EXISTS (SELECT [Id] FROM [dbo].[Users] WHERE Id = 4918 AND [dbo].[Users].[Id] = [dbo].[Badges].[UserId]) 

-- PPN=[id], CPN=[userId], PC=ExtendedUser, CC=Vote, CS=false, JP=votes, PQ=false}
SELECT [Id], [PostId], [UserId], [BountyAmount], [VoteTypeId], [CreationDate] 
FROM [dbo].[Votes] 
	WHERE EXISTS (SELECT [Id] FROM [dbo].[Users] WHERE Id = 4918  AND [dbo].[Users].[Id] = [dbo].[Votes].[UserId]) 

*/