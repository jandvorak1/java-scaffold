# Global settings
MAKEFLAGS+=--no-print-directory

# Project settings
PROJECT_SLUG:=java-scaffold
TARGET_DIR:=target
METADATA_FILE:=reachability-metadata.json

# OS detection
ifeq ($(shell echo "check_quotes"),"check_quotes")
    OS_RAW:=Windows
    SEP:=\\
else
    OS_RAW:=$(shell uname -s 2>/dev/null || echo unknown)
    SEP:=/
endif

# OS name normalization
ifeq ($(OS_RAW),Linux)
    OS_NAME := linux
else ifeq ($(OS_RAW),Darwin)
    OS_NAME := macos
else ifeq ($(OS_RAW),Windows)
    OS_NAME := windows
else ifeq ($(OS_RAW),FreeBSD)
    OS_NAME := freebsd
else ifeq ($(OS_RAW),OpenBSD)
    OS_NAME := openbsd
else ifeq ($(OS_RAW),NetBSD)
    OS_NAME := netbsd
else ifeq ($(OS_RAW),SunOS)
    OS_NAME := solaris
else
    OS_NAME := $(shell printf '%s' "$(OS_RAW)" | tr '[:upper:]' '[:lower:]')
endif

# Architecture detection
ifeq ($(OS_RAW),Windows)
    ifdef PROCESSOR_ARCHITEW6432
        ARCH_RAW := $(PROCESSOR_ARCHITEW6432)
    else ifdef PROCESSOR_ARCHITECTURE
        ARCH_RAW := $(PROCESSOR_ARCHITECTURE)
    else
        ARCH_RAW := unknown
    endif
else
    ARCH_RAW := $(shell uname -m 2>/dev/null || echo unknown)
endif

# Architecture name normalization
ifeq ($(ARCH_RAW),x86_64)
    ARCH_NAME := amd64
else ifeq ($(ARCH_RAW),amd64)
    ARCH_NAME := amd64
else ifeq ($(ARCH_RAW),AMD64)
    ARCH_NAME := amd64
else ifeq ($(ARCH_RAW),x86)
    ARCH_NAME := 386
else ifeq ($(ARCH_RAW),i386)
    ARCH_NAME := 386
else ifeq ($(ARCH_RAW),i686)
    ARCH_NAME := 386
else ifeq ($(ARCH_RAW),aarch64)
    ARCH_NAME := arm64
else ifeq ($(ARCH_RAW),arm64)
    ARCH_NAME := arm64
else ifeq ($(ARCH_RAW),ARM64)
    ARCH_NAME := arm64
else ifeq ($(ARCH_RAW),armv7l)
    ARCH_NAME := arm
else ifeq ($(ARCH_RAW),armv6l)
    ARCH_NAME := arm
else ifeq ($(ARCH_RAW),arm)
    ARCH_NAME := arm
else
    ARCH_NAME := $(shell printf '%s' "$(ARCH_RAW)" | tr '[:upper:]' '[:lower:]')
endif

# C library detection (Linux only)
ifeq ($(OS_NAME),linux)
    ifneq ($(shell ldd --version 2>&1 | grep -i musl),)
        LIBC := musl
    else ifneq ($(shell getconf GNU_LIBC_VERSION 2>/dev/null),)
        LIBC := gnu
    else ifneq ($(shell ls /lib/ld-musl-*.so.1 /usr/lib/ld-musl-*.so.1 2>/dev/null),)
        LIBC := musl
    else
        LIBC := gnu
    endif
else
    LIBC := none
endif

# Platform string construction
ifeq ($(OS_NAME),linux)
    PLATFORM := $(OS_NAME)-$(ARCH_NAME)-$(LIBC)
else
    PLATFORM := $(OS_NAME)-$(ARCH_NAME)
endif

# Java and jpackage detection
ifeq ($(OS_NAME),windows)    
    JAVA:=$(JAVA_HOME)$(SEP)bin$(SEP)java
    JDEPS:=$(JAVA_HOME)$(SEP)bin$(SEP)jdeps
    JPACKAGE:=$(JAVA_HOME)$(SEP)bin$(SEP)jpackage
    POWERSHELL:=powershell
    MAVEN:=mvn
else
    JAVA:=java
    JPACKAGE:=jpackage
    JDEPS:=jdeps
    POWERSHELL:=
    MAVEN:=mvn
endif

