# 1. Preface #
  1. Basically the server runs Restful ways. HTTP POST for new record, HTTP PUT for update, HTTP DELETE for delete.
  1. But for simplicity this uses POST methods heavily. and in very very rare cases you need to user HTTP GET for "logout' or other stuffs.
  1. The HTTP response of server conforms to that of ordinary HTTP standard. For ex, 200 is for OK, 4xx for client error, 5xx for server errors.
  1. If there is no unexpected error on the server side, the server will always try to respond in JSON format.



# 2. Register new user #


## API URL ##
> /api/v1/users.json 
## HTTP cmd ##
> > POST
## Purposes ##
> > Register new user
## Parameters ##
    1. when using 'email' authentication
      * email : email address of new user
      * password : password of new user
    1. when using 'facebook' authentication
      * facebook\_id : Facebook ID of new user
      * name : name of new user
      * image\_url : URL of profile image
Result
    * HTTP 200  : OK
      * Json format. Informtaion will be included in "data" field.
      * ` {"data":{"email":"a5@test.com","fb_user_id":null,"name":null}} `
    * HTTP 4XX : Client Error
    * HTTP 5XX : Server Error
      * Json format. The error message will be included in "data" field.
      * ` {"data":{"email":["has already been taken"]}} `










# 3. Get Access Token (Log in) #


## API URL ##

> /api/v1/tokens.json
## HTTP cmd ##
> POST
## Purposes ##
> 'Token' is the key value for identifying a user and you will need to provide it to almost all APIs described here.
> And you will get that token value by calling this API.
> This API will cause the server to generate the 'session' for the user so this is actually provides 'log in' function.
## Parameters ##
    1. when using email
      * email : email address of user
      * password : password of user
    1. when using facebook
      * facebook\_id : The Facebook ID of user
## Result ##
    1. HTTP 200  : OK
> > > {"token":"QRbjzCfs1GWvWSgCa4zs"}
> > > Currently the server will keep the issued token values even if you log out, so you can reuse it in your client program.
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `












# 4. Log out #


## API URL ##

> /logout
## HTTP cmd ##
> GET
## Purposes ##
> Log out from the server.
> For example, if you need to login as a different user then you need to call this first and re-login.
## Parameters ##
> 필요 없음.
## Result ##
    1. HTTP 302  : OK (The page will be redirected to default login page)
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `











# 5. List of users current user is following #


## API URL ##

> /api/v1/users/following.json
## HTTP cmd ##
> POST
## Purposes ##
> Get the list of users current user is following.
## Parameters ##
    1. auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    1. only\_accepted (optional)
      * true : Server will return ONLY the information of users who accepted the 'follow request' from current user.
      * false : Server will return ALL the information of users who the current user sent 'follow request' to.
## Result ##
    1. HTTP 200  : OK
> > > The "Data" field of this JSON message will contain the information of users.
```
{
  "data": [
    {
      "accepted": true,
      "user": {
        "_id": "506e64046a87457505000002",
        "created_at": "2012-10-05T04:37:24Z",
        "email": "a2@test.com",
        "fb_user_id": "1726051469",
        "name": "Bruce Wang",
        "remote_profile_image": "https://fbcdn-profile-a.akamaihd.net/hprofile-ak-snc6/273525_1726051469_471046448_q.jpg",
        "updated_at": "2012-10-09T09:31:09Z",
        "avatar_url": {
          "thumbnail": "/uploads/users/506e/6404/6a87/4575/0500/0002/thumbnail/imgprofile.jpg?1349775069",
          "original": "/uploads/users/506e/6404/6a87/4575/0500/0002/original/imgprofile.jpg?1349775069"
        }
      }
    }
  ]
}
```
> > > if ‘only\_accepted’ parameter is set 'true', you will not see the users whose 'accepted' field is 'false'.
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `}











# 6. The list of users who follow the current user #


## API URL ##

> /api/v1/users/followers.json
## HTTP cmd ##
> POST
## Purposes ##
> Get the list of users who follow the current user
## Parameters ##
    1. auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    1. only\_accepted (optional)
      * true : Server will return ONLY the information of users who the current user accepted the 'follow request' from.
      * false : Server will return ALL the information of users who sent the 'follow request' to current user.
## Result ##
    1. HTTP 200  : OK
> > > The "Data" field of this JSON message will contain the information of users.
```
{
  "data": [
    {
      "accepted": true,
      "user": {
        "_id": "506e64046a87457505000002",
        "created_at": "2012-10-05T04:37:24Z",
        "email": "a2@test.com",
        "fb_user_id": "1726051469",
        "name": "Bruce Wang",
        "remote_profile_image": "https://fbcdn-profile-a.akamaihd.net/hprofile-ak-snc6/273525_1726051469_471046448_q.jpg",
        "updated_at": "2012-10-09T09:31:09Z",
        "avatar_url": {
          "thumbnail": "/uploads/users/506e/6404/6a87/4575/0500/0002/thumbnail/imgprofile.jpg?1349775069",
          "original": "/uploads/users/506e/6404/6a87/4575/0500/0002/original/imgprofile.jpg?1349775069"
        }
      }
    }
  ]
}
```
> > > if ‘only\_accepted’ parameter is set 'true', you will not see the users whose 'accepted' field is 'false'.
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `











