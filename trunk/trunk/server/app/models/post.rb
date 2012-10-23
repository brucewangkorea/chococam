#
# 2012-09-03 brucewang
# Created Class
#
# ActionTarget 클래스로부터 파생.
#
class Post < ActionTarget
	# ActionTarget 클래스에 이미 존재하는 필드들이므로 필요 없음.
	#     include Mongoid::Document
	#     include Mongoid::Timestamps

	# Post를 게제한 사용자의 id
	field :user_id,				 type: String
	# 설명
	field :description,	   type: String
  # ref 된 사용자들
  field :referred_users, type: Array
  # hashtag들
  field :hashtags,       type: Array
  field :view_count,     type: Integer, default: 0
  field :like_count,     type: Integer, default: 0
  field :comment_count,     type: Integer, default: 0
  


  # 좋아요 횟수.
  # field :like_count,     type: Integer

  # comment 갯수
  # field :comment_count,  type: Integer
  #
  # 다음과 같이 갯수를 매번 계산할 수 있음.
  #p.comment_ids.count

  # 2012-09-25 brucewang
  # Post에 여러 다양한 컨텐츠를 지정할 수 있도록, Post와 Movie를 분리함.
  #
	# 하나의 동영상(Movie)을 가질 수 있음.
	# http://mongoid.org/en/mongoid/docs/relations.html
	#embeds_one :movie, cascade_callbacks: true #, class_name: "Movie"
	has_one :movie
	
  # 2012-09-12 brucewang
  # view에서 form_for 내에서 fields_for 로 'movie' 라는 association에 접근 가능하기 위해선
  # accepts_nested_attributes_for 를 지정해 줘야 함.
  accepts_nested_attributes_for :movie

  # 여러개의 코멘트를 가질 수 있음.
  has_many :comments
  accepts_nested_attributes_for :comments

  # 여러개의 like를 가질 수 있음.
  has_many :likes
  
  # http://mongoid.org/en/mongoid/docs/indexing.html
  # rake db:mongoid:create_indexes
  index ( {user_id: 1} )
  
  # Movie 에 대한 post를 만들어주는 함수.
  # params = {:user_id=> "1", :movie_id=>"50446a2d6a8745df51000001", :description=>"test", :scope=>"public"}
  #def self.create_movie_post( params )
  #  Post.new( :user_id=> "1", :movie_id=>"50446a2d6a8745df51000001", :description=>"test", :scope=>"public")
  #end
	
  
  
  # 2012-10-08 brucewang
  # 이 포스트를 작성한 사용자에 대한 상세 정보를 반환하는 함수.
  def user
  	u = User.find self.user_id
  	u.as_json( 
    		:except => [:avatar_file_size, :avatar_file_name, :avatar_content_type, :avatar_updated_at],
    		:methods => :avatar_url
    		) 
  end
  
  

  # 지정된 정보에 따라 post를 작성.
  def self.create_movie_post(user_id, movie, description, scope, recipients=[])
    post = Post.new( :user_id=> user_id, :movie=>movie, :description=>description, :scope=>scope, :recipients=>recipients )
    post.save
    post
  end


end
