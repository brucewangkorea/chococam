module Helpers
  def help
    :available
  end
  
  
  def create_user_1_2_3
    @user1 =User.create!(:fb_user_id => "1", :name => "user1", :email => "a1@test.com", :password=>"aaaaa1")
    @user2 =User.create!(:fb_user_id => "2", :name => "user2", :email => "a2@test.com", :password=>"aaaaa2")
    @user3 =User.create!(:fb_user_id => "3", :name => "user3", :email => "a3@test.com", :password=>"aaaaa3")
  end
  
  def get_authtoken(user)
    post 'api/v1/tokens.json', :email=>user.email, :password=>user.password
    parsed_body = JSON.parse(response.body)
    response.status.should be(200)
    response.body.should have_content "token"
    parsed_body["token"]
  end
  

  def user1_follow_user2_user3
    # user1이 user2를 follow
    token = get_authtoken(@user1)
    post '/api/v1/users/follow', :auth_token=>token, :to=>@user2.id
    response.status.should be(200)
    # user1이 user3를 follow
    post '/api/v1/users/follow', :auth_token=>token, :to=>@user3.id
    response.status.should be(200)
    #delete 'users/sign_out'
    #delete "api/v1/tokens/#{token}"
    get 'logout'
  end
  
  
  
  def file_attachment
    test_movie = "#{Rails.root}/spec/assets/attachments/a.mp4"
    Rack::Test::UploadedFile.new(test_movie, "video/mp4")
  end
  
  def create_movie_post_file_attach( user, description, name, scope=ActionTarget::TARGET_PUBLIC, recipients=[] )
		token = get_authtoken( user )
		post api_v1_posts_path, :auth_token=>token, 
			:post => {
				:description => description,
				:scope => scope,
				:recipients => recipients,
				:movie_attributes => {
					:name => name, 
					:file => file_attachment
				}
			}
		#response.status.should be(200)
		#puts response.body
		
		#if auto_signout
		#	#sign_out @user
		#	get 'logout'
	  #end
	  response
  end
end