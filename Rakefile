P5_MAIN_CLASS = "unwrap.layout.UnwrapMultiSheet"

MIN_MEMORY_MB = "64m"
MAX_MEMORY_MB = "512m"

SOURCE_PATHS = ['src']
LIBRARY_PATH = 'libs'


require 'rake'

task :default => :build

task :build, [:main_class] do |t, args|
    args.with_defaults(:main_class => P5_MAIN_CLASS)
    
	puts "\n## Building..."
	
	start_secs = Time.now.to_f
	
	Dir.mkdir("bin") unless File.exists?("bin")
	Dir.mkdir("bin/classes") unless File.exists?("bin/classes")
	
	main_class_path = File.join(args.main_class.split('.'))
	src_paths = SOURCE_PATHS.join(':')
	
	system "javac -sourcepath \"#{src_paths}\" -cp \"#{library_classpath}\" -d bin/classes #{SOURCE_PATHS[0]}/#{main_class_path}.java"
	
	puts "## Built sketch in #{Time.now.to_f - start_secs} seconds."
	
	# Stuff for Jar which isnt working yet
	# sh "echo Main-Class: #{MAIN_CLASS}>bin/manifest"
	# sh "jar cmf bin/manifest bin/#{MAIN_CLASS}.jar -C bin/classes ."
end

task :run, [:main_class] => :build do |t, args|
    args.with_defaults(:main_class => P5_MAIN_CLASS)
	run_sketch(args.main_class)
end

# task :run_fs => :build do
# 	run_sketch true
# end

task :clean do
	rmfiles = ['bin/classes', 'bin/manifest', "bin/#{P5_MAIN_CLASS}.jar"]
	rmfiles.each do |file|
		sh "rm -rf #{file}" if File.exists?(file)
	end
end


def run_sketch(main_class)
	puts "\n## Running... Prepare for downcount.\n\n"
	
	java_args = main_class
	
	# if present
	# 	java_args += " --present --exclusive --hide-stop"
	# end
	
	system "java -Xms#{MIN_MEMORY_MB} -Xmx#{MAX_MEMORY_MB} -cp \"#{library_classpath}:bin/classes\" -Djava.library.path=\"#{library_jnipath}\" #{main_class} #{java_args}"
	
	# Hmm running from Jar isn't working yet..
	# sh "java -cp \"#{class_path}\" -jar bin/#{MAIN_CLASS}.jar #{MAIN_CLASS}"
end


def library_classpath()
	cp_jars = Dir.glob(File.join(LIBRARY_PATH, '*.jar')) # catch free floating jars
	
	library_dirs.each do |dir|
		# eventually work this in
		# if File.exists?("#{dir}/export.txt")
		
		cp_jars += Dir.glob(File.join(dir, '*.jar'))
	end
	
	return cp_jars.join(':')
end

def library_jnipath()
	jni_dirs = [] # Native Library Folders
	
	library_dirs.each do |dir|
		jni_files = Dir.glob(File.join(dir, '*.jnilib'))
		
		if jni_files.length > 0
			jni_dirs << dir
		end
	end
	
	return jni_dirs.join(':')
end

def library_dirs()
	return Dir.glob(File.join(LIBRARY_PATH, '**', 'library'))
end
