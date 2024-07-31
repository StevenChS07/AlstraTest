//
// Created by Steven Chaves on 17/7/24.
//
#include <jni.h>
#include <string>
#include <ifaddrs.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <netdb.h>

#ifndef NI_MAXHOST
#define NI_MAXHOST 1025
#endif

#ifndef IN6_IS_ADDR_GLOBAL
#define IN6_IS_ADDR_GLOBAL(a) \
    (((a)->s6_addr[0] & 0x0f) == 0x0e)
#endif

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_alstratest_MainActivity_getDeviceIpAddress(JNIEnv* env, jobject) {
    struct ifaddrs *ifaddr, *ifa;
    char ip[NI_MAXHOST];
    bool found = false;

    if (getifaddrs(&ifaddr) == -1) {
        return env->NewStringUTF("Error retrieving IP addresses");
    }

    for (ifa = ifaddr; ifa != nullptr; ifa = ifa->ifa_next) {
        if (ifa->ifa_addr == nullptr) continue;
        int family = ifa->ifa_addr->sa_family;

        if (family == AF_INET || family == AF_INET6) {
            int s = getnameinfo(ifa->ifa_addr,
                                (family == AF_INET) ? sizeof(struct sockaddr_in) :
                                sizeof(struct sockaddr_in6),
                                ip, NI_MAXHOST,
                                nullptr, 0, NI_NUMERICHOST);

            if (s != 0) continue;

            if (family == AF_INET6) {
                struct in6_addr addr = ((struct sockaddr_in6*)ifa->ifa_addr)->sin6_addr;
                if (IN6_IS_ADDR_GLOBAL(&addr)) {
                    found = true;
                    break;
                }
            } else {
                struct in_addr addr = ((struct sockaddr_in*)ifa->ifa_addr)->sin_addr;
                if (!(addr.s_addr & htonl(0xff000000)) && !(addr.s_addr & htonl(0x00ffffff))) {
                    found = true;
                    break;
                }
            }
        }
    }

    freeifaddrs(ifaddr);
    if (found) {
        return env->NewStringUTF(ip);
    } else {
        return env->NewStringUTF("No suitable IP address found");
    }
}
