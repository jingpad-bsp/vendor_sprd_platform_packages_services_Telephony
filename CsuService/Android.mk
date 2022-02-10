LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional

LOCAL_PRIVILEGED_MODULE := true

LOCAL_SRC_FILES := $(call all-java-files-under,src)

LOCAL_SRC_FILES += \ $(call all-Iaidl-files-under, src)

LOCAL_JAVA_LIBRARIES := \
        radio_interactor_common \
        ims-common \

LOCAL_STATIC_JAVA_LIBRARIES := \
        fastjson-1.1.72.android \
        dom4j-2.0.3 \

LOCAL_PACKAGE_NAME := CsuService

LOCAL_PRIVATE_PLATFORM_APIS := true

LOCAL_PROGUARD_ENABLED := disabled

LOCAL_CERTIFICATE := platform

#LOCAL_DEX_PREOPT := false

include $(BUILD_PACKAGE)
include $(call all-makefiles-under,$(LOCAL_PATH))

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := fastjson-1.1.72.android:lib/fastjson-1.1.72.android.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += dom4j-2.0.3:lib/dom4j-2.0.3.jar
include $(BUILD_MULTI_PREBUILT)
