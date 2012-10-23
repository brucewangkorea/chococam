class Api::V1::PostsController < ApplicationController

  skip_before_filter :verify_authenticity_token
  before_filter :authenticate_user! #, :except=>[:create]
  
  
  
  
  # 2012-09-13 brucewang
  # 모든 post들을 반환.
  # GET http://localhost:3000/api/v1/posts.json?auth_token=vWLsJF4au1StrEC7nPpf&page=0
  # 파라미터로 get_count=true 를 지정하면 총 레코드 카운트만 반환.
  def index
    page = params[:page] || 0
    getcount = params[:get_count]=="true"
    
    if getcount
    	count = Post.count
      render :json=>{:data=>count}
    else
    	@posts = Post.page(page).entries
      render :json=>{:data=>@posts.as_json( :include=>:movie )}
    end
  end
  
  
  
  
 

  # 2012-09-26 brucewang
  # 인기 포스트들의 목록을 반환
  # 우선 검색 대상은 scope이 public인 것들.
  # 그 다음으로  like 나 view 카운트가 높은 것들..
  # 또는,  Movie 의 play count가 높은 것들...
  # limit 25개..
  def popular
    page = params[:page] || 0
    getcount = params[:get_count]=="true"

    #p = Post.where( :scope=>"public" ).descending(:like_count).page(page)
    p = Post.where( :scope=>"public" ).descending( :'movie.play_count' ).descending(:created_at).page(page)

    if getcount
      render :json=>{:data=>p.count}
    else
      render :json=>{:data=>p.entries.as_json( :include=>{:movie=>{:methods=>:file_url}}, :methods=>:user )}
    end

  end

  
  # 2012-09-13 brucewang
  # 현재 사용자가 following 하는 사람들의 post들을 반환.
  # GET http://localhost:3000/api/v1/posts/of_friends.json?auth_token=vWLsJF4au1StrEC7nPpf&page=0
  # 파라미터로 get_count=true 를 지정하면 총 레코드 카운트만 반환.
  def of_friends
  	# 현재사용자가 follow하고 있는 사람들의 목록 취득.
    page = params[:page] || 0
    getcount = params[:get_count]=="true"
    
    
    only_accepted = true
    followings = Follow.following(current_user.id, only_accepted) 
    array_users_following = []
    followings.each{ |f|
    	array_users_following << f[0]
    }
    
    
    if getcount
    	count = Post.any_of( 
        {
          # 친구, 또는 공개로 되어 있는 포스트
          :scope.in => ["public","friends"], 
          # 현재 사용자의 친구들 (자신이 following하는) id 목록... 이사람들의 post를 찾는것이다.
          :user_id.in=>array_users_following # ["1","2"]
        }, 
        # 또는...
        {
          # 이 피드를 보게 될 사용자의 id 목록중에 현재 사용자 id가 있다면...
          :recipients=>current_user.id  
        }
        ).count
      render :json=>{:data=>count}
    else      
    	@posts = Post.any_of( 
        {
          # 친구, 또는 공개로 되어 있는 포스트
          :scope.in => ["public","friends"], 
          # 현재 사용자의 친구들 (자신이 following하는) id 목록... 이사람들의 post를 찾는것이다.
          :user_id.in=>array_users_following # ["1","2"]
        }, 
        # 또는...
        {
          # 이 피드를 보게 될 사용자의 id 목록중에 현재 사용자 id가 있다면...
          :recipients=>current_user.id  
        }
        ).page(page).ascending(:created_at) #.count
        
      render :json=>{:data=>@posts.entries.as_json( :include=>{:movie=>{:methods=>:file_url}},:methods=>:user )}
    end
  rescue Mongoid::Errors::DocumentNotFound
    render :json=>{:data=>[]}
  end
  
  
  
  
  
  
  
  
  # 2012-09-13 brucewang
  # Web을 통해 새로운 Movie Post를 남길때 사용..
  # http://localhost:3000/api/v1/post/new.html
  def new
    @post = Post.new()
    @post.movie = Movie.new()
  end





  # 2012-09-13 brucewang
  # CURL 로는 다음과 같이 테스트
  # curl -F "auth_token=vWLsJF4au1StrEC7nPpf" -F "post[description]=testtest" -F "post[movie_attributes][file]=@./a.mp4" -F "post[movie_attributes][name]=apitest" "http://localhost:3000/api/v1/posts" 
  def create
    @post = Post.new(params[:post])
    @post.user_id = current_user.id
    saveresult = @post.save!
    
    # 2012-09-13 brucewang
    # 적절한 사용자들에 대해 Feed를 작성.
    #
    # Ex)
  	#     movie = Movie.new
  	#     movie.save
  	#     user_id="1"
  	#     description = "Adfagfadgadg"
  	#     scope = "friends"
  	#     post = Post.create_movie_post(user_id, movie, description, scope, recipients=[])
  	#     ActionFeed.create_movie_post_feed(post)
    ActionFeed.create_movie_post_feed(current_user.id, @post, Action::ACT_TYPE_POSTMOVIE, Action::TARGET_TYPE_POST, @post.id)
    
    logger.info "----------------------------"
    logger.info "Current user id : #{current_user.id}"
    logger.info "----------------------------"
    logger.info params
    logger.info "----------------------------"
    logger.info params[:post]
    logger.info "----------------------------"
    logger.info @post.inspect
    logger.info "----------------------------"
    logger.info @post.movie.inspect
    logger.info "----------------------------"
    
    if saveresult
      render :json=>{:data=>@post.as_json( :include=>:movie )}
    else
      # HTTP 550 은 Internal Server Error를 의미한다.
      render :status=>500, :json=>{:data=>@post.errors}
    end
  end
  
  
  
  def show
    @post = Post.find(params[:id])
    
    # 2012-09-26 brucewang
    # view count 증가.
    @post.view_count+=1
    @post.save!

    #@new_comment = Comment.new(:post=>@post)
    render :json=>{:data=>@post.as_json( :include=>[:movie, :comments, :likes] )}
  end
  
  
  # 2012-10-11 brucewang
  # 지정된 post에 대한 comment 목록을 반환.
  def comments
  	@post = Post.find(params[:id])
  	page = params[:page] || 0
    getcount = params[:get_count]=="true"
    
    result = @post.comments.page(page).entries
    result.each{ |c|
			c["user"] = User.find(c.user_id).as_json( :methods=>:avatar_url,  :except => [:avatar_file_size, :avatar_file_name, :avatar_content_type, :avatar_updated_at] ) 
		}
    render :json=>{:data=>
    	result.as_json
    }
  end
  
  # 2012-09-14 brucewang
  # Web에서 테스트 하기 위해 임시 생성.
  def create_or_update_comment
    unless params[:comment]["id"].nil?
      @comment = Comment.find(params[:comment]["id"]) 
    else
      @comment = Comment.new(params[:comment])
    end
    @comment.user_id = current_user.id
    @comment.description = params[:comment]["description"]
    if @comment.save!
      # 이 액션에 해당하는 Feed를 생성.
      @post = @comment.post
      ActionFeed.create_movie_post_feed(current_user.id, @post, Action::ACT_TYPE_COMMENT, Action::TARGET_TYPE_POST, @comment.id)
      
      render :json=>{:data=>@comment.as_json}
    else
      # HTTP 550 은 Internal Server Error를 의미한다.
      render :status=>500, :json=>{:data=>@comment.errors}
    end
  end
  
  # 2012-09-14 brucewang
  def create_comment
    @post = Post.find(params[:post_id])
    @comment = Comment.new( :description=>params[:description],:post_id=>params[:post_id] ,:reply_to_id=>params[:reply_to_id] )
    @comment.user_id = current_user.id
    if @comment.save!
    	# 2012-10-11 brucewang
	    @post.comment_count+=1
	    @post.save
	    
      # 이 액션에 해당하는 Feed를 생성.
      target_type = params[:reply_to_id].nil? ? Action::TARGET_TYPE_POST : Action::TARGET_TYPE_COMMENT
      target_id = @comment.id #params[:reply_to_id].nil? ? params[:post_id] : params[:reply_to_id]
      ActionFeed.create_movie_post_feed(current_user.id, @post, Action::ACT_TYPE_COMMENT, target_type, target_id)
      
      render :json=>{:data=>@comment.as_json}
    else
      # HTTP 550 은 Internal Server Error를 의미한다.
      render :status=>500, :json=>{:data=>@comment.errors}
    end
  end
  
  # 2012-09-14 brucewang
  def delete_comment
  
  	# 2012-10-12 brucewang
  	# 해당 ActionFeed도 모두 제거.
  	action = Action.where( :action_type => Action::ACT_TYPE_COMMENT, :target_id=>params[:id] ).first
  	unless action.nil?
  		ActionFeed.where( :action_id=>action._id ).delete_all
  		action.delete
  	end
  	
    @comment = Comment.find(params[:id])
    @post = @comment.post
    if @comment.delete
			unless @post.nil?
				# 2012-10-11 brucewang
				@post.comment_count+=1
				@post.save
			end
			
      render :json=>{:data=>"ok"}
    else
      # HTTP 550 은 Internal Server Error를 의미한다.
      render :status=>500, :json=>{:data=>@comment.errors}
    end
  end
  
  
  # 2012-09-14 brucewang
  def like_post
    if Like.where(:user_id=>current_user.id,:post_id=>params[:id]).count>0
      render :status=>406, :json=>{:data=>"You already liked it"}
      return
    end
    
    @post = Post.find(params[:id])
    @like = Like.new( :user_id=>current_user.id, :post=>@post)
    if @like.save
      # 2012-09-26 brucewang
      # 해당 포스트의 좋아요 카운트를 증가시킴.
      @post.like_count+=1
      @post.save

      # 이 액션에 해당하는 Feed를 생성.
      ActionFeed.create_movie_post_feed(current_user.id, @post, Action::ACT_TYPE_LIKE, Action::TARGET_TYPE_POST, @like.id)
      
      render :json=>{:data=>"ok"}
    else
      # HTTP 550 은 Internal Server Error를 의미한다.
      render :status=>500, :json=>{:data=>@like.errors}
    end
  end
  
  
  # 2012-09-14 brucewang
  def like_comment
    if Like.where(:user_id=>current_user.id,:comment_id=>params[:id]).count>0
      render :status=>500, :json=>{:data=>"You already liked it"}
      return
    end    
    @comment = Comment.find(params[:id])
    @like = Like.new( :user_id=>current_user.id, :comment=>@comment)
    if @like.save
      # 이 액션에 해당하는 Feed를 생성.
      @post = @comment.post
      @post.like_count+=1
      @post.save
      ActionFeed.create_movie_post_feed(current_user.id, @post, Action::ACT_TYPE_LIKE, Action::TARGET_TYPE_COMMENT, @like.id)
      
      render :json=>{:data=>"ok"}
    else
      # HTTP 550 은 Internal Server Error를 의미한다.
      render :status=>500, :json=>{:data=>@like.errors}
    end
  end
  
  # 2012-09-14 brucewang
  def unlike
  	case params[:type]
    when "post"
    	unlike_post(current_user.id, params[:id])
    when "comment"
	    unlike_comment(current_user.id, params[:id])
    else
    	render :json=>{:data=>"ok"}
    end
  end
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  # 2012-10-16 brucewang
  def unlike_post(userid, id)
  	@like = Like.where(:user_id=>userid,:post_id=>id).first
    if @like.nil?
      render :status=>406, :json=>{:data=>"record not found"}
      return
    end
    
    
    # 해당 ActionFeed도 모두 제거.
  	action = Action.where( :action_type => Action::ACT_TYPE_LIKE, :target_id=>@like.id ).first
  	unless action.nil?
  		ActionFeed.where( :action_id=>action._id ).delete_all
  		action.delete
  	end

    # 해당 포스트의 좋아요 카운트를 감소시킴.
    if  @like.post.like_count>0 && @like.comment.nil?
      @like.post.like_count-=1
      @like.post.save
    end
    
    @like.delete
    render :status=>200, :json=>{:data=>"ok"}
  rescue Mongoid::Errors::DocumentNotFound
    render :status=>500, :json=>{:data=>"Record not found"}
  end
  
  
  # 2012-10-16 brucewang
  def unlike_comment(userid, id)
	  @like = Like.where(:user_id=>userid,:comment_id=>id).first
    if @like.nil?
      render :status=>406, :json=>{:data=>"record not found"}
      return
    end
    
    # 해당 ActionFeed도 모두 제거.
  	action = Action.where( :action_type => Action::ACT_TYPE_LIKE, :target_id=>@like.id ).first
  	unless action.nil?
  		ActionFeed.where( :action_id=>action._id ).delete_all
  		action.delete
  	end
    
    # 해당 포스트의 좋아요 카운트를 감소시킴.
    if  @like.post.like_count>0 && @like.comment.nil?
      @like.post.like_count-=1
      @like.post.save
    end
    
    @like.delete
    render :status=>200, :json=>{:data=>"ok"}
  rescue Mongoid::Errors::DocumentNotFound
    render :status=>500, :json=>{:data=>"Record not found"}
  end
  
  


  

end
