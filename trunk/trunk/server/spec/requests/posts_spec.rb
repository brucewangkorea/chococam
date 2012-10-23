require 'spec_helper'
require 'helpers'
RSpec.configure do |c|
  c.include Helpers
end



describe "Posts" do
  
  
  

  
  describe "POST /api/v1/posts#create" do
    it "should upload movie correctly" do
	    create_user_1_2_3
	    response = create_movie_post_file_attach( @user1, "Test Description", "Test Movie Title" )
	    response.status.should be(200)
			#puts response.body
			get 'logout'
    end
  end
  
  
  
  
  describe "GET /api/v1/posts/of_friends.json" do
  	it "should show friends' posts properly" do
  		# uer1　이 user2,3을 follow
  		create_user_1_2_3
  		user1_follow_user2_user3
  		
  		# user 2,3이 각각 post를 생성
  		http_response = create_movie_post_file_attach( @user2, "Test Description 2", "Test Movie Title 2" )
  		get 'logout'
  		http_response = create_movie_post_file_attach( @user3, "Test Description 3", "Test Movie Title 3" )
  		get 'logout'
  		
  		# user 2만 user1 의 follow  요청을 승인
  		Follow.accept( @user1.id, @user2.id )
  		
  		# API 수행 결과가 user1의 following 친구들 중 허락된 친구인  user2의 포스트만 리스트업 해야 함.
  		token = get_authtoken( @user1 )
  		post '/api/v1/posts/of_friends.json', :auth_token=>token
  		#puts response.body
  		response.status.should be(200)
  		parsed_body = JSON.parse(response.body)
	    parsed_body["data"].count.should eql 1
  	end
  end
  
  
  
  
  describe "comments" do
  	before do
	  	create_user_1_2_3
  	end
  	
  	it "should create/delete comments" do
  		# user 1이 post를 생성
  		http_response = create_movie_post_file_attach( @user1, "Test Description 1", "Test Movie Title 1" )
  		get 'logout'
  		
  		# 생성된 포스트의 갯수는 1개
  		token = get_authtoken( @user2 )
  		get '/api/v1/posts.json', :auth_token=>token
  		parsed_body = JSON.parse(response.body)
  		parsed_body["data"].count.should eql 1
  		post_id = parsed_body["data"][0]["_id"]
  		
  		# user2, user3가 코멘트를 작성.
  		token = get_authtoken( @user2 )
  		post '/api/v1/posts/create_comment.json', :auth_token=>token, :description => "comment 2", :post_id=>post_id
  		response.status.should be(200)
  		get 'logout'
  		token = get_authtoken( @user3 )
  		post '/api/v1/posts/create_comment.json', :auth_token=>token, :description => "comment 3", :post_id=>post_id
  		response.status.should be(200)
  		
  		# 해당 포스트의 코멘트 수가 2개이어야 함.
  		get '/api/v1/posts/show.json', :auth_token=>token, :id=>post_id
  		response.status.should be(200)
  		parsed_body = JSON.parse(response.body)
  		parsed_body["data"]["comments"].count.should eql 2
  		
  		# user2가 자신의 코멘트를 삭제
  		comment_id = parsed_body["data"]["comments"][0]["_id"]
  		post '/api/v1/posts/delete_comment.json', :auth_token=>token, :id=>comment_id
  		
  		# 해당 포스트의 코멘트 수가 1개이어야 함.
  		get '/api/v1/posts/show.json', :auth_token=>token, :id=>post_id
  		response.status.should be(200)
  		parsed_body = JSON.parse(response.body)
  		parsed_body["data"]["comments"].count.should eql 1
  	end
  	
  	
  	
  	
  	it "should create/delete likes" do
  		# user 1이 post를 생성
  		http_response = create_movie_post_file_attach( @user1, "Test Description 1", "Test Movie Title 1" )
  		get 'logout'
  		
  		# 생성된 포스트의 갯수는 1개
  		token = get_authtoken( @user2 )
  		get '/api/v1/posts.json', :auth_token=>token
  		parsed_body = JSON.parse(response.body)
  		parsed_body["data"].count.should eql 1
  		post_id = parsed_body["data"][0]["_id"]
  		
  		# user2가 코멘트를 작성.
  		token = get_authtoken( @user2 )
  		post '/api/v1/posts/create_comment.json', :auth_token=>token, :description => "comment 2", :post_id=>post_id
  		response.status.should be(200)
  		get 'logout'
  		
  		# 첫 포스트의 상세 정보 확인.
  		get '/api/v1/posts/show.json', :auth_token=>token, :id=>post_id
  		response.status.should be(200)
  		parsed_body = JSON.parse(response.body)
  		# 첫 코멘트의  id 확인.
  		comment_id = parsed_body["data"]["comments"][0]["_id"]
  		
  		# user3 가  post를 '좋아요' 함.
  		post '/api/v1/posts/like_post.json', :auth_token=>token, :id=>post_id
  		
  		# 해당 포스트의 좋아요 갯수가 1개이어야 함.
  		get '/api/v1/posts/show.json', :auth_token=>token, :id=>post_id
  		response.status.should be(200)
  		parsed_body = JSON.parse(response.body)
  		#parsed_body["data"]["likes"].count.should eql 1
  		parsed_body["data"]["like_count"].should eql 1
  		
  		# user3 가 comment를 '좋아요' 함.
  		post '/api/v1/posts/like_comment.json', :auth_token=>token, :id=>comment_id
  		
  		# 해당 포스트의 좋아요 갯수가 2개이어야 함.
  		get '/api/v1/posts/show.json', :auth_token=>token, :id=>post_id
  		response.status.should be(200)
  		parsed_body = JSON.parse(response.body)
  		parsed_body["data"]["like_count"].should eql 2
  		like_id = parsed_body["data"]["likes"][0]["_id"]
  		
  		# user3가 자신의 '좋아요' 를 취소.
  		post '/api/v1/posts/unlike.json', :auth_token=>token, :id=>like_id
  		
  		# 해당 포스트의 좋아요 갯수가 1개이어야 함.
  		get '/api/v1/posts/show.json', :auth_token=>token, :id=>post_id
  		response.status.should be(200)
  		parsed_body = JSON.parse(response.body)
  		#parsed_body["data"]["likes"].count.should eql 1
  		parsed_body["data"]["like_count"].should eql 1
  	end
  	
  end
end


