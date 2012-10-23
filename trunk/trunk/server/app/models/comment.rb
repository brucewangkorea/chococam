#
# 2012-09-04 brucewang
# Created Class
#
# 코멘트
#
class Comment
	include Mongoid::Document
	include Mongoid::Timestamps
	
  # comment 를 게제한 사용자의 id
	field :user_id,				 type: String
	
	# 설명
	field :description,	   type: String

	belongs_to :post, index: true #, class_name: "Post"
  belongs_to :reply_to, index: true, class_name: "Comment"
  has_many   :replies, class_name: "Comment"
  
  # 여러개의 like를 가질 수 있음.
  has_many :likes
  
  
  # 2012-10-08 brucewang
  # 이 포스트를 작성한 사용자에 대한 상세 정보를 반환하는 함수.
  def user
  	u = User.find self.user_id
  	u.as_json( 
    		:except => [:avatar_file_size, :avatar_file_name, :avatar_content_type, :avatar_updated_at],
    		:methods => :avatar_url
    		) 
  end
end