# 7. The number of users who the current user follows #


## API URL ##

> /api/v1/users/following\_count.json
## HTTP cmd ##
> POST
## Purposes ##
> Get the number of users who the current user follows.
## Parameters ##
    1. auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    1. only\_accepted (optional)
      * true : The server will return ONLY the number of users who accepted 'follow request'.
      * false : The server will return the number of users who current user sent 'follow request'.
## Result ##
    1. HTTP 200  : OK
> > > ` {"data":2} `
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `









# 8. The number of users who follow the current user #


## API URL ##

> /api/v1/users/followers\_count.json
## HTTP cmd ##
> POST
## Purposes ##
> Get the number of users who follow the current user
## Parameters ##
    1. auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    1. only\_accepted (optional)
      * true : Get the number of users who accepted the 'follow request' from current user.
      * false : Get the number of users the current user sent the 'follow request' to.
## Result ##
    1. HTTP 200  : OK
> > > ` {"data":2} `
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `







# 9. Send the 'Follow' request #


## API URL ##

> /api/v1/users/follow.json
## HTTP cmd ##
> POST
## Purposes ##
> The current user sends the 'Follow' request to specific user.
## Parameters ##
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * to : The ID of user to follow.
## Result ##
    1. HTTP 200  : OK
> > > {"data":"ok"}
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `









# 10. Get the list of 'follow requests' #


## API URL ##

> /api/v1/users/follow\_requests.json
## HTTP cmd ##
> POST
## Purposes ##
> Get the list of 'follow requests' to current user.
## Parameters ##
    1. auth\_token: the Access Token string returned by ‘tokens.json’ API call.
## Result ##
    1. HTTP 200  : OK
```
{
  "data": [
    {
        "_id": "506e64046a87457505000002",
        "created_at": "2012-10-05T04:37:24Z",
        "email": "a2@test.com",
        "fb_user_id": "1726051469",
        "name": "Bruce Wang",
        "remote_profile_image": "https://fbcdn-profile-a.akamaihd.net/hprofile-ak-snc6/273525_1726051469_471046448_q.jpg",
        "updated_at": "2012-10-09T09:31:09Z",
        "avatar_url": {
          "thumbnail": "/uploads/users/506e/6404/6a87/4575/0500/0002/thumbnail/imgprofile.jpg?1349775069",
          "original": "/uploads/users/506e/6404/6a87/4575/0500/0002/original/imgprofile.jpg?1349775069"
      }
    }
  ]
}
```
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `









# 11. Cancel following #


## API URL ##

> /api/v1/users/unfollow.json
## HTTP cmd ##
> POST
## Purposes ##
> Current user stops to follow specific user.
## Parameters ##
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * to : The ID of target user to un-follow.
## Result ##
    1. HTTP 200  : OK
> > > ` {"data":"ok"} `
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `










# 12. Accept the 'follow request' #

## API URL ##

> /api/v1/users/accept\_follow.json
## HTTP cmd ##
> POST
## Purposes ##
> Accept the 'follow request' from other user.
## Parameters ##
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * to : The user id who sent the 'follow request' to current user.
## Result ##
    1. HTTP 200  : OK
> > > ` {"data":"ok"} `
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `








# 13. Reject the 'follow request' #


## API URL ##

