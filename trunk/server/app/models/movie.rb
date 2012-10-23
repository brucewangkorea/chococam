#
# 2012-09-03 brucewang
# Created Class
#
# 동영상
#
class Movie
	include Mongoid::Document
	include Mongoid::Timestamps
	
	#embedded_in :post #, index: true #, class_name: "Post"
	belongs_to :post, index: true
  
  # CarrierWave를 사용한 파일 업로드 사용.
  # https://github.com/jnicklas/carrierwave-mongoid
  #attr_accessible :file
  #field :file_processing, type: Boolean # 백그라운드 작업중인가?
  #
  #mount_uploader :file, MovieUploader 
  #process_in_background :file
  
  
  attr_accessible :file, :name, :remote_url, :remote_source, :thumbnails
  
  def file_url
	  dir, base = File.split( self.file.url )
    extension = File.extname( self.file.url )
    filename = File.basename( self.file.url, extension )
    parentdir, currentdir = File.split(dir)
  	{ 
  		"mp4" => parentdir + '/mp4/' + "#{filename}.mp4",
  		"flv" => parentdir + '/flv/' + "#{filename}.flv",
  		"thumbnail" => parentdir + '/thumbnail/' + "#{filename}.png",
  	}
  end
  
  
  
  
  
  
  # Paperclip 을 사용한 파일 업로드.
  include Mongoid::Paperclip
  has_mongoid_attached_file :file, 
    :url => '/uploads/:class/:id_partition/:style/:filename',
    :default_url => 'missing_:style.png',
    :path => ':rails_root/public/uploads/:class/:id_partition/:style/:filename'
    
    
    
    
    
  # 2012-09-11 brucewang 
  # 백그라운드 작업중인가? 의 여부를 알려주는 플래그
  # 이 값이 true라면 아직 외부에 공유될 수 없는 상태임을 나타낸다.
  field :processing, type: Boolean, default: false 
  #
  field :name, type: String
	# 동영상 자체의 포멧.
	#field :format,				type: String



  # 2012-09-05 brucewang
  # 동영상의 title==name
  def title
    name
  end

  # 2012-09-25 brucewang
  # 외부 동영상일 경우 그 링크 정보를 저장.
  field :remote_url, type: String

  # 2012-09-25 brucewang
  # 여러개의 썸네일 이미지가 존재할 경우 그 정보를 저장.
  field :thumbnails, type: Array, default: []

  # 2012-09-25 brucewang
  # 외부 동영상 인 경우 그 동영상 서비스의 이름을 저장. 
  field :remote_source, type: String

  # 2012-09-26 brucewang
  # Post의view_count와 별도로, 동영상은 외부에 공유되서  play되는 경우도 있으므로
  # 동영상만의  play count를 관리하는것이 좋겠음.
  field :play_count, type: Integer, default: 0




  
  
  
  
  
  # 2012-09-10 brucewang
  # before_XXXXXXXX_post_process 함수는,
  # 파일 업로드 후 이뤄지는 styles옵션에서 지정한 thumbnail 등의 이미지 프로세싱이 
  # 이루어 지기 전에 호출되는 함수임...
  before_file_post_process do |datafile|
    logger.info "------------------------------------"
    logger.info "!!! before_file_post_process !!!!"
    logger.info "------------------------------------"
    
    # 2012-09-11 brucewang
    # 뭔가 background 작업이 진행중임을 db에 저장..
    datafile.processing = true
    
    # 2012-09-11 brucewang
    # 이 함수에서 false를 반환하면 paperclip은 style 작업을 하지 않음.
    false 
  end
 
  # 2012-09-10 brucewang
  # 이 모델이 DB에 완전히 저장된 후 수행되는 함수.
  after_save do |datafile| 
    logger.info "------------------------------------"
    logger.info "!!! after_save !!!!"
    logger.info "------------------------------------"
    
    # 2012-09-11 brucewang
    # processing 이 false인 것은 이미 background 작업이 완료되면서 save된 것이므로
    # 이때는 다시 background job을 실행하지 않도록 한다.
    if datafile.processing
      
      # bitrate
      bitrate = 22050
      # 동영상에서 thumbnail 이미지를 추출할 시간 위치(처음으로부터 몇 초 후)
      thumb_second = 0
      # thumbnail 이미지의 폭.
      thumb_width = 130
      
      
      # 2012-09-10 brucewang
      # MovieFileJob 클래스는 'lib' 디렉토리에 정의되어 있으며
      # DelayedJob에서 사용할 수 있는 Job 을 정의하고 있다.
      Delayed::Job.enqueue( 
        MovieFileJob.new(datafile.id, bitrate, thumb_second, thumb_width), 
        :priority=>Delayed::Worker.default_priority, 
        :run_at=>2.seconds.from_now )#Time.now )
    end
  end

end


