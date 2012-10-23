class Users::OmniauthCallbacksController < Devise::OmniauthCallbacksController
  def facebook
    @user = User.find_or_create_from_auth_hash(request.env["omniauth.auth"])
    sign_in_and_redirect @user
  end
  
  
  
  
  
  
  
  
  # # 2012-08-30  brucewang
  # # Mobile Web browser일 경우에 대한 처리.
  # if mobile?
  #   @user = User.find_for_facebook_oauth(request.env["omniauth.authMOBILE"], current_user)
  # else
  #   @user = User.find_for_facebook_oauth(request.env["omniauth.auth"], current_user)
  # end

  #def facebook
  #  # You need to implement the method below in your model (e.g. app/models/user.rb)
  #  @user = User.find_for_facebook_oauth(request.env["omniauth.auth"], current_user)
  #
  #  if @user.persisted?
  #    flash[:notice] = I18n.t "devise.omniauth_callbacks.success", :kind => "Facebook"
  #    sign_in_and_redirect @user, :event => :authentication
  #  else
  #    session["devise.facebook_data"] = request.env["omniauth.auth"]
  #    redirect_to new_user_registration_url
  #  end
  #end

  def passthru
    render :file => "#{Rails.root}/public/404.html", :status => 404, :layout => false
    # Or alternatively,
    # raise ActionController::RoutingError.new('Not Found')
  end
end


