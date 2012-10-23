# 2012-09-03 brucewang
# Created Class
#
# Like count 등을 매번 MongoDb를 조회해서 반환해선 performance에 큰 영향을 주므로
# 클라이언트에 보내주는 기본 feed 정보에는 count정보를 포함시키지 말고
# count 정보가 필요할 때 명시적으로 클라이언트가 요청을 보낼 때에만 조회를 하도록 한다.
#
# 아니면, 추후에 count 필드를 아예 저장해 두는것도 한 방법이겠으나
# 상황 발생시 매번 DB 필드 정보를 새로 저장해야 하는 단점도 있다.
class Action
	include Mongoid::Document
	include Mongoid::Timestamps

  has_many :action_feeds

	# Action을 수행한 사용자의 id 로서,
	# SQL DB의 레코드 id 에 매치된다.
	# 나중에 혹시 SQL DB 의 레코드 ID대신 MongoDB의 _id를 사용할 수도 있으므로
	# 일단 타입을 String으로 해 둔다.
	#field :actor_id,				type: String

	# 수행한 Action의 종류
  ACT_TYPE_COMMENT   = 0 # 0: 'Comment'
  ACT_TYPE_LIKE      = 1 # 1: 'Like/Dislike'
  ACT_TYPE_FOLLOW    = 2 # 2: 'Follow request' (수락/거절/블록 등의 action은 피드로 나타날 필요가 없음)
  ACT_TYPE_PLAYMOVIE = 3 # 3: 'Play movie'
  ACT_TYPE_POSTMOVIE = 4 # 4: 'Post Movie'
	field :action_type,			type: Integer

	# Action을 수행한 대상 (ActionTarget) 의 종류
	# 'Post' (Movie와 설명등을 포함함)
	# 	=> comment, like/dislike
	# 'Comment'
	# 	=> like/dislike
	# 'Movie'
	# 	=> play/post
	#
	# 이상의 Movie, Comment 등의 Action 대상이 되는 정보들은
	# MongoDb에 저장할 것임.
  TARGET_TYPE_POST = 0 # Post
  TARGET_TYPE_COMMENT = 1 # Comment
	field :target_type,			type: Integer

	# Action을 수행한 대상(ActionTarget) 의 id 로서,
	# 앞서 'target_type'에 따라 그에 매치되는 MongoDb
	# 레코드 id 에 매치된다.
	field :target_id,				type: String


	# # 피드가 생성/수정된 시각
	# field :created_at, type: DateTime, default: Time.now
	# field :updated_at, type: DateTime, default: Time.now
	# 
	# 
	# # 피드 생성 시각은, 생성된 이후로는 코드에서 변경할 수 없도록 한다.
	# attr_readonly :created_at


  # Indexing의 다양한 옵션..
  # http://mongoid.org/en/mongoid/docs/indexing.html   	
  index ( {action_type: 1, target_type: 1, target_id: 1} )
end
