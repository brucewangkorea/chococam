class MoviesController < ApplicationController\

  skip_before_filter :verify_authenticity_token
  
  def new
    @movie = Movie.new()
    @movie.post = Post.new()
  end

  # CURL 로는 다음과 같이 테스트
  # curl -F "movie[file]=@./image001.gif" -F "movie[name]=api" "http://localhost:3000/movies"
  def create
    
    @movie = Movie.new(params[:movie])
    #@movie.post = params[:post]
    @movie.save
    
    logger.info "----------------------------"
    logger.info params
    logger.info "----------------------------"
    logger.info params[:movie][:post_attributes]
    logger.info "----------------------------"
    logger.info @movie.inspect
    logger.info "----------------------------"
    logger.info @movie.post.inspect
    logger.info "----------------------------"
  end
end