# Shell module settings
SHELL_MODULE:=shell-ui-webapp
SHELL_DIR:=$(SHELL_MODULE)
SHELL_TARGET_DIR:=$(SHELL_DIR)$(SEP)$(TARGET_DIR)
SHELL_DIST_DIR:=$(SHELL_TARGET_DIR)$(SEP)dist
SHELL_DEPS_DIR:=$(SHELL_TARGET_DIR)$(SEP)dependency
SHELL_NATIVE_IMAGE_DIR:=$(SHELL_MODULE)$(SEP)src$(SEP)main$(SEP)resources$(SEP)META-INF$(SEP)native-image
SHELL_NATIVE_BUILD_DIR:=$(SHELL_TARGET_DIR)$(SEP)native-build
SHELL_AGENT_OUTPUT_DIR:=$(SHELL_TARGET_DIR)$(SEP)native$(SEP)agent-output$(SEP)main
SHELL_MAIN_CLASS:=cz.cdcargo.javascaffold.shell.ui.webapp.Main
SHELL_EXTRA_MODULES?=jdk.localedata
SHELL_JAVA_VERSION:=25

# Default commands
.PHONY: all
all: dist

# Cleaning the application
clean:	
	@echo "Cleaning..."
	@$(MAVEN) clean -P$(PLATFORM)	
	@echo "Cleaning done."
	
# Building the application
build: clean
	@echo "Building..."
	@$(MAVEN) install -DskipTests -P$(PLATFORM)
	@echo "Building done."

# Building the application natively
nbuild: clean
	@echo "Building natively..."		
	@$(MAVEN) compile package -Pnative -DskipTests -P$(PLATFORM)
	@echo "Building natively done."  

# Running the application with agent
agent: build
	@echo "Running with agent..."
	@$(JAVA) -version		
	@$(MAVEN) -pl "$(SHELL_MODULE)" exec:exec@java-agent -Pnative -Dagent=false -DskipTests -DskipNativeBuild=true -Dexec.executable="$(JAVA)" -Dexec.jvmArgs="--enable-native-access=ALL-UNNAMED"
	@echo "Running with agent done."          

# Running the application, run with arguments: make run ARGS="arg1 arg2"
run: build
	@echo "Running..."		
	@$(MAVEN) -pl "$(SHELL_MODULE)" -q -Dexec.executable="$(JAVA)" -P$(PLATFORM) -Dexec.args="--enable-native-access=ALL-UNNAMED -cp %classpath $(SHELL_MAIN_CLASS) $(ARGS)" -Dexec.inheritIo=true -Dexec.classpathScope=runtime exec:exec 
	@echo "Running done."

# Distributing the application	
dist: build
	@echo "Distributing..."	
ifeq ($(OS_NAME),windows)	
	@$(POWERSHELL) -NoProfile -ExecutionPolicy Bypass -Command "if (Test-Path -LiteralPath '$(SHELL_DIST_DIR)') { Remove-Item -LiteralPath '$(SHELL_DIST_DIR)' -Recurse -Force }; if (-not (Test-Path -LiteralPath '$(SHELL_DEPS_DIR)')) { New-Item -ItemType Directory -Path '$(SHELL_DEPS_DIR)' -Force | Out-Null }; Remove-Item -Path '$(SHELL_DEPS_DIR)$(SEP)$(PROJECT_SLUG)-$(SHELL_MODULE)-*.jar' -Force -ErrorAction SilentlyContinue; Copy-Item -Path '$(SHELL_TARGET_DIR)$(SEP)$(PROJECT_SLUG)-$(SHELL_MODULE)-*.jar' -Destination '$(SHELL_DEPS_DIR)$(SEP)' -Force; $$jar = Get-ChildItem -Path '$(SHELL_DEPS_DIR)' -Filter '$(PROJECT_SLUG)-$(SHELL_MODULE)-*.jar' | Sort-Object LastWriteTime -Descending | Select-Object -First 1; if ($$null -eq $$jar) { throw 'Application jar not found.' }; $$modules = (& '$(JDEPS)' --ignore-missing-deps --multi-release $(SHELL_JAVA_VERSION) --print-module-deps --class-path '$(SHELL_DEPS_DIR)$(SEP)*' $$jar.FullName | Select-Object -Last 1); if ($$LASTEXITCODE -ne 0) { exit $$LASTEXITCODE }; if (-not $$modules) { throw 'jdeps did not return modules.' }; $$modules = $$modules.Trim() + ',$(SHELL_EXTRA_MODULES)'; & '$(JPACKAGE)' --input '$(SHELL_DEPS_DIR)' --name '$(PROJECT_SLUG)' --main-jar $$jar.Name --main-class '$(SHELL_MAIN_CLASS)' --type app-image --dest '$(SHELL_DIST_DIR)' --icon '$(PROJECT_SLUG).ico' --add-modules $$modules --java-options '--enable-native-access=ALL-UNNAMED -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8'; exit $$LASTEXITCODE"
