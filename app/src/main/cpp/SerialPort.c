/*
 * Copyright 2009-2011 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>

#include "SerialPort.h"

//android编译环境
#ifdef ANDROID
    #define LOG_TAG "serial_port"
	#ifdef YUNOVO_NOT_NDK
		#include <cutils/log.h>
	#else
		#include <android/log.h>
		#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
		#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
		#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
		#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
	#endif
#else  //电脑环境
	#define LOGI(...) printf(__VA_ARGS__)
	#define LOGD(...) printf(__VA_ARGS__)
	#define LOGE(...) printf(__VA_ARGS__)
#endif

//无符号
static speed_t getBaudrate(jint baudrate)
{
	switch(baudrate) {
	case 0: return B0;
	case 50: return B50;
	case 75: return B75;
	case 110: return B110;
	case 134: return B134;
	case 150: return B150;
	case 200: return B200;
	case 300: return B300;
	case 600: return B600;
	case 1200: return B1200;
	case 1800: return B1800;
	case 2400: return B2400;
	case 4800: return B4800;
	case 9600: return B9600;
	case 19200: return B19200;
	case 38400: return B38400;
	case 57600: return B57600;
	case 115200: return B115200;
	case 230400: return B230400;
	case 460800: return B460800;
	case 500000: return B500000;
	case 576000: return B576000;
	case 921600: return B921600;
	case 1000000: return B1000000;
	case 1152000: return B1152000;
	case 1500000: return B1500000;
	case 2000000: return B2000000;
	case 2500000: return B2500000;
	case 3000000: return B3000000;
	case 3500000: return B3500000;
	case 4000000: return B4000000;
	default: return -1;
	}
}

/*
 * Class:     cn_spt_serialport_SerialPort
 * Method:    open
 * Signature: (Ljava/lang/String;II)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL Java_win_wilber_commassistant_serialport_SerialPort_open
  (JNIEnv *env, jclass thiz, jstring path, jint baudrate, jint flags)
{
	LOGD( "Java_win_wilber_commassistant_serialport_SerialPort_open" );
	int fd;
	speed_t speed;
	jobject mFileDescriptor;

	/* Check arguments */
	{
		speed = getBaudrate(baudrate);
		if (speed == -1) { //无符号数不能和有符号比较
			/* TODO: throw an exception */
			LOGE("Invalid baudrate");
			return NULL;
		}
	}

	/* Opening device */
	{
		jboolean iscopy;
		const char *path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);
		LOGD("Opening serial port %s with flags 0x%x", path_utf, O_RDWR | flags);
		fd = open(path_utf, O_RDWR | flags);
		LOGD("open() fd = %d", fd);
		(*env)->ReleaseStringUTFChars(env, path, path_utf);
		if (fd == -1)
		{
			/* Throw an exception */
			LOGE("Cannot open port");
			/* TODO: throw an exception */
			return NULL;
		}
	}

	/* Configure device */
	{
		struct termios cfg;
		LOGD("Configuring serial port");
		if (tcgetattr(fd, &cfg))
		{
			LOGE("tcgetattr() failed");
			close(fd);
			/* TODO: throw an exception */
			return NULL;
		}

		cfmakeraw(&cfg);
		LOGE("cfsetispeed() cfsetospeed() speed:%u(%x).",speed,speed);
		cfsetispeed(&cfg, speed);
		cfsetospeed(&cfg, speed);

		if (tcsetattr(fd, TCSANOW, &cfg))
		{
			LOGE("tcsetattr() failed");
			close(fd);
			/* TODO: throw an exception */
			return NULL;
		}
	}

	/* Create a corresponding file descriptor */
	{
		jclass cFileDescriptor = (*env)->FindClass(env, "java/io/FileDescriptor");
		jmethodID iFileDescriptor = (*env)->GetMethodID(env, cFileDescriptor, "<init>", "()V");
		jfieldID descriptorID = (*env)->GetFieldID(env, cFileDescriptor, "descriptor", "I");
		mFileDescriptor = (*env)->NewObject(env, cFileDescriptor, iFileDescriptor);
		(*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint)fd);
	}

	return mFileDescriptor;
}

/*
 * Class:     cn_spt_serialport_SerialPort
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_win_wilber_commassistant_serialport_SerialPort_close
  (JNIEnv *env, jobject thiz)
{
	LOGD("Java_win_wilber_commassistant_serialport_SerialPort_close");
	jclass SerialPortClass = (*env)->GetObjectClass(env, thiz);
	jclass FileDescriptorClass = (*env)->FindClass(env, "java/io/FileDescriptor");

	jfieldID mFdID = (*env)->GetFieldID(env, SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
	jfieldID descriptorID = (*env)->GetFieldID(env, FileDescriptorClass, "descriptor", "I");

	jobject mFd = (*env)->GetObjectField(env, thiz, mFdID);
	jint descriptor = (*env)->GetIntField(env, mFd, descriptorID);

	LOGD("close(fd = %d)", descriptor);
	close(descriptor);
}

/* get #of elements in a static array */
#ifndef NELEM
# define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
#endif

static const JNINativeMethod methods[] = {
    { "open"  ,  "(Ljava/lang/String;II)Ljava/io/FileDescriptor", (jobject*)Java_win_wilber_commassistant_serialport_SerialPort_open },
	{ "close" ,  "()V", (void*)Java_win_wilber_commassistant_serialport_SerialPort_close }
};

static const char* const kClzName = "win/wilber/commassistant/serialport/SerialPort";

static int register_x(JNIEnv* env) {
	LOGD("-RegisterNatives-\n"  );
	jclass clazz = (*env)->FindClass(env, kClzName );
	LOGD("-RegisterNatives- %p \n" , clazz );
    return //android::AndroidRuntime::registerNativeMethods (env,
    	(*env)->RegisterNatives(env,
    	clazz ,
        methods, NELEM(methods));
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)   {
	LOGD(" -JNI_OnLoad- \n");
    JNIEnv* env = NULL;
    if ( (*vm)->GetEnv( vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK ) {
		LOGE(" JNI_OnLoad fail ");
        return JNI_ERR;
    }
    //register_x( env );
	return JNI_VERSION_1_4; //must
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved){
	LOGD(" -JNI_OnUnload- \n");
	JNIEnv* env = NULL;
    if ( (*vm)->GetEnv( vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK ) {
		LOGE(" JNI_OnUnload fail ");
        return;
    }
	//
}