> /api/v1/users/reject\_follow.json
## HTTP cmd ##
> POST
## Purposes ##
> Reject the 'follow request'. The peer person will think the follow request is not accepted yet.
> And the follow request from that peer person will not be shown to the current user.
> Blocked user will not be able to see the current user's posts, the rejected users can see the posts but will not received the feed from current user.
## Parameters ##
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * to : The user id who sent the 'follow request' to current user.
## Result ##
    1. HTTP 200  : OK
> > > ` {"data":"ok"} `
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `











# 14. Block a specific user #

## API URL ##

> /api/v1/users/block\_follow.json
## HTTP cmd ##
> POST
## Purposes ##
> To block a specific user.
> > Blocked user will not be able to see the current user's posts, the rejected users can see the posts but will not received the feed from current user.
## Parameters ##
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * to : The id of user to block
## Result ##
    1. HTTP 200  : OK
> > > ` {"data":"ok"} `
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `








# 15. Cancel the block #

## API URL ##

> /api/v1/users/unblock\_follow.json
## HTTP cmd ##
> POST
## Purposes ##
> Cancel the block to a specific user.
## Parameters ##
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * to : The id of user to un-block
## Result ##
    1. HTTP 200  : OK
> > > ` {"data":"ok"} `
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `






# 16. Create a movie post. #
## API URL ##

> /api/v1/posts
## HTTP cmd ##
> POST
## Purposes ##
> Upload a movie file and create a new post.
## Parameters ##
    1. You should use 'HTTP multipart' request to post 'form data'.
      * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
      * ` post[description] `: description to the post.
      * ` post[scope] `:
        * “public” : open to all users.
        * “friends” : open only to the friends
        * “onlyme”  : only I can see the post
        * “restricted” : only to the specified users. You should specify the list of users to the ‘recipients’ parameter.
      * ` post[recipients] ` : Only valid when ‘scope’ parameter is set to 'restricted'.
> > > > You just need to add IDs of users who could see this post like following sample.
```
post[recipients][]=fsoi13413e19341klfadf
post[recipients][]=32edfjk24r213rklqef81
post[recipients][]=sdf2490rf243rkrf2eq14
```
      * ` post[movie_attributes][name] `: Name of the movie.
    * When the movie is link to other site.
      * ` post[movie_attributes][remote_source] ` : Name of the server where original movie clip is.
> > > > ex) YouTube
      * ` post[movie_attributes][remote_url] `:
> > > > The URL of playable movie.
> > > > 예) "http://www.youtube.com/v/2rCP4CRRO7E&feature=youtube_gdata_player"
      * ` post[movie_attributes][thumbnails] `:
> > > > The URL of thumbnail images
> > > > Specify them in Array shape like following.
```
[“http://www.a.com/a.png”,”...”,...]
```
    * When uploading a movie file directly.
      * ` post[movie_attributes][file] ` : Name of the file to upload
> > > > You should send the binary file data into this part using 'HTTP Multipart'.
## Result ##
    1. HTTP 200  : OK
```
{
  "data": [
    {
      "_id": "507291096a8745f40300002b",
      "created_at": "2012-10-08T08:38:33Z",
      "description": "test descripion",
      "hashtags": null,
      "like_count": 0,
      "recipients": [
        "Afafadfadf",
        "BBBBB"
      ],
      "referred_users": null,
      "scope": "public",
      "updated_at": "2012-10-08T08:38:33Z",
      "user_id": "506e64046a87457505000002",
      "view_count": 0,
      "user": {
        "name": "dfafafad",
        "email": "a2@test.com",
        "fb_user_id": "1726051469",
        "avatar_url": {
          "thumbnail": "/uploads/../a.jpg",
          "original": "/uploads/../a.jpg"
        }
      },
      "movie": {
        "_id": "507291096a8745f40300002c",
        "created_at": "2012-10-08T08:38:33Z",
        "file_content_type": "",
        "file_file_name": "VID_20120907_214125.mp4",
        "file_file_size": 8169968,
        "file_updated_at": "2012-10-08T08:38:33+00:00",
        "name": "test name",
        "play_count": 0,
        "post_id": "507291096a8745f40300002b",
        "processing": false,
        "remote_source": null,
        "remote_url": null,
        "thumbnails": [
          “http://a.com/a.png”,“http://a.com/b.png”
        ],
        "updated_at": "2012-10-08T08:38:46Z",
        "file_url": {
          "mp4": "/uploads/movies/5072/9109/6a87/45f4/0300/002c/mp4/VID_20120907_214125.mp4",
          "flv": "/uploads/movies/5072/9109/6a87/45f4/0300/002c/flv/VID_20120907_214125.flv",
          "thumbnail": "/uploads/movies/5072/9109/6a87/45f4/0300/002c/thumbnail/VID_20120907_214125.png"
        }
      }
    }
  ]
}
```
      * _id : The id of post
      * created\_at : created time
      * description : The message
      * hashtags : List of Tags
      * recipients : If the value of ’scope’ is ‘restricted’, the list of user ids will be here.
      * referred\_users : ID of users mentioned(@)
      * scope : Publication scope of this post.
        * 'public'  : This post can be seen by all users. But the feeds will be created only to the friends (Followers).
        * 'friends' : Friends(Follwer/Following) can see this post, and the feed will be created for the followers.
        * 'onlyme'  : Only the creator can see this post.
        * 'restricted' : Only the specified users can see the post.
      * updated\_at : Time when this post updated.
      * user\_id :	The id of user created this post.
      * user.name :	Name of the user
      * user.email :	E-mail address of the user.
      * user.fb\_user\_id :	Facebook id of the user.
      * user.avatar\_thumbnail :	The URL of the thumbnail profile image.
      * user.avatar\_original :	The URL of the original profile image.
      * movie.created\_at :	Time when this movie created.
> > > > If the movie is just the link, this field will be null.
      * movie.file\_content\_type
> > > > File type of the movie (MIME TYPE)
> > > > If the movie is just the link, this field will be null.
      * movie.file\_file\_name : Name of the movie file itself.
> > > > If the movie is just the link, this field will be null.
      * movie.file\_file\_size : The size of the movie file
> > > > If the movie is just the link, this field will be null.
      * movie.file\_updated\_at :	Time when the movie file is updated.
> > > > If the movie is just the link, this field will be null.
      * movie.name : Name of the movie (title)
      * movie.post\_id :	The id of the Post which contains this movie.
      * movie.processing :
> > > > The uploaded movie file will be transcoded to a mp4 and flv file and it requires time.
> > > > So initially this flag will be set to 'true', saying it is still in process
> > > > and if the transcoding job is done, this flag will be set to 'false'.
      * movie.remote\_source :	If the movie is just the link, this field contains the name of the service provider. ex) YouTube
      * movie.remote\_url :	If the movie is just the link, The URL of the playable movie.
      * movie.thumbnails :	The URL of thumbnail images of the movie.
      * movie.updated\_at :	Time when the movie information is updated.
      * movie.file\_url :	The URL of mp4,flv,thumbnail.
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error

> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `_











# 17. List of Friends' Posts #


## API URL ##

> /api/v1/posts/of\_friends.json
## HTTP cmd ##
> GET
## Purposes ##
> Get the list of Friends' Posts.
## Parameters ##
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * page :
> > > The page you need to get.
> > > The index starts from 0.
> > > You will get 20 records per page.
    * get\_count (optional):
> > > If this parameter is 'true', you will get only the number of records.
## Result ##
    1. HTTP 200  : OK
```
{“data”:
  [
   {
     "_id"=>"505fcde26a87453d1500001d", 
     "created_at"=>Mon, 24 Sep 2012 03:05:06 UTC +00:00,
     "description"=>"test", 
     "hashtags"=>nil, 
     "recipients"=>[],
     "referred_users"=>nil, 
     "scope"=>"friends",
     "updated_at"=>Mon, 24 Sep 2012 03:05:06 UTC +00:00,
     "user_id"=>"4",
     “user” => {...},
     “movie” => {...}
   }
   ,
   ….
  ] 
}
```
> > > If ‘get\_count’ parameter is set and it's 'true', you will get the number of records.` {“data”:200} `
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `












# 18. List of popular Posts #


## API URL ##

> /api/v1/posts/popular.json
## HTTP cmd ##
> GET
## Purposes ##
> Get the list of popular Posts.
## Parameters ##
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * 
    * page :
> > > The page you need to get.
> > > The index starts from 0.
> > > You will get 20 records per page.
    * get\_count (optional):
> > > If this parameter is 'true', you will get only the number of records.
## Result ##
    1. HTTP 200  : OK
```
{“data”:
  [
   {
     "_id"=>"505fcde26a87453d1500001d", 
     "created_at"=>Mon, 24 Sep 2012 03:05:06 UTC +00:00,
     "description"=>"test", 
     "hashtags"=>nil, 
     "recipients"=>[],
     "referred_users"=>nil, 
     "scope"=>"friends",
     "updated_at"=>Mon, 24 Sep 2012 03:05:06 UTC +00:00,
     "user_id"=>"4",
     “user” => {...},
     “movie” => {...}
   }
   ,
   ….
  ] 
}
```
> > > If ‘get\_count’ parameter is set and it's 'true', you will get the number of records.` {“data”:200} `
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `











# 19. List of all Posts #


## API URL ##

> /api/v1/posts.json
## HTTP cmd ##
> GET
## Purposes ##
> Get the list of all Posts
## Parameters ##
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * 
    * page :
> > > The page you need to get.
> > > The index starts from 0.
> > > You will get 20 records per page.
    * get\_count (optional):
> > > If this parameter is 'true', you will get only the number of records.
## Result ##
    1. HTTP 200  : OK
```
{“data”:
  [
   {
     "_id"=>"505fcde26a87453d1500001d", 
     "created_at"=>Mon, 24 Sep 2012 03:05:06 UTC +00:00,
     "description"=>"test", 
     "hashtags"=>nil, 
     "recipients"=>[],
     "referred_users"=>nil, 
     "scope"=>"friends",
     "updated_at"=>Mon, 24 Sep 2012 03:05:06 UTC +00:00,
     "user_id"=>"4",
     “user” => {...},
     “movie” => {...}
   }
   ,
   ….
  ] 
}
```
> > > If ‘get\_count’ parameter is set and it's 'true', you will get the number of records.` {“data”:200} `
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `










# 20. Feeds #


## API URL ##

> /api/v1/action\_feeds.json
## HTTP cmd ##
> GET
## Purposes ##
> > Feed is the list of the Activities made by users.
> > This will return the recent activities of friends.
## Parameters ##
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * page :
> > > The page you need to get.
> > > The index starts from 0.
> > > You will get 20 records per page.
    * get\_count (optional):
> > > If this parameter is 'true', you will get only the number of records.
## Result ##
    1. HTTP 200  : OK
```
{“data”:
  [
   {
     "_id"=>"505fcde26a87453d1500001d", 
     "reader_id"=>1,
     "action"=>{
       "actor_id"=>1, 
       "action_type"=>0, 
       "target_type"=>0,
       "target_id"=>nil,
       “user” => {...},
       “movie” => {...}
     }
   }
   ,
   ….
  ] 
}
```
      * _id : The id of Feed.
      * reader\_id : ID of User who will see this Feed. It should be the same as that of current user.
      * action : The Action related to this feed.
        * action.action\_id : The id of Action
        * action.action\_type : Type of this Action.
          * 0: 'Comment'
          * 1: 'Like/Dislike'
          * 2: 'Follow request' (accept/reject/block needn't be shown as feed)
          * 3: 'Play movie'
          * 4: 'Post Movie'
        * action.target\_type : type of the target of the Action.
          * 0 : Post
          * 1 : Comment
        * action.target\_id : The id of target of Action. You need to check the 'target\_type' for matching object in the Database.
      * user : user Information. (For details refer to "16. Create a movie post" section)
      * movie : movie information. (For details refer to "16. Create a movie post" section)
> > > If ‘get\_count’ parameter is set and it's 'true', you will get the number of records.` {“data”:200} `
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `_














# 21. Write comment to a Post. #


## API URL ##

> /api/v1/posts/create\_comment.json
## HTTP cmd ##
> POST
## Purposes ##
> Write comment to a Post
## Parameters ##
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * description : the content of the comment
    * post\_id : Id of post to write comment
    * reply\_to\_id : If this is a comment to another comment, the ID of that comment
## Result ##
    1. HTTP 200  : OK
```
{“data”:
   {
     "_id"=>"505fcde26a87453d1500001d", 
     "user_id"=>1, 
     "description"=>”testetest”,
     "post_id"=>"505fcde26a87453d1500001d", 
     "reply_to_id"=>"505fcde26a87453d1500001d", , 
     "created_at"=>Mon, 24 Sep 2012 03:05:06 UTC +00:00,
     "updated_at"=>Mon, 24 Sep 2012 03:05:06 UTC +00:00
     }
   }
}
```
      * _id : ID of this new comment
      * user\_id : ID of user who wrote comment.
      * description : content of comment
      * post\_id : Id of post where this comment attached to
      * reply\_to\_id : If this is a comment to another comment, the id of that comment will be here.
      * created\_at : Time when this comment created.
      * updated\_at : Time when this comment updated.
> > > ...
> > > ...
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `_













# 22. Delete comment #


## API URL ##

> /api/v1/posts/delete\_comment.json
## HTTP cmd ##
> POST
## Purposes ##
> Delete a specific comment.
## Parameters ##
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * id : ID of comment to delete.
## Result ##
    1. HTTP 200  : OK
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `












# 23. List of comments to a post. #


## API URL ##

> /api/v1/posts/comments.json
## HTTP cmd ##
> POST
## Purposes ##
> Get the list of comments to a post.
## Parameters ##
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * id : Id of post to check.
    * page : the specific page want to see from the all results.
## Result ##
    1. HTTP 200  : OK
```
{
  "data": [
    {
      "_id": "507659556a87458a21000001",
      "created_at": "2012-10-11T05:29:57Z",
      "description": "Fucking Funny",
      "post_id": "507291096a8745f40300002b",
      "reply_to_id": null,
      "updated_at": "2012-10-11T05:29:57Z",
      "user_id": "506e632f6a87457505000001",
      "user": {
          "_id": "506e64046a87457505000002",
          "created_at": "2012-10-05T04:37:24Z",
          "email": "a2@test.com",
          "fb_user_id": "1726051469",
          "name": "dfafafad",
          "updated_at": "2012-10-09T01:42:15Z",
          "avatar_url": {
            "thumbnail": "/uploads/../a.jpg",
            "original": "/uploads/../a.jpg"
          }
        }
    },
    {
      "_id": "50765bfa6a87458a21000005",
      "created_at": "2012-10-11T05:41:14Z",
      "description": "Fucking Funny",
      "post_id": "507291096a8745f40300002b",
      "reply_to_id": null,
      "updated_at": "2012-10-11T05:41:14Z",
      "user_id": "506e632f6a87457505000001",
      "user": {
          "_id": "506e64046a87457505000002",
          "created_at": "2012-10-05T04:37:24Z",
          "email": "a2@test.com",
          "fb_user_id": "1726051469",
          "name": "dfafafad",
          "updated_at": "2012-10-09T01:42:15Z",
          "avatar_url": {
            "thumbnail": "/uploads/../a.jpg",
            "original": "/uploads/../a.jpg"
          }
        }
    }
  ]
}
```
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `










# 24. Like Post #


## API URL ##

> /api/v1/posts/like\_post.json
## HTTP cmd ##
> POST
## Purposes ##
> Record 'like' to a post.
## Parameters ##
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * id : ID of post to 'like'.
## Result ##
    1. HTTP 200  : OK
> > > ` {"data":"ok"} `
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"You already liked it"} `









# 25. Like Comment #


## API URL ##

> /api/v1/posts/like\_comment.json
## HTTP cmd ##
> POST
## Purposes ##
> Record 'like' to a comment.
## Parameters ##
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * id : ID of comment to like
## Result ##
    1. HTTP 200  : OK
> > > ` {"data":"ok"} `
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"You already liked it"} `











# 26. Cancel ‘like’. #


## API URL ##

> /api/v1/posts/unlike.json
## HTTP cmd ##
> POST
## Purposes ##
> Cancel ‘like’
## Parameters ##
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * type: “post” or “comment”
    * id : ID of “post” or “comment” to cancel 'like'.
## Result ##
    1. HTTP 200  : OK
> > > ` {"data":"ok"} `
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `








