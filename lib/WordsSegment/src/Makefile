TYPE=release

CC=gcc
CFLAGS=-fPIC -Wall -lm

ifeq ($(TYPE),debug)
	CFLAGS+=-g3 -pg -DDEBUG
else
	ifeq ($(TYPE),release)
		CFLAGS+=-O3
	else
		CFLAGS+=-g -O
	endif
endif

CXX=g++
CXXFLAGS=-fPIC -Wall -std=c++11

ifeq ($(TYPE),debug)
	CXXFLAGS+=-g3 -pg -DDEBUG
else
	ifeq ($(TYPE),release)
		CXXFLAGS+=-O3
	else
		CXXFLAGS+=-g -O
	endif
endif

LD=g++
LDFLAGS=-std=c++11

AR=ar
ARFLAGS=rc

RM=rm
MV=mv

############Parameter Setting###########

#sub directory && exe directory

MODDIRS=

EXEDIRS=

SUBDIRS=$(MODDIRS) $(EXEDIRS)

#source files directory, default is current directory
#be sure that your source files is under this directory
SRC_DIR=. ./jieba ./jieba/Limonp ./jieba/Husky

#included header files directory, you can specify more than one directory
#names here seperated by whitespace, e.g. INCDIR=-I. -I../inc1 -I../inc2
#don't forget add '-I'
#INCDIR=-I../../include/utils -I../../include/imageroi -I../../include/opencv -I../../include
INCDIR=-I./ -I/usr/lib/jvm/java-8-oracle/include -I/usr/lib/jvm/java-8-oracle/include/linux
#vpath %.h

#specify the path of the library if needed
#don't forget add '-L' or '-l{libname}'
LIBS=-L./ 

#-lFaceTaggingAPI -lFaceDetection -lFaceGaborFea -lFaceCluster -lFaceEyeLocation -lopencv_highgui -lopencv_imgproc -lopencv_core -lopencv_objdetect -lopencv_features2d 

#binary files name and path
MODULE_NAME=
#jni o
#JNI_O = jni_faceTag_Interface

#static library target
STATICLIB=# ../lib/libJNI_FaceTagInterface.a

#dynamic library target
SHARELIB=/usr/lib/libTokenizer.so

SRCS:=$(wildcard $(addsuffix /*.c,$(SRC_DIR)))\
      $(wildcard $(addsuffix /*.C,$(SRC_DIR)))\
      $(wildcard $(addsuffix /*.cpp,$(SRC_DIR)))\
      $(wildcard $(addsuffix /*.CP,$(SRC_DIR)))

OBJS:=$(SRCS:%.c=%.o)
OBJS:=$(OBJS:%.C=%.o)
OBJS:=$(OBJS:%.cpp=%.o)
OBJS:=$(OBJS:%.CPP=%.o)
OBJS:=$(OBJS:%.hpp=%.o)

DEPS=$(OBJS:%.o=%.d)

#if you want to only compile the source files, write like this 'COMPILE=$(OBJS)'
COMPILE=

############Parameter Setting###########

.PHONY:

TARGET=$(MODULE_NAME) $(STATICLIB) $(SHARELIB)

all:$(COMPILE)
	@set -e;for dir in ${SUBDIRS};        \
		  do                          \
		    ${MAKE} all -C $${dir};   \
	done
	@set -e;for target in ${TARGET};      \
		  do                          \
		    ${MAKE} $${target};       \
	done

$(MODULE_NAME):$(OBJS)
	$(LD) $(LDFLAGS) $(LIBS) $^ -o $@
	@echo "  ^_^ LD $@"

static-lib:$(STATICLIB)

$(STATICLIB):$(OBJS)
	$(AR) $(ARFLAGS) $@ $^
	@echo "  ^_^ AR $@"

share-lib:$(SHARELIB)

$(SHARELIB):$(OBJS)
	$(LD) -fPIC -shared -o $@  $(LDFLAGS)  $(LIBS) $^
	@echo "  ^_^ LD $@"
debug:
	$(MAKE) TYPE=debug

clean:
	@set -e;for dir in ${SUBDIRS};        \
		  do                          \
	            ${MAKE} clean -C $${dir}; \
	done
	@$(RM) -rf $(OBJS)
	@$(RM) -rf $(STATICLIB)
	@$(RM) -rf $(SHARELIB)
	@$(RM) -rf $(DEPS)
	@$(RM) -rf *~
	@echo "  ^_^ RM ALL"

.SUFFIXES: .o .c .C .cpp .CPP

-include $(DEPS)

%.o:%.c
	if $(CC) $(INCDIR) $(CFLAGS) -MT $@ -MD -MP -MF "$(@D)/$(@F:%.o=%.td)" -c -o $@ $<; then \
           $(MV) -f "$(@D)/$(@F:%.o=%.td)" "$(@D)/$(@F:%.o=%.d)"; \
	else \
	   $(RM) -f "$(@D)/$(@F:%.o=%.td)"; exit 1; \
	fi
	@echo "  ^_^ CC $@"
	   
%.o:%.C
	if $(CC) $(INCDIR) $(CFLAGS) -MT $@ -MD -MP -MF "$(@D)/$(@F:%.o=%.td)" -c -o $@ $<; then \
           $(MV) -f "$(@D)/$(@F:%.o=%.td)" "$(@D)/$(@F:%.o=%.d)"; \
	else \
	   $(RM) -f "$(@D)/$(@F:%.o=%.td)"; exit 1; \
	fi
	@echo "  ^_^ CC $@"

%.o:%.cpp
	if $(CXX) $(INCDIR) $(CXXFLAGS) -MT $@ -MD -MP -MF "$(@D)/$(@F:%.o=%.td)" -c -o $@ $<; then \
           $(MV) -f "$(@D)/$(@F:%.o=%.td)" "$(@D)/$(@F:%.o=%.d)"; \
	else \
	   $(RM) -f "$(@D)/$(@F:%.o=%.td)"; exit 1; \
	fi
	@echo "  ^_^ CXX $@"

%.o:%.CPP
	if $(CXX) $(INCDIR) $(CXXFLAGS) -MT $@ -MD -MP -MF "$(@D)/$(@F:%.o=%.td)" -c -o $@ $<; then \
           $(MV) -f "$(@D)/$(@F:%.o=%.td)" "$(@D)/$(@F:%.o=%.d)"; \
	else \
	   $(RM) -f "$(@D)/$(@F:%.o=%.td)"; exit 1; \
	fi
	@echo "  ^_^ CXX $@"
