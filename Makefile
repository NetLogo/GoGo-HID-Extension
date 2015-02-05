ifeq ($(origin JAVA_HOME), undefined)
  JAVA_HOME=/usr
endif

ifeq ($(origin NETLOGO), undefined)
  NETLOGO=../..
endif

ifneq (,$(findstring CYGWIN,$(shell uname -s)))
  COLON=\;
  JAVA_HOME := `cygpath -up "$(JAVA_HOME)"`
else
  COLON=:
endif

JAVAC:=$(JAVA_HOME)/bin/javac
COMMON_SRCS=$(wildcard src/common/gogoHID/*/*.java)
DAEMON_SRCS=$(wildcard src/daemon/gogoHID/*/*.java)
EXT_SRCS=$(wildcard src/extension/gogoHID/*/*.java)
RESOURCES=resources

all: gogo-daemon.jar gogo.jar

gogo.jar: $(COMMON_SRCS) $(EXT_SRCS) manifest.txt Makefile
	rm -rf classes
	mkdir -p classes
	$(JAVAC) -g -deprecation -Xlint:all -Xlint:-serial -Xlint:-path -encoding us-ascii -source 1.5 -target 1.5 -classpath $(NETLOGO)/NetLogoLite.jar:$(NETLOGO)/NetLogo.jar -d classes $(COMMON_SRCS) $(EXT_SRCS)
	jar cmf manifest.txt gogo.jar -C classes .
	pack200 --modification-time=latest --effort=9 --strip-debug --no-keep-file-order --unknown-attribute=strip gogo.jar.pack.gz gogo.jar

gogo-daemon.jar: $(COMMON_SRCS) $(DAEMON_SRCS) manifest.txt Makefile
	rm -rf classes
	mkdir -p classes
	echo $(COMMON_SRCS)
	cp -ap resources/* classes/
	$(JAVAC) -g -deprecation -Xlint:all -Xlint:-serial -Xlint:-path -encoding us-ascii -source 1.5 -target 1.5 -classpath hid4java.jar:jna.jar -d classes $(COMMON_SRCS) $(DAEMON_SRCS)
	jar cmf manifest.txt gogo-daemon.jar -C classes .

clean:
	rm -rf classes
	rm -f gogo.jar
	rm -f gogo.jar.pack.gz
	rm -f gogo-daemon.jar
