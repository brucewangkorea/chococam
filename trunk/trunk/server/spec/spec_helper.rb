# This file is copied to spec/ when you run 'rails generate rspec:install'
ENV["RAILS_ENV"] ||= 'test'
require File.expand_path("../../config/environment", __FILE__)
require 'rspec/rails'
require 'rspec/autorun'
require 'capybara/rspec'


# Requires supporting ruby files with custom matchers and macros, etc,
# in spec/support/ and its subdirectories.
Dir[Rails.root.join("spec/support/**/*.rb")].each {|f| require f}


# 2012-09-14 brucewang
# Mongoid 사용시 변경해야 할 사항들이 있는데, 상세한 내용은 다음 링크를 참고.
# http://procbits.com/2011/08/18/using-mongoid-with-rspec/
RSpec.configure do |config|
  # ## Mock Framework
  #
  # If you prefer to use mocha, flexmock or RR, uncomment the appropriate line:
  #
  # config.mock_with :mocha
  # config.mock_with :flexmock
  # config.mock_with :rr

  # Remove this line if you're not using ActiveRecord or ActiveRecord fixtures
  #config.fixture_path = "#{::Rails.root}/spec/fixtures"

  # If you're not using ActiveRecord, or you'd prefer not to run each of your
  # examples within a transaction, remove the following line or assign false
  # instead of true.
  #config.use_transactional_fixtures = true
  config.use_transactional_fixtures = false

  # If true, the base class of anonymous controllers will be inferred
  # automatically. This will be the default behavior in future versions of
  # rspec-rails.
  config.infer_base_class_for_anonymous_controllers = false

  # Run specs in random order to surface order dependencies. If you find an
  # order dependency and want to debug it, you can fix the order by providing
  # the seed, which is printed after each run.
  #     --seed 1234
  config.order = "random"

  # 2012-09-18 brucewang
  # 여러개의 ORM에 대해 자동으로 테스트 데이터를 지워주도록 설정.
  config.before(:suite) do
      DatabaseCleaner[:active_record].strategy = :truncation
      DatabaseCleaner[:mongoid].strategy = :truncation
  end

  config.before(:each) do
      DatabaseCleaner[:active_record].start
      DatabaseCleaner[:mongoid].start
  end

  config.after(:each) do
      DatabaseCleaner[:active_record].clean
      DatabaseCleaner[:mongoid].clean
  end
end