# 27. Change user information. #


## API URL ##

> /api/v1/users/:id
## HTTP cmd ##
> PUT
## Purposes ##
> Change the information of user specified by (:id).
## Parameters ##
> > You need to use HTTP multipart to send form data to server.
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * ` user[name] `: Name of user
    * ` user[avatar] `: Profile image data.
## Result ##
    1. HTTP 200  : OK
```
 {
  "data": {
    "_id": "506e64046a87457505000002",
    "avatar_content_type": "image/png",
    "avatar_file_name": "smile_big_icon2.png",
    "avatar_file_size": 33856,
    "avatar_updated_at": "2012-10-09T01:42:14+00:00",
    "created_at": "2012-10-05T04:37:24Z",
    "email": "a2@test.com",
    "fb_user_id": "1726051469",
    "name": "dfafafad",
    "updated_at": "2012-10-09T01:42:15Z"
  }
}
```
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.









# 28. Get user info. #


## API URL ##

> /api/v1/users/:id
## HTTP cmd ##
> GET
## Purposes ##
> Get the information of a user specified by (:id).
## Parameters ##
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * 
## Result ##
    1. HTTP 200  : OK
```
{
  "data": {
    "_id": "506e64046a87457505000002",
    "created_at": "2012-10-05T04:37:24Z",
    "email": "a2@test.com",
    "fb_user_id": "1726051469",
    "name": "dfafafad",
    "updated_at": "2012-10-09T01:42:15Z",
    "avatar_url": {
      "thumbnail": "/uploads/../a.jpg",
      "original": "/uploads/../a.jpg"
    }
  }
}
```
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `










# 29. Get Post info. #


## API URL ##

> /api/v1/posts/show.json
## HTTP cmd ##
> POST
## Purposes ##
> Get the detailed information about a Post.
## Parameters ##
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * id : ID of Post to check.
## Result ##
    1. HTTP 200  : OK
```
{
  "data": {
    "_id": "50a06792921e00e226000001",
    "comment_count": 1,
    "created_at": "2012-11-12T03:05:54Z",
    "description": "ddxx",
    "hashtags": null,
    "like_count": 1,
    "recipients": [
      "Afafadfadf",
      "BBBBB"
    ],
    "referred_users": null,
    "scope": "public",
    "updated_at": "2012-11-20T01:03:27Z",
    "user_id": "50a0613e921e00ad23000001",
    "view_count": 3,
    "movie": {
      "_id": "50a06792921e00e226000002",
      "created_at": "2012-11-12T03:05:54Z",
      "file_content_type": "",
      "file_file_name": "VID_20120907_214125.mp4",
      "file_file_size": 8169968,
      "file_updated_at": "2012-11-12T03:05:54+00:00",
      "name": "dddd",
      "play_count": 0,
      "post_id": "50a06792921e00e226000001",
      "processing": false,
      "remote_source": null,
      "remote_url": null,
      "thumbnails": [
        
      ],
      "updated_at": "2012-11-12T03:06:03Z"
    },
    "comments": [
      {
        "_id": "50aad3e2921e009a45000001",
        "created_at": "2012-11-20T00:50:42Z",
        "description": "\u314e\u314e\u314e",
        "post_id": "50a06792921e00e226000001",
        "reply_to_id": null,
        "updated_at": "2012-11-20T00:50:42Z",
        "user_id": "50a0613e921e00ad23000001"
      }
    ],
    "likes": [
      {
        "_id": "50aad690921e009a45000004",
        "comment_id": null,
        "created_at": "2012-11-20T01:02:08Z",
        "post_id": "50a06792921e00e226000001",
        "updated_at": "2012-11-20T01:02:08Z",
        "user_id": "50a0613e921e00ad23000001"
      }
    ]
  }
}
```
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `









# 30. Find Facebook friends #


## API URL ##

> /api/v1/users/find\_by\_fbid.json
## HTTP cmd ##
> POST
## Purposes ##
> Find the Facebook friends who are already using ChocoCam service.
> The client will send the list of Facebook user id of current user's Facebook friends
> and the server will search the Database records to find users with a specified Facebook Id and return the list of user information found.
> The returned users are already using the service so the current user can send 'follow request' to them.
## Parameters ##
    * auth\_token: the Access Token string returned by ‘tokens.json’ API call.
    * 
    * ` fb_ids[] `: List of Facebook ids of current user's Facebook friends.
> > > You just need to add the Facebook ids to request parameter like this.
```
fb_ids[]=fsoi13413e19341klfadf
fb_ids[]=32edfjk24r213rklqef81
fb_ids[]=sdf2490rf243rkrf2eq14
```
## Result ##
    1. HTTP 200  : OK
```
{"data":[{"_id":"50a0613e921e00ad23000001","fb_user_id":"1726051469"}]}
```
    1. HTTP 4XX : Client Error
    1. HTTP 5XX : Server Error
> > > Json format. The error message will be included in "data" field.
> > > ` {"data":"user not found"} `
