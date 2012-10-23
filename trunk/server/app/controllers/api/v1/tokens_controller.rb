class Api::V1::TokensController  < ApplicationController
    skip_before_filter :verify_authenticity_token
    respond_to :json




    def create
      email = params[:email]
      password = params[:password]

      # 2012-08-31 brucewang
      # Support Facebook login
      facebook_id = params[:facebook_id]

      if request.format != :json
        render :status=>406, :json=>{:message=>"The request must be json"}
        return
      end

      if facebook_id.nil? and (email.nil? or password.nil?)
        render :status=>400,
          :json=>{:message=>"The request must contain the user 'email and password', or Facebook id."}
        return
      end

      unless facebook_id.nil?
        @user=User.find_by_fb_user_id(facebook_id)
      else
        @user=User.find_by_email(email.downcase)
      end

      if @user.nil?
        logger.info("User #{email} failed signin, user cannot be found.")
        render :status=>401, :json=>{:message=>"Invalid email or passoword."}
        return
      end

      # http://rdoc.info/github/plataformatec/devise/master/Devise/Models/TokenAuthenticatable
      @user.ensure_authentication_token!

      if ( facebook_id.nil? and not @user.valid_password?(password) )
        logger.info("User #{email} failed signin, password \"#{password}\" is invalid")
        render :status=>401, :json=>{:message=>"ooops Invalid email or password."}
      else
        render :status=>200, :json=>{:token=>@user.authentication_token}
      end
    end




    def destroy
      @user=User.find_by_authentication_token(params[:id])
      if @user.nil?
        logger.info("Token not found.")
        render :status=>404, :json=>{:message=>"Invalid token."}
      else
        @user.reset_authentication_token!
        render :status=>200, :json=>{:token=>params[:id]}
      end
    end

end

