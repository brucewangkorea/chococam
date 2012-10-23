class Api::V1::ActionFeedsController < ApplicationController
  skip_before_filter :verify_authenticity_token
  before_filter :authenticate_user!
  
  
  
  
  # 2012-09-13 brucewang
  # 현재 사용자를 위해 생성된 모든 feed 들을 반환.
  # GET http://localhost:3000/api/v1/action_feeds.json?auth_token=vWLsJF4au1StrEC7nPpf&page=0
  # 파라미터로 get_count=true 를 지정하면 총 레코드 카운트만 반환.
  def index
    page = params[:page] || 0
    getcount = params[:get_count]=="true"
    
    if getcount
      count = ActionFeed.where( reader_id: current_user.id ).count
      render :json=>{:data=>count}
    else
      # ActionFeed.where( reader_id: u.id, :created_at.lte => tnow ).count
      @feeds = ActionFeed.where( reader_id: current_user.id ).page(page).descending(:created_at).entries
      
      @feeds.each{ |f|
      	next if f.action.nil?
      	
      	case f.action.action_type
      	when Action::ACT_TYPE_COMMENT
      	  result = Comment.find( f.action.target_id ) rescue nil
      	  unless result.nil?
	    f["comment"] = result.as_json(:methods=>:user)
	  end
      	when Action::ACT_TYPE_LIKE
      	  result = Like.find( f.action.target_id ) rescue nil
      	  unless result.nil?
            f["like"] = result.as_json(:methods=>:user)
          end
      	when Action::ACT_TYPE_FOLLOW
      	when Action::ACT_TYPE_PLAYMOVIE, Action::ACT_TYPE_POSTMOVIE
	      	f["post"] = Post.find( f.action.target_id ).as_json(
	      		:include=> { :movie => {:methods=>:file_url} },
	      		:methods=>:user
	      	)
      	else
      	end
      }	
			
      #render :json=>{:data=>@feeds.as_json( :include=>{:action, {:data, :include=>:movie} } )}
      render :json=>{:data=>@feeds.as_json( 
		      :include=> :data, 
		      :include=> {:action => {:only =>[:user_id, :action_type, :target_type, :target_id]} } 
        )}
    end
  end
end
