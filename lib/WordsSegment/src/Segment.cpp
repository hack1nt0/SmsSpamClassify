

#include "com_xiaomi_smsspam_Utils_Tokenizer.h"
#include "jieba/MixSegment.hpp"
#include <iostream>
#include <vector>
#include <string>
using namespace std;

JNIEXPORT jlong JNICALL Java_com_xiaomi_smsspam_Utils_Tokenizer_nativeInitObject
  (JNIEnv *, jclass){
    cout<< "native init" << endl;
    jlong result = 0;
    result = (jlong)(new CppJieba::MixSegment("/home/dy/IdeaProjects/SmsSpamClassify/data/jieba.dict.utf8", "/home/dy/IdeaProjects/SmsSpamClassify/data/hmm_model.utf8"));
    //result = (jlong)(new CppJieba::MixSegment("/home/wangsirui/workspace/jieba.dict.utf8.5000+1468", "/usr/share/CppJieba/dict/hmm_model.utf8"));
    return result;
}

JNIEXPORT void JNICALL Java_com_xiaomi_smsspam_Utils_Tokenizer_nativeDestroyObject
  (JNIEnv *, jclass, jlong thiz){
    CppJieba::MixSegment* p = (CppJieba::MixSegment*)thiz;
    delete p;
    return;
}

JNIEXPORT jobjectArray JNICALL Java_com_xiaomi_smsspam_Utils_Tokenizer_nativeCut
  (JNIEnv *env, jclass, jlong thiz, jstring str){
    CppJieba::MixSegment* p = (CppJieba::MixSegment*)thiz;
    jobjectArray args = 0;

    vector<string> words;
    jboolean isCopy = JNI_FALSE;
    p->cut(env->GetStringUTFChars(str, &isCopy), words);
    int len = words.size();

    jclass objClass = (env)->FindClass("java/lang/Object");

    args = (env)->NewObjectArray(len, objClass, 0);


    for(int  i=0; i < len; i++ )
    {
        jstring jstr = env->NewStringUTF(words.at(i).c_str());

        (env)->SetObjectArrayElement(args, i, jstr);
    }
    return args;
}

JNIEXPORT jboolean JNICALL Java_com_xiaomi_smsspam_Utils_Tokenizer_nativeInDict
  (JNIEnv *env, jclass, jlong thiz, jstring str){
    CppJieba::MixSegment* p = (CppJieba::MixSegment*)thiz;
    jboolean isCopy = JNI_FALSE;
    return p->inDict(env->GetStringUTFChars(str, &isCopy));
}
