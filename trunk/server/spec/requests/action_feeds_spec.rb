require 'spec_helper'
require 'helpers'
RSpec.configure do |c|
  c.include Helpers
end



describe "ActionFeeds" do
	it "should show feeds properly" do
		# user1 이 user2,3를 follow
		create_user_1_2_3
		user1_follow_user2_user3
		# user2,3 가 user1으로부터의 follow 요청을 승락.
		Follow.accept( @user1.id, @user2.id )
		Follow.accept( @user1.id, @user3.id )
		
		# user2가 public으로 포스트를 작성.
		http_response = create_movie_post_file_attach( @user2, 
			"Test Description 2", 
			"Test Movie Title 2",
			ActionTarget::TARGET_PUBLIC )
		http_response.status.should be(200)
		get 'logout'
		
		# user3가 friends_only로 포스트를 작성.
		http_response = create_movie_post_file_attach( @user3, 
			"Test Description 3", 
			"Test Movie Title 3",
			ActionTarget::TARGET_FRIENDS )
		http_response.status.should be(200)
		
		# user3가 restricted로 대상을  user1으로 하여 포스트를 작성.
		http_response = create_movie_post_file_attach( @user3, 
			"Test Description 3-1", 
			"Test Movie Title 3-1",
			ActionTarget::TARGET_RESTRICTED,
			[@user1.id] )
		http_response.status.should be(200)
		get 'logout'
		
		# user1에 대한  ActionFeed가 3개가 반환되어야 함.
		token = get_authtoken( @user1 )
		get '/api/v1/action_feeds.json', :auth_token=>token
		puts response.body
		parsed_body = JSON.parse(response.body)
  	parsed_body["data"].count.should eql 3
	end
end