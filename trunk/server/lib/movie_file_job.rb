# 2012-09-11 brucewang
# Job 코드가 바뀌면 Delayed_job 을 restart시켜야 한다.
# > script/server restart
class MovieFileJob < Struct.new(#:movie_id, :bitrate => 22050, :thumb_second => 0, :thumb_width => 130)
                        :movie_id, :bitrate, :thumb_second, :thumb_width)
  def perform
    
    movie = Movie.find(movie_id)
    Delayed::Worker.logger.info("****************************")
    Delayed::Worker.logger.info("Processed the movie id : #{movie_id}")
    Delayed::Worker.logger.info("#{movie.file.path}")
    
    
    # 2012-09-11 brucewang
    # Delayed job 로그를 살펴봐서 exception을 내면서 실패하는것이 있다면
    # rails console 에서 다음과 같이 해당 에러 로그를 상세히 살펴보도록 한다.
    # job = Delayed::Job.where("last_error is not null").last
    
    
    
    dir, base = File.split(movie.file.path)
    extension = File.extname( movie.file.path )
    filename = File.basename( movie.file.path, extension )
    parentdir, currentdir = File.split(dir)
    
    #dir = File.dirname( movie.file.path )
    #extension = File.extname( movie.file.path )
    #filename = File.basename( movie.file.path, extension )
    
    # 2012-09-12 brucewang
    # Video converting 등의 작업 수행..
    #ffmpeg -i 'VID_20120822_162116.mp4'  -f mp4 -vcodec mpeg4 -ar 22050 'kkk.mp4' 
    #ffmpeg -ss 0 -i 'VID_20120822_162116.mp4' -y -vf scale=130:-1 -vframes 1 'a.png'
    #ffmpeg -i 'VID_20120822_162116.mp4'  -ar 22050 'kkk.flv' 
    
    
    params = []
    if extension!='mp4'
      Dir.mkdir(parentdir + '/mp4')
      params << "-i #{movie.file.path} -r 25 -f mp4 -vcodec mpeg4 -ar #{bitrate} #{parentdir + '/mp4/' +filename + '.mp4'}" # for mp4
    end
    if extension!='flv'
      Dir.mkdir(parentdir + '/flv')
      params << "-i #{movie.file.path} -ar #{bitrate} #{parentdir + '/flv/' +filename + '.flv'}" # for flv
    end
    Dir.mkdir(parentdir + '/thumbnail')
    params << "-ss #{thumb_second} -i #{movie.file.path} -y -vframes 1  #{parentdir + '/thumbnail/' +filename + '.png'}" # for thumbnail
    params.each{|parameters|
      # ffmpeg을 사용해서 동영상 변환과 thumbnail 이미지 추출 작업을 진행.
      system("ffmpeg "+parameters)
      Delayed::Worker.logger.info(parameters)
    }
    # 2012-10-05 brucewang
    # Thumbnail 위치는 Movie 모델의 file_url 메서드가 알아서 반환하도록 하였음.
    Delayed::Worker.logger.info("****************************")
    
    
    # 2012-09-11 brucewang
    # 백그라운드 job이 완료되었음을 db 필드에 저장한다.
    movie.processing = false
    movie.save!
    
    # 2012-09-12 brucewang
    # 저장된  movie  파일의 정보는 다음과 같이 보이게 된다...
    #
    #m = Movie.first
    # => #<Movie _id: 505034476a87459267000003, _type: nil, created_at: 2012-09-12 07:05:43 UTC, updated_at: 2012-09-12 07:05:43 UTC, file_file_name: "VID_20120822_162116.mp4", file_content_type: "application/octet-stream", file_file_size: 1654641, file_updated_at: 2012-09-12 07:05:43 UTC, processing: true, name: "y3q5efrbgawegt", format: nil, post_id: nil> 
    #1.9.3p125 :025 > m.file.path
    # => "/project directory/public/uploads/movies/5050/3447/6a87/4592/6700/0003/original/VID_20120822_162116.mp4" 
    #1.9.3p125 :026 > m.file.path(:thumbnail)
    # => "/project directory/public/uploads/movies/5050/3447/6a87/4592/6700/0003/thumbnail/VID_20120822_162116.mp4" 
    
  end
end
