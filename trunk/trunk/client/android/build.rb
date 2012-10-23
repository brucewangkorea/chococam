# encoding: utf-8
require 'rubygems'
require 'find'
require 'fileutils'
require 'gmail'




$buildresultfile = "buildresult.txt"

a = `svn log -r PREV:HEAD > lastchanges.txt`
#File.open("lastchanges.txt", "w") {|f| f.puts a}

system("android update project --target 7 --path ./ ")
system("cp ./lib/*jar ./libs")
system("svn up")
system("svn info > svninfo.txt")
$svninfo_txt = File.read("svninfo.txt")
$svninfo_txt =~ /Last Changed Rev: (\d*)/
$revision = $1


File.open("last_svn_revision.txt", File::RDONLY|File::CREAT)
$last_svn_revision = File.read("last_svn_revision.txt")
$last_svn_revision = "0" if $last_svn_revision.length==0
File.open("last_svn_revision.txt", "w") {|f| f.puts $revision}



time = Time.new
$date_str = time.strftime("%Y-%m-%d_%H%M%S")

$apk_file_name = "ProjectG_r#{$revision}_#{$date_str}.apk"
puts $apk_file_name


def replaceall_file( file, regex, replace )
	text = File.read(file)
	result = text.gsub(/#{regex}/, replace)
	File.open(file, "w") {|f| f.puts result}
end


$g_version_code
$g_version_name_major
$g_version_name_minor

def update_versioncode( file  )
	text = File.read(file)

	# VersionCode 증가.
	b = "android:versionCode=\"(\\d*)\""
	c = /#{b}/
	d = text =~ c
	return if b.nil?
	e = $1.to_i
	e = e+1
	$g_version_code=e.to_s

	result = text.gsub(/android:versionCode="\d*"/, "android:versionCode=\"#{e}\"")

	# Minor 버전 증가.
	c = /android:versionName="(\d*).(\d*)"/
	d = text =~ c
	return if b.nil?
	e = $2.to_i
	e = e+1
	$g_version_name_minor = e.to_s

	majorversion = $1
	$g_version_name_major = majorversion.to_s

	text = result
	result = text.gsub(/android:versionName="\d*.\d*"/, "android:versionName=\"#{majorversion}.#{e}\"")


	File.open(file, "w") {|f| f.puts result}

	# SVN 에 커밋.
	system("svn ci -m \"Version Up\" #{file}")
end


def check_string_exist( file, regex )
	text = File.read(file).unpack("C*").pack("U*")
	return (text =~ /#{regex}/) != nil
end



def build(apkfilename)
	system( "svn up")
	
	system("rm -rf ./gen")
	system("rm -rf ./bin")


	# b = "android:versionCode=\"(\\d*)\""
	# c = /#{b}/
	# d = a =~ b
	# e = $1.to_i

	#replaceall_file( "./AndroidManifest.xml", "android:versionCode=\"d*\"", "return u#{index};")




	
  # 빌드.
	system("ant release > #{$buildresultfile}")
	# 빌드가 종료되면  release 된 apk파일의 이름을 바꾸어 저장합니다.
	system("mv ./bin/*-release.apk ./bin/#{apkfilename}")
end




def send_build_result_to_mail( emailto, success, svninfo, file_to_atach )
	gmail = Gmail.new('redmine@chocopepper.com', 'redmine108')
	gmail.deliver do
		to emailto
                
        if success
            subject "GamChen 빌드 성공 VersionCode#{$g_version_code},VersioName=#{$g_version_name_major}.#{$g_version_name_minor}"
            text_part do
                body "빌드된 파일은 다음 링크에서 받으실 수 있습니다.\nhttp://64.23.68.147/#{$apk_file_name}\n다음은 svn revision에 대한 빌드 결과 입니다.\n#{svninfo}"
            end
        else
		    subject "GamChen 빌드 실패"
		    text_part do
				body "다음 svn revision에 대한 빌드 결과 입니다.\n#{svninfo}"
		    end
        end

		add_file file_to_atach if file_to_atach
		add_file "lastchanges.txt"
	end
	
	gmail.logout
end







if( $revision.to_i <= $last_svn_revision.to_i)
	puts "Nothing new"
	return
end

update_versioncode( "./AndroidManifest.xml" )
build( $apk_file_name )

system( "mv ./bin/#{$apk_file_name} /var/www/redmine/public/#{$apk_file_name}" )

success = check_string_exist($buildresultfile, "BUILD SUCCESSFUL")

send_build_result_to_mail("<all@chocopepper.com>, <moduad@gmail.com>", success, $svninfo_txt, $buildresultfile)


