JAVA_HOME=/mag/android/android-studio/jre
PKG=com.wildsparx.worldstein


.PHONY: build uninstall install launch clean bl

all: build

build:
	JAVA_HOME=$(JAVA_HOME) ./gradlew build

uninstall:
	adb uninstall $(PKG)

install:
	adb install ./app/build/outputs/apk/debug/app-debug.apk

launch:
	 adb shell am start -n $(PKG)/$(PKG).MainActivity

clean:
	rm -rf build app/build .gradle .idea

bl: build uninstall install launch
