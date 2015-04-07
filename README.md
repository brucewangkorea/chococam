# chococam
Automatically exported from code.google.com/p/chococam

SNS for sharing movies between friends.

ChocoCam is SNS for sharing movies on the phone between your friends. The Web Server for this project is written in RoR and we have provided Android client source code. 

On the server side, it uses MongoDB for DBMS and uses DelayedJob? for background processes like transcoding the uploaded movie files. 

Any help and supporta like joining as a committer are welcome. 

Thanks. 

chococam 은 스마트폰 사용자들이 자유롭게 동영상을 공유할 수 있는 SNS 서비스 입니다. 

서버는 RubyOnRails?로 작성되어 있고, 샘플 클라이언트는 Android 플랫폼을 대상으로 하고 있습니다. 상세한 설명은 Wiki나 Downloads 섹션의 문서들을 참고해 주세요. 

사용중인 외부 컴포넌트 또는 오픈소스프로젝트

서버

다음의 외부 컴포넌트들은 모두 OpenSource 프로젝트들이며 본 프로젝트와는 별도 입니다. 외부 컴포넌트들은 chococam서버에서 단순 호출만을 하게 되며, 이들 오픈소스의 수정은 하지 않았습니다. 

* ffmpeg : 동영상파일의 변환과, 동영상의 첫 화면 이미지를 추출하는데 사용하는 툴. 

* imagemagick : 그림 파일의 다양한 이미지프로세싱을 도와주는 툴 

* MySql : 데이터 저장을 위해 사용되는 DBMS 

* MongoDB : NoSQL 기반의 데이터 저장소 

* Devise : Rails에서 쉽게 사용자인증을 할 수 있도록 도와주는 Ruby Gem 

* mongoid : Rails에서 MongoDB를 쉽게 사용할 수 있도록 도와주는 Ruby Gem 

* kaminari : Rails에서 쉽게 여러 페이지에 달하는 DB결과를 분리하여 활용할 수 있도록 도와주는 RubyGem? 

* rmagick : Rails에서 쉽게 imagemagick의 기능을 사용할 수 있도록 도와주는 RubyGem? 

* mongoid-paperclip : Rails에서 쉽게 File upload를 할 수 있도록 도와주는 paperclip이라는 Ruby gem의 기능을, 다시 MongoDB에서 사용할 수 있도록 도와주는 Ruby Gem 

* delayed_job_mongoid : Rails에서 쉽게 Background job을 수행할 수 있도록 도와주는 RubyGem? 

* RSpec : Rails에서 쉽게 자동화 테스트를 할 수 있도록 도와주는 Ruby Gem 

클라이언트

* Facebook Android Library : Android app에서 Facebook의 기능을 사용할 수 있도록 도와주는 Facebook사에서 제공하는 라이브러리. 현재 샘플 안드로이드 프로젝트에서는 위 라이브러리 소스 자체를 프로젝트에 그대로 수정없이 포함하고 있습니다. 

* pulltorefresh : Android app의 PullToRefresh? 기능을 구현해 주는 오픈소스 (Apache 2.0) https://github.com/chrisbanes/Android-PullToRefresh http://www.senab.co.uk/contact/ 

* viewpagerindicator : Android app의 View pager UI를 구현해주는 오픈소스 (Apache 2.0) http://viewpagerindicator.com 

* android-support-v4.jar : PullToRefresh? 등에서 참조하게 되는 Google 제공 Android SDK에 포함. 

* commons-cli-.jar, commons-io-.jar : Http request등을 위해 참조하는 Apache 재단 제공 라이브러리 

* gson.jar : Google 이 제공하는 Json parser 
