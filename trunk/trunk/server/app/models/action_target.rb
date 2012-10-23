#
# 2012-08-31 brucewang
# Created Class
#
# Post, Comment, Like 등 Feed로서 등록될 수 있는 것들의 최상위 클래스이다.
# 이러한 자식클래스들은 모두 동일한 MongoDb collection에 저장되지만
# 자식클래스에서 where로 검색을 하면 자신의 클래스에 매치되는 것들만 따로 조회가 된다.
# 자세한 내용은 아래 링크 참고.
# 'http://mongoid.org/en/mongoid/docs/documents.html#inheritance'
#
class ActionTarget
	include Mongoid::Document
	include Mongoid::Timestamps
	
	
	# 이 Action을 볼 수 있는 대상 사용자 그룹.
	# 액션대상을 생성한 사람은 무조건 그 액션 대상을 볼 수 있다.
	# 
	# 이 정보는 생성자 이외의 사용자가 해당 Action대상을 볼 때 (전체공개인가 아닌가 등의 단순 정보)
	# 그리고 해당 Action이 발생했을 때 ActionFeed들을 부차적으로 생성할 때에만
	# 의미가 있다. 
	#
	# Public : Chococam 서비스를 사용하는 모든 사용자.
	# All Friends : 모든 친구가 볼 수 있음.
	# Only Me : 나에게만.
	# Restricted : 특별히 지정한 사용자에게만.
	#
  # 아래의 모든 scope에 있어서 작성자 본인은 무조건 해당 포스트를 조회할 수 있다.
  #
  # 'public'  : 이 scope 일 경우, 이 포스트는 모든 사용자들이 조회할 수 있다. 
  #     그러나 모든 사용자에게 ActionFeed가 생성되는것은 아니고, 친구들(그중에서도 Follower들)에게만 우선
  #     피드가 생성된다.
  # 'friends' : 친구 관계인 사람들(Follwer/Following모두)만 볼 수 있으며, Feed는 각 친구들(중 Follwer들만)을 대상으로생성된다.
  # 'onlyme'  : 오로지 자기만 볼 수 있다.
  # 'restricted' : Follwer 중 지정된 사용자만 볼 수 있다.
  #
  # ** 다음에서 지정한 값대로 Db에 저장되고 app 내부에서 비교되므로 :public 과 같이 symbol로
  # 정의해서는 안됨.. 반드시 "public" 과 같은 String으로 정의해야 함. **
  TARGET_PUBLIC = "public"
  TARGET_FRIENDS = "friends"
  TARGET_ONLYME = "onlyme"
  TARGET_RESTRICTED = "resctricted"
	field :scope,           type: String, default: TARGET_PUBLIC

  field :recipients,      type: Array, default: []
	
end
