# 
# 2012-09-18 brucewang
# 다음 명령으로 테스트 할 수 있다.
# bundle exec rspec spec/requests/* 
#
# 테스트 스펙 생성은 다음과 같이...
# rails generate integration_test [이름]
#
require 'spec_helper'
require 'helpers'
RSpec.configure do |c|
  c.include Helpers
end

describe Api::V1::UsersController, "Followings" do

  


  it "should do 'following' properly" do
	  create_user_1_2_3
    user1_follow_user2_user3

    # user1이 follow하는 친구 중 accept가 된 목록 => 0
    token = get_authtoken(@user1)
    post api_v1_users_following_path, :auth_token=>token, :only_accepted=>true
    response.status.should be(200)
    parsed_body = JSON.parse(response.body)
    parsed_body["data"].count.should eql 0
    #puts "---------------------------------"
    #puts Follow.following(@user1.id, true).as_json.inspect
    #puts response.body
    #puts "---------------------------------"

    # user1이 follow하는 친구들 (accept 여부는 상관 없음) => 2
    post '/api/v1/users/following', :auth_token=>token
    response.status.should be(200)
    parsed_body = JSON.parse(response.body)
    puts response.body
    #puts "---------------------------------"
    #puts Follow.following(@user1.id, false).as_json.inspect
    #puts response.body
    #puts "---------------------------------"
    parsed_body["data"].count.should eql 2

    #sign_out @user
    get 'logout'
    #delete 'users/sign_out'
    #delete "api/v1/tokens/#{token}"
  end
  

  it "should do 'followers' properly" do
  	create_user_1_2_3
    user1_follow_user2_user3

    # user2를 follow하는 사용자 중 accept 된 목록 => 0
    token = get_authtoken(@user2)
    post '/api/v1/users/followers', :auth_token=>token, :only_accepted=>true
    response.status.should be(200)
    puts response.body
    parsed_body = JSON.parse(response.body)
    parsed_body["data"].count.should eql 0

    # user2를 follow 하는 모든 사용자 (accept 여부 상관 없음) => 1
    post '/api/v1/users/followers', :auth_token=>token
    response.status.should be(200)
    puts response.body
    parsed_body = JSON.parse(response.body)
    parsed_body["data"].count.should eql 1
  end
  
  
  #
  # user2 accepts following request from user1
  # following list of user1 should contain user2
  # follower list of user2 should contain user1
  #
  #
  #


end
