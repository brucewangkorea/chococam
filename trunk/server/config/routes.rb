Chococam::Application.routes.draw do
  # 2012-08-30 brucewang
  # 기본적으로welcome 페이지가 나오도록 한다.
  # welcome 페이지에는 사용자가 로그인 상태인가 아닌가에 따라
  # 로그인 페이지로 연결하는 링크가 나오거나, logout 링크가 나오게 된다.
  root :to => 'welcome#index'
  #get "welcome/index"

  # 2012-08-30 brucewang
  # omniAuth 에 의한 callback, 에를들어 Facebook인증후 Facebook이 callback을 하면
  # 어떤 controller가 처리해야 하는지를 지정해 준다.
  devise_for :users, :controllers => { :omniauth_callbacks => "users/omniauth_callbacks" }

  # 2012-08-30 brucewang
  # Devise가 인증을 하도록 하는URL과 그 해당 controller가 어떤 것인지
  # 매핑 해 준다.
  devise_scope :user do
    #get '/users/auth/:provider' => 'users/omniauth_callbacks#passthru'
    get "register"  => "devise/registrations#new" 
    get "login"  => "devise/sessions#new"    
    get "logout" => "devise/sessions#destroy"
  end
 
  # API 처리용 route..
  namespace :api do
    namespace :v1  do

      # 2012-08-30 brucewang
      # auth token을 사용할 수 있도록 하기 위해 route를 지정해 준다.
      # 상세한 내용은 다음의 튜토리얼을 참고.
      # http://matteomelani.wordpress.com/2011/10/17/authentication-for-mobile-devices/
      resources :tokens, :only => [:create, :destroy]
      
      resources :users, :only => [:create, :show, :edit, :update]
      match 'users/following' => 'users#following'
      match 'users/followers' => 'users#followers'
      match 'users/follow_requests' => 'users#follow_requests'
      match 'users/following_count' => 'users#following_count'
      match 'users/followers_count' => 'users#followers_count'
      match 'users/follow' => 'users#follow'
      match 'users/unfollow' => 'users#unfollow'
      match 'users/accept_follow' => 'users#accept_follow'
      match 'users/reject_follow' => 'users#reject_follow'
      match 'users/block_follow' => 'users#block_follow'
      match 'users/unblock_follow' => 'users#unblock_follow'
      match 'users/find_by_fbid' => 'users#find_by_fbid'
  
      # 2012-09-13 brucewang
      resources :posts, :only => [:index, :new, :create]
      match 'posts/show' => 'posts#show'
      match 'posts/popular' => 'posts#popular'
      match 'posts/of_friends' => 'posts#of_friends'
      match 'posts/comments' => 'posts#comments'
      
      match 'posts/create_or_update_comment' => 'posts#create_or_update_comment'
      match 'posts/create_comment' => 'posts#create_comment'
      match 'posts/delete_comment' => 'posts#delete_comment'
      
      match 'posts/like_post' => 'posts#like_post'
      match 'posts/like_comment' => 'posts#like_comment'
      match 'posts/unlike' => 'posts#unlike'
      
      resources :action_feeds, :only => [:index]
    end
  end
  
  

  # The priority is based upon order of creation:
  # first created -> highest priority.

  # Sample of regular route:
  #   match 'products/:id' => 'catalog#view'
  # Keep in mind you can assign values other than :controller and :action

  # Sample of named route:
  #   match 'products/:id/purchase' => 'catalog#purchase', :as => :purchase
  # This route can be invoked with purchase_url(:id => product.id)

  # Sample resource route (maps HTTP verbs to controller actions automatically):
  #   resources :products

  # Sample resource route with options:
  #   resources :products do
  #     member do
  #       get 'short'
  #       post 'toggle'
  #     end
  #
  #     collection do
  #       get 'sold'
  #     end
  #   end

  # Sample resource route with sub-resources:
  #   resources :products do
  #     resources :comments, :sales
  #     resource :seller
  #   end

  # Sample resource route with more complex sub-resources
  #   resources :products do
  #     resources :comments
  #     resources :sales do
  #       get 'recent', :on => :collection
  #     end
  #   end

  # Sample resource route within a namespace:
  #   namespace :admin do
  #     # Directs /admin/products/* to Admin::ProductsController
  #     # (app/controllers/admin/products_controller.rb)
  #     resources :products
  #   end

  # You can have the root of your site routed with "root"
  # just remember to delete public/index.html.
  # root :to => 'welcome#index'

  # See how all your routes lay out with "rake routes"

  # This is a legacy wild controller route that's not recommended for RESTful applications.
  # Note: This route will make all actions in every controller accessible via GET requests.
  # match ':controller(/:action(/:id))(.:format)'
end
