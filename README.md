fork VirtualApp

自己尝试撸一遍源码，增加注释

20190711

目录结构
VirtualApp
    ├─app
    │  ├─src
    │      └─main
    │          ├─assets
    │          ├─java
    │          │  └─io
    │          │      └─virtualapp
    │          │          ├─abs
    │          │          │  ├─nestedadapter
    │          │          │  ├─percent
    │          │          │  ├─reflect
    │          │          │  └─ui
    │          │          ├─delegate
    │          │          ├─effects
    │          │          ├─home
    │          │          │  ├─adapters
    │          │          │  │  └─decorations
    │          │          │  ├─location
    │          │          │  ├─models
    │          │          │  ├─platform
    │          │          │  └─repo
    │          │          ├─splash
    │          │          ├─vs
    │          │          └─widgets
    │          │              └─fittext
    │          └─res
    │              ├─drawable
    │              ├─drawable-hdpi
    │              ├─drawable-xxhdpi
    │              ├─layout
    │              ├─menu
    │              ├─mipmap-hdpi
    │              ├─mipmap-mdpi
    │              ├─mipmap-xhdpi
    │              ├─mipmap-xxhdpi
    │              ├─mipmap-xxxhdpi
    │              ├─values
    │              ├─values-zh-rCN
    │              └─values-zh-rTW
    └─lib
        └─src
            └─main
                ├─aidl
                │  ├─android
                │  │  ├─accounts
                │  │  ├─app
                │  │  │  ├─IActivityManager
                │  │  │  └─job
                │  │  ├─content
                │  │  │  └─pm
                │  │  ├─location
                │  │  └─net
                │  │      └─wifi
                │  └─com
                │      └─lody
                │          └─virtual
                │              ├─client
                │              ├─os
                │              ├─remote
                │              │  └─vloc
                │              └─server
                │                  ├─interfaces
                │                  └─pm
                │                      └─installer
                ├─java
                │  ├─android
                │  │  ├─content          //PackageParser，覆盖了系统的隐藏类 android.content.pm.PackageParser
                │  │  │  └─pm
                │  │  └─location
                │  ├─com
                │  │  └─lody
                │  │      └─virtual               //框架主代码
                │  │          ├─client            //client 子进程代码（双开的app进程被VA代理（hook）之后运行的代码）
                │  │          │  ├─badger
                │  │          │  ├─core
                │  │          │  ├─env
                │  │          │  ├─fixer
                │  │          │  ├─hook           // hook java层函数
                │  │          │  │  ├─base
                │  │          │  │  ├─delegate
                │  │          │  │  ├─providers
                │  │          │  │  ├─proxies
                │  │          │  │  │  ├─account
                │  │          │  │  │  ├─alarm
                │  │          │  │  │  ├─am
                │  │          │  │  │  ├─appops
                │  │          │  │  │  ├─appwidget
                │  │          │  │  │  ├─audio
                │  │          │  │  │  ├─backup
                │  │          │  │  │  ├─bluetooth
                │  │          │  │  │  ├─clipboard
                │  │          │  │  │  ├─connectivity
                │  │          │  │  │  ├─content
                │  │          │  │  │  ├─context_hub
                │  │          │  │  │  ├─devicepolicy
                │  │          │  │  │  ├─display
                │  │          │  │  │  ├─dropbox
                │  │          │  │  │  ├─fingerprint
                │  │          │  │  │  ├─graphics
                │  │          │  │  │  ├─imms
                │  │          │  │  │  ├─input
                │  │          │  │  │  ├─isms
                │  │          │  │  │  ├─isub
                │  │          │  │  │  ├─job
                │  │          │  │  │  ├─libcore
                │  │          │  │  │  ├─location
                │  │          │  │  │  ├─media
                │  │          │  │  │  │  ├─router
                │  │          │  │  │  │  └─session
                │  │          │  │  │  ├─mount
                │  │          │  │  │  ├─network
                │  │          │  │  │  ├─notification
                │  │          │  │  │  ├─persistent_data_block
                │  │          │  │  │  ├─phonesubinfo
                │  │          │  │  │  ├─pm
                │  │          │  │  │  ├─power
                │  │          │  │  │  ├─restriction
                │  │          │  │  │  ├─search
                │  │          │  │  │  ├─shortcut
                │  │          │  │  │  ├─telephony
                │  │          │  │  │  ├─usage
                │  │          │  │  │  ├─user
                │  │          │  │  │  ├─vibrator
                │  │          │  │  │  ├─view
                │  │          │  │  │  ├─wifi
                │  │          │  │  │  ├─wifi_scanner
                │  │          │  │  │  └─window
                │  │          │  │  │      └─session
                │  │          │  │  ├─secondary
                │  │          │  │  └─utils
                │  │          │  ├─interfaces
                │  │          │  ├─ipc              //伪造系统framework层的IPC服务类，命名方式：V+原名称。双开进程调用系统framework层代码其实是走的这些伪造类            
                │  │          │  ├─natives
                │  │          │  └─stub             //系统四大组件的插桩代码，
                │  │          ├─helper
                │  │          │  ├─collection
                │  │          │  ├─compat
                │  │          │  ├─ipcbus
                │  │          │  └─utils
                │  │          │      └─marks
                │  │          ├─os
                │  │          ├─remote              //继承Parcelable的一系列实体类   
                │  │          │  └─vloc
                │  │          └─server              //和client对应，这是server端，伪造了系统framework层中系统service的代码，通过binder通讯，与client端的IPC下面的V+原名称（各种manager）通讯，例如VAMS
                │  │              ├─accounts
                │  │              ├─am
                │  │              ├─device
                │  │              ├─interfaces
                │  │              ├─job
                │  │              ├─location
                │  │              ├─memory
                │  │              ├─notification
                │  │              ├─pm
                │  │              │  ├─installer
                │  │              │  └─parser
                │  │              ├─secondary
                │  │              └─vs
                │  └─mirror                        //系统framework层的源码，结构和系统的一样，封装了反射获取系统隐藏method和filed的方法，实现访问修改                        
                │      ├─android
                │      │  ├─accounts
                │      │  ├─app
                │      │  │  ├─admin
                │      │  │  ├─backup
                │      │  │  └─job
                │      │  ├─bluetooth
                │      │  ├─content
                │      │  │  ├─pm
                │      │  │  └─res
                │      │  ├─ddm
                │      │  ├─graphics
                │      │  │  └─drawable
                │      │  ├─hardware
                │      │  │  ├─display
                │      │  │  ├─fingerprint
                │      │  │  └─location
                │      │  ├─location
                │      │  ├─media
                │      │  │  └─session
                │      │  ├─net
                │      │  │  └─wifi
                │      │  ├─os
                │      │  │  ├─mount
                │      │  │  └─storage
                │      │  ├─providers
                │      │  ├─renderscript
                │      │  ├─rms
                │      │  │  └─resource
                │      │  ├─service
                │      │  │  └─persistentdata
                │      │  ├─telephony
                │      │  ├─util
                │      │  ├─view
                │      │  ├─webkit
                │      │  └─widget
                │      ├─com
                │      │  └─android
                │      │      └─internal
                │      │          ├─app
                │      │          ├─appwidget
                │      │          ├─content
                │      │          ├─os
                │      │          ├─policy
                │      │          ├─telephony
                │      │          └─view
                │      │              └─inputmethod
                │      ├─dalvik
                │      │  └─system
                │      ├─java
                │      │  └─lang
                │      └─libcore
                │          └─io
                ├─jni
                │  ├─fb
                │  │  ├─include
                │  │  │  ├─fb
                │  │  │  │  └─fbjni
                │  │  │  └─jni
                │  │  ├─jni
                │  │  │  ├─android
                │  │  │  └─java
                │  │  └─lyra
                │  ├─Foundation
                │  ├─HookZz
                │  │  ├─docs
                │  │  ├─include
                │  │  ├─src
                │  │  │  ├─platforms
                │  │  │  │  ├─arch-arm
                │  │  │  │  ├─arch-arm64
                │  │  │  │  ├─arch-x86
                │  │  │  │  ├─backend-arm
                │  │  │  │  ├─backend-arm64
                │  │  │  │  ├─backend-darwin
                │  │  │  │  ├─backend-linux
                │  │  │  │  ├─backend-posix
                │  │  │  │  ├─backend-x86
                │  │  │  │  └─x86
                │  │  │  └─zzdeps
                │  │  │      ├─common
                │  │  │      ├─darwin
                │  │  │      ├─linux
                │  │  │      └─posix
                │  │  ├─tests
                │  │  │  ├─arm-android
                │  │  │  ├─arm-insn-fix
                │  │  │  ├─arm-ios
                │  │  │  ├─arm64-insn-fix
                │  │  │  └─arm64-ios
                │  │  └─tools
                │  │      └─ZzSolidifyHook
                │  ├─Jni
                │  └─Substrate
                └─res
                    ├─layout
                    └─values
