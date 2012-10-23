#Mongoid::Fields.option :null do |model, field, value|
  # Your logic here...
#end
#Mongoid::Fields.option :limit do |model, field, value|
#  # Your logic here...
#end

 
class User #< ActiveRecord::Base
	include Mongoid::Document
	include Mongoid::Timestamps
  include Mongoid::Paperclip	
  # Include default devise modules. Others available are:
  # :token_authenticatable, :encryptable, :confirmable, :lockable, :timeoutable and :omniauthable

	def email_required?
	  false
	end
	
	def password_required?
		false
	end
        
  ## Database authenticatable
  field :email,              :type => String
  field :encrypted_password, :type => String
  #validates_presence_of :email,options = { :allow_nil => true }
  ## Recoverable
  field :reset_password_token,   type:  String
  field :reset_password_sent_at, type:  Time
  ## Rememberable
  field :remember_created_at, type:  Time
  ## Trackable
  field :sign_in_count,      type:  Integer, default: 0
  field :current_sign_in_at, type:  Time
  field :last_sign_in_at,    type:  Time
  field :current_sign_in_ip, type:  String
  field :last_sign_in_ip,    type:  String
  ## Confirmable
  field :confirmation_token,   type:  String
  field :confirmed_at,         type:  Time
  field :confirmation_sent_at, type:  Time
  field :unconfirmed_email,    type:  String # Only if using reconfirmable
  ## Lockable
  field :failed_attempts, type:  Integer, default: 0 # Only if lock strategy is :failed_attempts
  field :unlock_token,    type:  String # Only if unlock strategy is :email or :both
  field :locked_at,       type:  Time
  ## Token authenticatable
  field :authentication_token, type:  String
  field :name, type:  String, default: ""
  #validates_presence_of :name
  field :fb_user_id, type:  String
  field :remote_profile_image, type: String, default: nil
  
  # run 'rake db:mongoid:create_indexes' to create indexes
  #index({ email: 1, fb_user_id: 1 }, { unique: true, background: true })
  
  
  devise :token_authenticatable, :database_authenticatable, :omniauthable, :registerable,
        :recoverable, :rememberable, :trackable, :validatable
  
  
  
  


  # Setup accessible (or protected) attributes for your model
  attr_accessible :avatar, :name, :email, :password, :password_confirmation, :remember_me, :fb_user_id
  
  # 2012-10-09 brucewang
  # 원격지의 이미지를 업로드 하기 위해 사용.
  # 다음 링크 참고.
  # http://trevorturk.com/2008/12/11/easy-upload-via-url-with-paperclip/
  attr_accessor :image_url
  
  

  
  # Paperclip 을 사용한 파일 업로드.
  has_mongoid_attached_file :avatar, 
    :url => '/uploads/:class/:id_partition/:style/:filename',
    :default_url => 'missing_:style.png',
    :path => ':rails_root/public/uploads/:class/:id_partition/:style/:filename',
    :styles => {
      :original => ['1280x720', :jpg],
      :thumbnail    => ['122x122!',   :jpg]
    },
    :convert_options => { :all => '-background white -flatten +matte' }
  
  # 2012-10-09 brucewang
  # 원격지의 이미지를 업로드 하기 위해 사용.
  # 다음 링크 참고.
  # http://trevorturk.com/2008/12/11/easy-upload-via-url-with-paperclip/
  before_validation :download_remote_image, :if => :image_url_provided?
  validates_presence_of :remote_profile_image, :if => :image_url_provided?, :message => 'is invalid or inaccessible'
  
  def image_url_provided?
    !self.image_url.blank?
  end
  
  def download_remote_image
    self.avatar = do_download_remote_image
    self.remote_profile_image = image_url
  end
  
  def do_download_remote_image
    io = open(URI.parse(image_url))
    def io.original_filename; base_uri.path.split('/').last; end
    io.original_filename.blank? ? nil : io
  rescue # catch url errors with validations instead of exceptions (Errno::ENOENT, OpenURI::HTTPError, etc...)
  end
  
  
  # 2012-10-09 brucewang
  # 사용자의 avatar 정보의 필요 정보만 반환하는 함수.
  def avatar_url
  	{ 
  		"thumbnail" => self.avatar.url(:thumbnail),
  		"original" => self.avatar.url(:original)
  	}
  end
  
  
  # 2012-10-03 brucewang
  # ActiveRecord 대신  MongoId를 사용할 때는
  # ActiveRecord 처럼 find_by_XXX 함수가 자동으로 만들어지지 않는다.
  def self.find_by_fb_user_id(fb_user_id)
	  User.find_by( :fb_user_id=>fb_user_id )
  end
  def self.find_by_email(email)
	  User.find_by( :email=>email )
  end
  def self.find_by_authentication_token(authentication_token)
	  User.find_by( :authentication_token=>authentication_token )
  end
  
  
  
  

  def self.find_or_create_from_auth_hash(auth_hash)
    user_data = auth_hash.extra.raw_info
    if user = User.where(:fb_user_id => user_data.id).first
      user
    else # Create a user with a stub password. 
      User.create!(:fb_user_id => user_data.id, :name => user_data.name, :email => user_data.email)
    end
  end

  def self.new_with_session(params, session)
    super.tap do |user|
      if data = session["devise.facebook_data"] && session["devise.facebook_data"]["extra"]["raw_info"]
        user.email = data["email"] if user.email.blank?
      end
    end
  end
end
