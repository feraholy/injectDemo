#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>
#include <sys/mman.h>
#include <elf.h>
#include <fcntl.h>
#include <dlfcn.h>
#include <jni.h>

#define LOG_TAG "HOOK"
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)

const char *target_path = "/data/data/com.ry.target/lib/libtarget.so";

void (*_set_step)(int);
static JNIEnv* (*getJNIEnv)();

static JavaVM *mVM = NULL;
#include "classes.h"

#define		FILE_NAME		"/sdcard/hooker.dex"

static int saveDex(JNIEnv *env){
	FILE *f = fopen(FILE_NAME, "wb");
	LOGD("saveDex!");
	if(f == NULL){
		LOGD("DEX Open Error!");
		return (-1);
	}

	fwrite(DATA, sizeof(DATA), 1, f);
	LOGD("saveDex OK!");
	fclose(f);

	return 0;
}

static void loadJar(JNIEnv* env){
	char mPath[256] = {0};
	LOGD("loadJar!");
	jclass ActivityThread = (*env)->FindClass(env, "android/app/ActivityThread");
	LOGD("ActivityThread>>: %p", ActivityThread);
	jclass Context = (*env)->FindClass(env, "android/content/Context");
	LOGD("Context>>: %p", Context);

	jmethodID getPackageName = (*env)->GetMethodID(env, Context, "getPackageName", "()Ljava/lang/String;");
	jmethodID getClassLoader = (*env)->GetMethodID(env, Context, "getClassLoader", "()Ljava/lang/ClassLoader;");

	jmethodID currentApplication = (*env)->GetStaticMethodID(env, ActivityThread, "currentApplication", "()Landroid/app/Application;");
	LOGD("currentApplication>>: %p", currentApplication);
	jobject app = (*env)->CallStaticObjectMethod(env, ActivityThread, currentApplication);


	jobject pack = (*env)->CallObjectMethod(env, app, getPackageName);
	jobject clsP = (*env)->CallObjectMethod(env, app, getClassLoader);
	LOGD("Parent ClassLoader:%p", clsP);

	const char *szPack = (*env)->GetStringUTFChars(env, pack, NULL);
	sprintf(mPath, "/data/data/%s/", szPack);
	LOGD("path:%s", mPath);
	(*env)->ReleaseStringUTFChars(env, pack, szPack);

	jstring path = (*env)->NewStringUTF(env, mPath);

	jclass DexClassLoader = (*env)->FindClass(env, "dalvik/system/DexClassLoader");
	jclass gDexClassLoader = (*env)->NewGlobalRef(env, DexClassLoader);
	LOGD("DexClassLoader>>: %p", DexClassLoader);
	jclass String = (*env)->FindClass(env, "java/lang/String");
	LOGD("String>>: %p", String);
	jclass ClassLoader = (*env)->FindClass(env, "java/lang/ClassLoader");

	LOGD("ClassLoader>> %p", ClassLoader);
	jmethodID loadClass = (*env)->GetMethodID(env, ClassLoader, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
	LOGD("loadClass>>: %p", loadClass);
	jmethodID init = (*env)->GetMethodID(env, gDexClassLoader, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V");
	LOGD("DexClassLoader>>Init: %p", init);

	jstring name = (*env)->NewStringUTF(env, FILE_NAME);
	jstring gname = (*env)->NewGlobalRef(env, name);
	jstring gpath = path;//(*env)->NewGlobalRef(env, path);

//	{
//		jclass ABCD = (*env)->FindClass(env, "com/ry/inject/ABCD");
//		jmethodID init2 = (*env)->GetMethodID(env, gDexClassLoader, "<init>", "()V");
//		jobject mABCD = (*env)->NewObject(ABCD, init2);//, gname, gpath, NULL, clsP);
//	}



	LOGD("DexFileName:%p", gname);
	LOGD("DexFilePath:%p", gpath);
	LOGD("DexClassLoader:%p", gDexClassLoader);
	LOGD("clsP:%p", clsP);
	jobject dexCl = (*env)->NewObject(env, gDexClassLoader, init, gname, gpath, NULL, clsP);
	LOGD("dexCl:%p", dexCl);

//	(*env)->ReleaseStringUTFChars(env, name, FILE_NAME);
//	(*env)->ReleaseStringUTFChars(env, path, mPath);

#define CLS_NAME 		"com.wzh.gpclick.GPAPK"
	jstring clsName = (*env)->NewStringUTF(env, CLS_NAME);
	jstring gclsName = (*env)->NewGlobalRef(env, clsName);
	jclass GPAPK = (*env)->CallObjectMethod(env, dexCl, loadClass, gclsName);
	LOGD("GPAPK:%p", GPAPK);
//	(*env)->ReleaseStringUTFChars(env, clsName, CLS_NAME);

	jmethodID Main = (*env)->GetStaticMethodID(env, GPAPK, "Main", "([Ljava/lang/String;)V");
	LOGD("Main:%p", Main);

	(*env)->CallStaticVoidMethod(env, GPAPK, Main, NULL);
	LOGD("Done");
}

int hook_entry(char * a) {
	LOGD("Hook success, pid = %d\n", getpid());
	LOGD("Hello %s\n", a);

    void* handle = dlopen("libandroid_runtime.so", RTLD_NOW);
    LOGD("libandroid_runtime >> %p", handle);
	if (handle == NULL) {
		LOGD("open target so error!\n");
		return -1;
	}

    getJNIEnv = dlsym(handle, "_ZN7android14AndroidRuntime9getJNIEnvEv");
    LOGD("getJNIEnv >> %p", getJNIEnv);
	if (getJNIEnv == NULL) {
		LOGD("get set_step error!\n");
		return -1;
	}
    JNIEnv* env = getJNIEnv();
    (*env)->GetJavaVM(env, &mVM);

    LOGD("mVM=%p", mVM);
    saveDex(env);
    loadJar(env);


	return 0;
}

//JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
//	jint result = JNI_VERSION_1_4;
//
//	LOGD("wzh JNI_OnLoad  V2.0 vm=0X%p", vm);
//	JNIEnv *env = NULL;
//	(*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_4);
//
////    saveDex(env);
//    loadJar(env);
//
//	return result;
//}
