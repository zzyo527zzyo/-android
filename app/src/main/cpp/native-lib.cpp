#include <jni.h>
#include <string>
#include <linux/ptrace.h>
#include <sys/ptrace.h>
#include <android/log.h>
#include <unistd.h>
#include <fcntl.h>
#include <dirent.h>
// ✅ 添加日志宏定义（关键！）
#define LOG_TAG "SecupayNative"  // 自定义标签，用于 logcat 过滤
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)


extern "C" JNIEXPORT jstring

JNICALL
Java_com_example_secupay_1native_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" {
JNIEXPORT jint JNICALL
Java_com_example_secupay_1jni_security_SecurityChecks_DetectJDWP(
        JNIEnv *env,
        jclass thiz) {

    char path[256], name[32];
    DIR *dir = opendir("/proc/self/task");
    if (!dir) return 0;

    struct dirent *entry;
    while ((entry = readdir(dir))) {
        // 只处理线程目录（纯数字）
        if (entry->d_type == DT_DIR && entry->d_name[0] >= '0' && entry->d_name[0] <= '9') {
            snprintf(path, sizeof(path), "/proc/self/task/%s/comm", entry->d_name);
            int fd = open(path, O_RDONLY);
            if (fd >= 0) {
                ssize_t len = read(fd, name, sizeof(name) - 1);
                close(fd);
                if (len > 0) {
                    name[len] = '\0';
                    // 移除换行符
                    if (name[len - 1] == '\n') name[len - 1] = '\0';

                    // 🔥 精准匹配：只认特定调试线程名（全名或前缀）
                    if (strcmp(name, "jdwp") == 0 ||
                        strcmp(name, "JDWP") == 0 ||
                        strcmp(name, "jdb") == 0 ||
                        strncmp(name, "JDWP", 4) == 0 ||
                        strncmp(name, "jdwp", 4) == 0) {

                        LOGD("检测到调试线程: %s (TID: %s)", name, entry->d_name);
                        closedir(dir);
                        return -1;  // 真正的调试线程
                    }
                }
            }
        }
    }
    closedir(dir);
    return 0;  // 未检测到
}
}
