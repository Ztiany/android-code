/**
 * @author Lance
 * @date 2019-06-10
 */

#ifndef MMKV_MMKVLOG_H
#define MMKV_MMKVLOG_H

#include <android/log.h>

#define MMKV_LOG_TAG "MMKV"

#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, MMKV_LOG_TAG, __VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, MMKV_LOG_TAG, __VA_ARGS__)

#endif //MMKV_MMKVLOG_H
