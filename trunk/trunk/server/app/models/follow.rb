#
# 2012-09-05 brucewang
# Created Class
#
# User 모델에 통합하려고 하였으나, amico 모델 처럼 따로  Followship을 분리해 두는것이
# 더 나을것으로 판단하였음.
# https://github.com/agoragames/amico
#
#
# Follow
#
#=================================================================
#                         Test Flow
#=================================================================
# from=1
# to=2
#----------------------------------------- 
# # (1) from이 to에게 follow를 요청.
#----------------------------------------- 
# Follow.follow(from, to)
# Follow.following(from) #=> [["2",false]]
# Follow.pending?(from, to) #=> true
# Follow.followers(to) #=> [["1",false]]
# Follow.following_count(from) #=> 1
# Follow.following_count(from, true) #=> 0 # 아직 accept를 하지 않았음.
# Follow.accept(from, to)
# Follow.following_count(from, true) #=> 1 # accept가 완료되었음.
# 
# 
# 
#----------------------------------------- 
# # (2) from이 to의 follow요청을 거절.
#----------------------------------------- 
# Follow.reject(from, to)
# Follow.following(from) #=> [["2",false]] # 여전히 친구 관계인 듯 보임. 대신 pending상태...
# Follow.rejected?(from, to) #=> true # reject 되었음
# Follow.pending?(from, to) #= > true # 그러나 상대방에겐 follow요청이 아직 pending 된 상태로 보임.
# Follow.following?(from,to) #=> false
# Follow.followers_count(to) #=> 1
# Follow.followers_count(to,true) #=> 0 # 아직 reject된 상태임.
# Follow.accept(from, to)
# Follow.followers_count(to, true) #=> 1
# 
#----------------------------------------- 
# # (3) from 이 더이상 to를 follow하지 않음.
#----------------------------------------- 
# Follow.unfollow(from, to)
# Follow.following(from) #=> []
# Follow.followers(to) #=> []
# 
# 
#----------------------------------------- 
# # (4) from 이 to를 follow하고 있던 상황에서 
#----------------------------------------- 
# Follow.follow(from, to)
# Follow.accept(from, to)
# # to가 from이 싫어서 block하는 경우
# # (from으로부터 to로의 follow를 금지 시킴.)
# Follow.block(from, to)
# Follow.blocked?(from, to) #=> true
# Follow.following(from) #=> []  #block되었음..
# Follow.followers(to) #=> []
# # block을 해제함
# Follow.unblock(from, to)
# Follow.blocked?(from, to) #=> false
# Follow.following(from) #=> [] # 원래 관계는 사라짐
# Follow.followers(to) #=> [] # 원래 관계는 사라짐.
# 
# 
#----------------------------------------- 
# # (5) from과 to 사이에 아무런 follow 관계가 없을때
# # 일방적으로 from이 to에게 접근하지 못하도록 block
#----------------------------------------- 
# Follow.block(from, to)
# Follow.blocked?(from, to) #=> true
# Follow.following(from) #=> []  #block되었음..
# Follow.followers(to) #=> []
#
#=================================================================
#
class Follow
	include Mongoid::Document
	
  # comment 를 게제한 사용자의 id
	field :user_id_from,	 type: String
	field :user_id_to,	   type: String
	field :accepted,	     type: Boolean, default: false
	field :blocked,	       type: Boolean, default: false

  # rejected 상태는 blocked 상태와는 약간 다름..
  # blocked 된 사용자는 block한 사용자의 피드는 물론 post 전체를 전혀 볼 수 없게 해야 하지만
  # rejected 된 사용자는 피드만 못 볼 뿐 post는 직접 찾아가서 볼 수 있음. 
  # 그리고 마치 following 신청은 아직도 유효한 듯 보이게 됨.
	field :rejected,	     type: Boolean, default: false


  # from, to 사이에 unique index를 걸어준다.
  index({ user_id_from: 1, user_id_to:1 }, { unique: true, name: "followship_index" })

  # from,to 는 unique 해야 함. (중복 레코드 방지)
  validates_uniqueness_of :user_id_from, :scope => [:user_id_to], :message => "combination of user_id_from and user_id_to should be unique."


  # 사용자 'from'이 follow 하고 있는 사용자들의 id를 반환한다.
  def self.following(from, only_accepted=false)
    result = []
    param = { :user_id_from=>from, :blocked=>false }
    param[:accepted] = true if only_accepted
    Follow.only("user_id_to", "accepted").where( param ).each { |f|
      result << [f.user_id_to, f.accepted]
    }
    result
  end
  
  # 사용자 'from'에게 들어 온 follow 요청 (아직 accept 되지 않은) 목록을 확인한다.
  def self.follow_requests(from)
    result = []
    param = { :user_id_to=>from , :blocked=>false, :rejected=>false}
    param[:accepted] = false

    Follow.only("user_id_from").where( param ).each { |f|
      result << f.user_id_from
    }
    result
  end

  # 사용자 'to'를 follow 하고 있는 사용자들의 id를 반환한다.
  def self.followers(to, only_accepted=false)
    result = []
    param = { :user_id_to=>to , :blocked=>false}
    param[:accepted] = true if only_accepted

    Follow.only("user_id_from","accepted").where( param ).each { |f|
      result << [f.user_id_from, f.accepted]
    }
    result
  end

  # 사용자 'from'이 follow 하고 있는 사용자들의 총 수를 반환한다.
  def self.following_count(from, only_accepted=false)
    param = { :user_id_from=>from, :blocked=>false }
    param[:accepted] = true if only_accepted

    Follow.where( param ).count
  end

  # 사용자 'to'를 follow 하고 있는 사용자들의 총 수를 반환한다.
  def self.followers_count(to, only_accepted=false)
    param = { :user_id_to=>to, :blocked=>false }
    param[:accepted] = true if only_accepted

    Follow.where( param ).count
  end


  # 사용자 'from'이 'to'에게 follow 신청을 한다.
  def self.follow(from, to)
    Follow.create( :user_id_from=>from, :user_id_to=>to)
  end

  # 사용자 'from'이 'to'를 follow 하던 것을 취소한다.
  def self.unfollow(from, to)
     Follow.where( :user_id_from=>from, :user_id_to=>to ).delete
  end

  # 사용자 'from'이 'to' 를 follow 하고 있는지 확인한다.
  # 반환값이 true라면 to가 accept까지 완료 한 것이고,
  # false라면 아직 pending 중이거나, 아예 요청을 하지 않은 것이다.
  # blocked 상태와는 상관이 없다.
  def self.following?(from,to)
    Follow.where( :user_id_from=>from, :user_id_to=>to, :accepted=>true ).exists?
  end

  # 사용자 'from'이 'to'에게 보낸 follow 신청이 아직 수락 대기중인지 확인한다.
  # 반환값이 false라면 수락된 것이다.
  def self.pending?(from, to)
    Follow.where( :user_id_from=>from, :user_id_to=>to, :accepted=>false ).exists?
  end

  # 사용자 'from'이 'to'에게 보낸 follow 요청을 accpet한다.
  def self.accept(from, to)
    Follow.where( :user_id_from=>from, :user_id_to=>to ).update( :accepted=>true, :blocked=>false, :rejected=>false )
  end

  # 사용자 'from'이 'to'에게 보낸 follow 요청을 reject 한다.
  # (요청 자체를 삭제하는것이 아니라, 단순히 무시하는 것임.
  # 요청을 보낸 상대방에겐 아직도 pending인 것처럼 보임.)
  def self.reject(from, to)
    Follow.where( :user_id_from=>from, :user_id_to=>to ).update( :accepted=>false, :blocked=>false, :rejected=>true)  
  end

  # from이 to의 follow요청을 reject 했는지 확인한다.
  def self.rejected?(from, to)
    Follow.where( :user_id_from=>from, :user_id_to=>to, :rejected=>true ).exists?
  end

  # from이 to를 block 한다.
  # to는 follow를 이미 하고 있어야 하고,
  # 앞으로 to는 from의 피드를 받아볼 수 없다.
  # 하지만 to에게 from은 여전히 follow 관계인 것처럼 보여줘야 한다.
  def self.block(from, to)
    if Follow.where( :user_id_from=>from, :user_id_to=>to ).exists?
       Follow.where( :user_id_from=>from, :user_id_to=>to ).update( :blocked=>true )
    else
      Follow.create( :user_id_from=>from, :user_id_to=>to, :blocked=>true )
    end
  end

  # from이 to를 block 하였던것을 취소한다.
  def self.unblock(from,to)
    if Follow.where( :user_id_from=>from, :user_id_to=>to, :blocked=>true ).exists?
      Follow.where( :user_id_from=>from, :user_id_to=>to ).delete 
    end
  end

  # from이 to를 block했는지 확인한다.
  def self.blocked?(from, to)
    Follow.where( :user_id_from=>from, :user_id_to=>to, :blocked=>true ).exists?
  end


end
