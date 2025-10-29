# Makefile to control Maven project in my-app/

APP_DIR = my-app
APP_NAME = my-java-app
MAIN_CLASS = com.example.App
JAR_FILE = $(APP_DIR)/target/$(APP_NAME)-1.0-SNAPSHOT.jar

.PHONY: all build run clean package runjar

all: build

build:
	cd $(APP_DIR) && mvn compile

run:
	cd $(APP_DIR) && mvn compile exec:java -Dexec.mainClass=$(MAIN_CLASS)

package:
	cd $(APP_DIR) && mvn clean package

runjar: package
	java -jar $(JAR_FILE)

clean:
	cd $(APP_DIR) && mvn clean
