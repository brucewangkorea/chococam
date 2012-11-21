class Api::V1::UsersController  < ApplicationController
  skip_before_filter :verify_authenticity_token, :only => [:create]
  before_filter :authenticate_user!, :except=>[:create]
  respond_to :json


  # 2012-09-06 brucewang
  # 새로운 유저 생성
  #
  #
  # 예를 들어 다음과 같이 호출하면 됨.
  # POST로 http://localhost:3000/api/v1/users.json 
  # Param : auth_token="adfadfadfadf"
  def create
    email = params[:email]
    password = params[:password]
    fb_user_id = params[:facebook_id]
    name = params[:name]
    image_url = params[:image_url]

    if !email.nil? and !password.nil?
      @user = User.new(:email=>email, :password=>password)
    elsif !fb_user_id.nil?
      # 2012-10-09 brucewang
      # 이미 존재하고 있는 사용자에대한 처리.
	    if User.where( fb_user_id: fb_user_id ).exists?
		    @user = User.find_by( :fb_user_id => fb_user_id )
		    @user.name = name
		    @user.image_url = image_url
	    else
	    	@user = User.new(:fb_user_id=>fb_user_id, :name=>name, :image_url=>image_url)
	    end
    else
      # 2012-09-06 brucewang
      # json response의 기본은, 성공이면 http 200을, 
      # 에러일 경우는 4xx/5xx 등의http response를 보내는것이 맞다.
      #
      # 다음의 Http error code list 를 참조하면,
      # http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
      # 4xx는 클라이언트쪽의 문제, 5xx는 서버쪽의 문제라고 보면 된다.
      # 
      # HTTP 406 : Not Acceptable
      render :status=>406, :json=>{:data=>"either email/password or fabok_id parameter must be set"}
      return
    end

    saveresult = @user.save
    if saveresult
      @user.ensure_authentication_token!
      render :json=>{:data=>@user.as_json}
    else
      # HTTP 550 은 Internal Server Error를 의미한다.
      render :status=>500, :json=>{:data=>@user.errors}
    end
  end



  # 2012-09-06 brucewang
  # 사용자 정보 반환.
  #
  def show
  	user = User.find( params[:id] )
    render :json=>{
    	:data=>user.as_json( 
    		:except => [:avatar_file_size, :avatar_file_name, :avatar_content_type, :avatar_updated_at],
    		:methods => :avatar_url
    		) 
    	}
  end


	def edit
  	@user = User.find( params[:id] )
  end
  
  
  def update
	  @user = User.find( params[:id] )
	  @user.update_attributes(params[:user])
    saveresult = @user.save!
    render :json=>{:data=>current_user.as_json}
  end





  # POST http://localhost:3000/api/v1/users/following.json
  # param: 
  #      auth_token="adadfa"
  #      only_accepted=true/false (optional)
  def following
  	# 2012-11-21 brucewang
  	# 파라미터 유효성 체크 (refs #277)
  	only_accepted = params[:only_accepted]
  	if( only_accepted!="true" && only_accepted!="false")
  		render :status=>500, :json=>{:data=>"Invalid parameter for only_accepted."}
  		return
  	end
    only_accepted = params[:only_accepted]=="true" || false
    
    array = Follow.following(current_user.id, only_accepted) 
    list = []
    array.each{ |a|
    	user = User.find a[0]
    	res = {}
    	res["accepted"] = a[1]
    	res["user"]= user.as_json( 
    		:except => [:avatar_file_size, :avatar_file_name, :avatar_content_type, :avatar_updated_at],
    		:methods => :avatar_url
    		) 
    	list << res
    }
    render :json=>{:data=>list.as_json}
  end

  def followers
    # 2012-11-21 brucewang
  	# 파라미터 유효성 체크 (refs #277)
  	only_accepted = params[:only_accepted]
  	if( only_accepted!="true" && only_accepted!="false")
  		render :status=>500, :json=>{:data=>"Invalid parameter for only_accepted."}
  		return
  	end
    only_accepted = params[:only_accepted]=="true" || false
    
    
    array = Follow.followers(current_user.id, only_accepted)
    list = []
    array.each{ |a|
    	user = User.find a[0]
    	res = {}
    	res["accepted"] = a[1]
    	res["user"]= user.as_json( 
    		:except => [:avatar_file_size, :avatar_file_name, :avatar_content_type, :avatar_updated_at],
    		:methods => :avatar_url
    		) 
    	list << res
    }
    render :json=>{:data=>list.as_json}
  end
  
  
  def follow_requests
	  array = Follow.follow_requests(current_user.id)
	  list = []
    array.each{ |a|
    	user = User.find a
    	list << user.as_json( 
    		:except => [:avatar_file_size, :avatar_file_name, :avatar_content_type, :avatar_updated_at],
    		:methods => :avatar_url
    		) 
    }
    render :json=>{:data=>list.as_json}
  end

  def following_count
    # 2012-11-21 brucewang
  	# 파라미터 유효성 체크 (refs #277)
  	only_accepted = params[:only_accepted]
  	if( only_accepted!="true" && only_accepted!="false")
  		render :status=>500, :json=>{:data=>"Invalid parameter for only_accepted."}
  		return
  	end
    only_accepted = params[:only_accepted]=="true" || false
    
    
    count = Follow.following_count(current_user.id, only_accepted)
    render :json=>{:data=>count.as_json}
  end

  def followers_count
    # 2012-11-21 brucewang
  	# 파라미터 유효성 체크 (refs #277)
  	only_accepted = params[:only_accepted]
  	if( only_accepted!="true" && only_accepted!="false")
  		render :status=>500, :json=>{:data=>"Invalid parameter for only_accepted."}
  		return
  	end
    only_accepted = params[:only_accepted]=="true" || false
    
    
    count = Follow.followers_count(current_user.id, only_accepted)
    render :json=>{:data=>count}
  end

  def follow
    to = params[:to]
    #user_exists = User.exists?(to)
    # 2012-10-03 brucewang
    # ActiveModel 대신  MongoId를 사용할 때의 문법.
    user_exists = User.where(id: to).exists?()
    if !user_exists
      render :status=>406, :json=>{:data=>"user with that id does not exists"}
      return
    end
    Follow.follow(current_user.id, to)
    render :json=>{:data=>"ok"}
  rescue Mongoid::Errors::DocumentNotFound
    # 2012-11-19 brucewang
    # 없는 사용자에 대한 예외상황 처리.
    render :status=>500, :json=>{:data=>"user not found."}
  end

  def unfollow
    to = params[:to]
    
    # 2012-11-21 brucewang
    # 존재하지 않는 사용자에 대한 처리 (refs #276)
    user_exists = User.where(id: to).exists?()
    if !user_exists
      render :status=>406, :json=>{:data=>"user with that id does not exists"}
      return
    end
    
    
    # unfollow 할 때는 대상 사용자가 서비스를 탈퇴해서 조회가 안되더라도
    # follow record를 지워주면 된다.
    Follow.unfollow(current_user.id, to)
    render :json=>{:data=>"ok"}
  end

  def accept_follow
    # accept/reject는 상대방이 나를 follow하는것을 허용/거부 하는것이기때문에
    # 함수의 파라미터 순서에 유의해야 한다.
    to = params[:to]
    
    # 2012-11-21 brucewang
    # 존재하지 않는 사용자에 대한 처리 (refs #276)
    user_exists = User.where(id: to).exists?()
    if !user_exists
      render :status=>406, :json=>{:data=>"user with that id does not exists"}
      return
    end
    
    
    Follow.accept( to, current_user.id )
    render :json=>{:data=>"ok"}
  end

  def reject_follow
    # accept/reject는 상대방이 나를 follow하는것을 허용/거부 하는것이기때문에
    # 함수의 파라미터 순서에 유의해야 한다.
    to = params[:to]
    
    # 2012-11-21 brucewang
    # 존재하지 않는 사용자에 대한 처리 (refs #276)
    user_exists = User.where(id: to).exists?()
    if !user_exists
      render :status=>406, :json=>{:data=>"user with that id does not exists"}
      return
    end
    
    
    Follow.reject( to, current_user.id )
    render :json=>{:data=>"ok"}
  end

  def block_follow
    # block 할 때는 from이to로 접근하는 것을 막는다는 의미이기때문에
    # Follow.block 함수의 파라미터 순서를 유의해야 한다.
    # 즉, 현재 사용자에게 to가 접근하는것을 막는것이다.
    to = params[:to]
    
    # 2012-11-21 brucewang
    # 존재하지 않는 사용자에 대한 처리 (refs #276)
    user_exists = User.where(id: to).exists?()
    if !user_exists
      render :status=>406, :json=>{:data=>"user with that id does not exists"}
      return
    end
    
    
    Follow.block( to, current_user.id )
    render :json=>{:data=>"ok"}
  end

  def unblock_follow
    # unblock 할 때는 from이to로 다시 접근할 수 있도록 제한을 푼다...의미이기때문에
    # Follow.unblock 함수의 파라미터 순서를 유의해야 한다.
    # 즉, 현재 사용자에게 to가 접근하는것에대한 제한을 삭제하는것이다.
    to = params[:to]
    
    # 2012-11-21 brucewang
    # 존재하지 않는 사용자에 대한 처리 (refs #276)
    user_exists = User.where(id: to).exists?()
    if !user_exists
      render :status=>406, :json=>{:data=>"user with that id does not exists"}
      return
    end
    
    Follow.unblock( to, current_user.id )
    render :json=>{:data=>"ok"}
  end
  
  
  
  
  # irb(main):018:0* User.where( :fb_user_id.in => ["100004325374187","100001236559948"] ).only(:_id).entries.as_json( :only=>:_id )
	# => [{"_id"=>"507769ec421aa9b53f000001"}, {"_id"=>"50761b6d421aa9d372000001"}]
	def find_by_fbid
		fb_ids = params[ :fb_ids ]
		users = User.where( :fb_user_id.in => fb_ids ).only(:_id, :fb_user_id).entries
		render :json=>{ :data => users.as_json( 
			:only => [:_id, :fb_user_id ]
     	)
     }
	end

end

