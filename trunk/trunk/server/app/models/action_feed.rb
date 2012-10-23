# 2012-08-31 brucewang
# Created Class
#
# ActionFeed는 어떤 특정 Action 이 발생했음을 특정 구독자에게 알려주기 위해
# 만들어지는 레코드 이다.
# 
# 이러한 ActionFeed는 Action을 수행한 사용자 (Actor)가 지정한 공개 대상에 따라
# 소식을 받을 수 있는 사용자도 있고 받을 수 없는 사용자도 있다. 어떤 사용자가
# ActionFeed들을 조회할 때,각 ActionFeed의 actor 친구들 정보를 일일이 확인해서
# Feed를 수신하는것 보다는, 미리 Feed를 받게될 사람들을 대상으로 Feed레코드를 
# 만들어 두는것이 조회 시간이 단축될 것이다.
#
#
class ActionFeed
	include Mongoid::Document
	include Mongoid::Timestamps

  belongs_to :action, index: true
	
	# 이 ActionFeed를 읽게 될 특정 사용자의 id
	#
	# *참고로*
	# 	Post, Movie 등 Action이 수행될 대상은 공개 설정이 
	#   public(Chococam 서비스를 사용하는 모든 사용자.)이 될 수 있지만
	# 	Feed자체는 모든 사람들에 대해 만들어 지지 않고 All Friends에게만 피드가 만들어진다.
	# 	친구관계가 아닌 사람에게 다른 사람의 public 피드가 다 보이면 피드 수가 기하급수적으로 증가하고
	# 	또 자기와 친구 관계가 아닌 사람의 모든 피드를 Wall에서 볼 필요는 없다.
	#
	# Action이 발생하면, 해당 Action의 ActionTarget에 지정된 공개대상(scope) 
	# 설정에 맞추어 해당 피드를 볼 수 있는 대상들에 대해 피드가 만들어진다.
	# 이후에 ActionTarget에 지정된 공개대상(scope) 이 바뀌더라도 다시 Feed가 생성되진 않는다.
	field :reader_id,       type: String
		
	
  index ( {reder_id: 1} )


  # 2012-09-07 brucewang
  # 지정된 'post' 에 대하여 적절한 대상 사용자들에게 Feed를 생성.
  def self.create_movie_post_feed(user_id, post, action_type, target_type, target_id)
    #ActiveRecord::Base.logger.info "---------------------------"
    #ActiveRecord::Base.logger.info "create_movie_post_feed"
    
    # Feed로 알려줄 Action 레코드를 만들고...
    action = Action.new( :user_id=>user_id, :action_type=>action_type, :target_type=>target_type, :target_id=>target_id)
    saveresult = action.save
    if saveresult
      # Post의 공개 scope 값에 따라, 지정된 사람들이 읽을 수 있는 Feed 레코드를 생성.
      #ActiveRecord::Base.logger.info "scope = #{post.scope}"
      
      case post.scope
      when ActionTarget::TARGET_PUBLIC, ActionTarget::TARGET_FRIENDS
        # Follwer 들에게 Feed를 생성.
        followers = Follow.followers(user_id)
        followers.each{|follower_id,accepted|
          #ActiveRecord::Base.logger.info "Create feed for #{follower_id}"
          ActionFeed.create( :action_id=>action.id, :reader_id=>follower_id )
        }
      when ActionTarget::TARGET_RESTRICTED
        #ActiveRecord::Base.logger.info "ActionTarget::TARGET_RESTRICTED"
        
        # 지정된 사용자들에게 Feed를 생성.
        post.recipients.each{ |recepient_id|
          ActionFeed.create( :action_id=>action.id, :reader_id=>recepient_id )
        }
      else
      end # endof 'case' statement
      
      # 2012-10-08 brucewang
      # 마지막으로 자기 자신에 대한 feed를 작성.
      ActionFeed.create( :action_id=>action.id, :reader_id=>user_id )
    end # endof saveresult
    #ActiveRecord::Base.logger.info "---------------------------"
  end



end