else
	@rm -rf "$(SHELL_DIST_DIR)"		
	@cp -r "$(SHELL_TARGET_DIR)$(SEP)$(PROJECT_SLUG)-$(SHELL_MODULE)"-*.jar "$(SHELL_DEPS_DIR)" > /dev/null
	@for f in "$(SHELL_DEPS_DIR)$(SEP)$(PROJECT_SLUG)-$(SHELL_MODULE)"-*.jar; do modules=$$("$(JDEPS)" --ignore-missing-deps --multi-release $(SHELL_JAVA_VERSION) --print-module-deps --class-path "$(SHELL_DEPS_DIR)$(SEP)*" "$$f"); modules="$$modules,$(SHELL_EXTRA_MODULES)"; "$(JPACKAGE)" --input "$(SHELL_DEPS_DIR)" --name "$(PROJECT_SLUG)" --main-jar "$$(basename "$$f")" --main-class "$(SHELL_MAIN_CLASS)" --type app-image --dest "$(SHELL_DIST_DIR)" --icon "$(PROJECT_SLUG).png" --add-modules "$$modules" --java-options "--enable-native-access=ALL-UNNAMED -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8"; done
endif		
	@echo "Distributing done."	

# Distributing the application natively	
ndist: nbuild
	@echo "Distributing natively..."	
ifeq ($(OS_NAME),windows)	
	@$(POWERSHELL) -NoProfile -ExecutionPolicy Bypass -Command "if (Test-Path -LiteralPath '$(SHELL_DIST_DIR)') { Remove-Item -LiteralPath '$(SHELL_DIST_DIR)' -Recurse -Force }; New-Item -ItemType Directory -Path '$(SHELL_DIST_DIR)' -Force | Out-Null; Copy-Item -Path '$(SHELL_NATIVE_BUILD_DIR)$(SEP)*' -Destination '$(SHELL_DIST_DIR)$(SEP)' -Recurse -Force; & editbin /SUBSYSTEM:WINDOWS '$(SHELL_DIST_DIR)$(SEP)$(PROJECT_SLUG).exe' | Out-Null; exit $$LASTEXITCODE"
else
	@rm -rf "$(SHELL_DIST_DIR)"		
	@mkdir -p "$(SHELL_DIST_DIR)"
	@cp -r "$(SHELL_NATIVE_BUILD_DIR)"/. "$(SHELL_DIST_DIR)" > /dev/null	
endif		
	@echo "Distributing natively done."	    
    
# Executing the application, execute with arguments: make exec ARGS="arg1 arg2"
exec: dist
	@echo "Executing..."
ifeq ($(OS_NAME),windows)			
	@$(POWERSHELL) -NoProfile -ExecutionPolicy Bypass -Command "& '$(SHELL_DIST_DIR)$(SEP)$(PROJECT_SLUG)$(SEP)$(PROJECT_SLUG).exe' $(ARGS); exit $$LASTEXITCODE"
else
	@"$(SHELL_DIST_DIR)$(SEP)$(PROJECT_SLUG)$(SEP)bin$(SEP)$(PROJECT_SLUG)" $(ARGS)
endif
	@echo "Executing done."	

# Executing the application natively, execute with arguments: make nexec ARGS="arg1 arg2"
nexec: ndist
	@echo "Executing natively..."
ifeq ($(OS_NAME),windows)			
	@$(POWERSHELL) -NoProfile -ExecutionPolicy Bypass -Command "& '$(SHELL_DIST_DIR)$(SEP)$(PROJECT_SLUG).exe' $(ARGS); exit $$LASTEXITCODE"
else
	@"$(SHELL_DIST_DIR)$(SEP)$(PROJECT_SLUG)" $(ARGS)
endif
	@echo "Executing natively done."    
	
# Testing the application	
test: build
	@echo "Testing..."
	@$(MAVEN) test -P$(PLATFORM)
	@echo "Testing done."

# Integration testing the application	
it:
	@echo "Integration testing..."
	@$(MAVEN) integration-test -P$(PLATFORM)
	@echo "Integration testing done."

# Documenting the application
doc: build
	@echo "Documenting..."
	@$(MAVEN) javadoc:aggregate -P$(PLATFORM)
	@echo "Documenting done."

# Versions checking the application
version:
	@echo "Versions checking..."
	@$(MAVEN) versions:display-dependency-updates -P$(PLATFORM)
	@echo "Versions checking done."		

# Platform checking the application
platform:
	@echo "Platform checking..."
	@echo $(PLATFORM)
	@echo "Platform checking done."

%:
	@: