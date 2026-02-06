VERSION = 1.1.3

DESTINATION_DIR = $(APPDATA)/ATLauncher/instances/Minecraft12110withFabric/mods
DESTINATION_FILE = $(APPDATA)/ATLauncher/instances/Minecraft12110withFabric/mods/12th-client-$(VERSION).jar
FILE_TO_MOVE = C:/Users/Noadsch12/Documents/12th-Client/build/libs/12th-client-$(VERSION).jar
ATLAUNCHER = C:\\Users\\Noadsch12\\AppData\\Roaming\\ATLauncher\\ATLauncher.exe

.PHONY: all remove_old move build start

all: headers build footer remove_old move start

headers:
	python src/main/java/com/noadsch12/add_lpgl_header.py

check:
	echo $(VERSION)

move: build
	rm -f $(DESTINATION_FILE)
	mv $(FILE_TO_MOVE) $(DESTINATION_FILE)

build:
	./gradlew.bat clean build

footer:
	python src/main/java/com/noadsch12/code_lines_counter.py

start:
	$(ATLAUNCHER) --launch "Minecraft12110withFabric" --no-launcher

remove_old:
	rm -f $(DESTINATION_DIR)/12th-client-*.jar