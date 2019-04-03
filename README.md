# Compass

	WARNING:
	这是一个练手性质的、功能不完善的、设计存在缺陷的、API 极其不稳定的、随时可能提交破坏性更新的……页面路由框架。
	通过注解将 Activity 注册到统一的路由表中，解耦模块间的页面跳转。

## 开始上手

暂未上传至远程 Maven 仓库。可 clone 项目最新代码后, 依次将 `api`, `compiler`, `compass` 上传至你的私有 maven 仓库中。
还可以设置 `uploadLocal=true` 以提交到本地 maven 仓库)

需要注意的是, 在上传 `compiler` 与 `compass` 时, 需要把对 `api` 的 module dependency 改为 library dependency (在 `build.gradle` 中有注释).

另外，项目代码基于 [AndroidX](https://developer.android.com/jetpack/androidx/)，暂时没有兼容 [Support Library](https://developer.android.com/topic/libraries/support-library/index) 的计划。

```groovy
dependencies {
  implementation "com.arch.jonnyhsia:compass:$pomVersion"
  // If you don't use kotlin, replace kapt with annotationProcessor
  kapt "com.arch.jonnyhsia:compass-compiler:$pomVersion"
}
```

## 基本用法

### 1. 为目标页面声明注解

```kotlin
@Route(name = "Detail")
class DetailActivity : Activity()
```

### 2. Rebuild 项目，并导入路由表

当没有 `Route` 注解时不会生成路由表，因此需要先声明注解后编译 (后续改进)。
最好是能自动加载路由表，暂时没想好，后续会尽可能实现。

```kotlin
// 在 Application 中初始化，手动添加路由表
Compass.initialize(CompassTable.getPages())
```

### 3. 跳转页面

通过一个 Activity 或 Fragment 以及页面的路径即可进行简单的跳转：
```kotlin
Compass.navigate("scheme://name").go(activity)
Compass.navigate("scheme://name").go(fragment)
```
此外提供了对应的拓展方法，以更简洁地调用
```kotlin
activityOrFragment.navigate("scheme://name")
```
可以为跳转添加参数
```kotlin
// XXXActivity.kt
Compass.navigate("sample://ArticleDetail")
    .addParameter("id", articleId)
    .go(this)
    
// 或者使用拓展方法
navigate("sample://ArticleDetail") {
    addParameter("id", articleId)
}
    
// ArticleDetailActivity.kt
val articleId = intent.getIntExtra("id")
```

### 4. startActivityForResult
默认配置的跳转走的是 startActivity 方法，如果页面需要返回数据，则在 `Route` 注解中配置 `requestCode` 参数 (默认 -1 即不需要返回数据).
发送数据与接收数据双方实现正常的收发代码即可。

```kotlin
@Route(name = "Login", requestCode = 10)
class LoginActivity : Activity()
```

### 5. SchemeInterceptor

```kotlin
Compass.setSchemeInterceptor(object: SchemeInterceptor {
    override fun intercept(intent: ProcessableIntent) {
        // 判断 intent.uri 是否为 web 链接，实现内嵌页与原生页匹配
        // 若是，则跳转至 Web 页面或其他对应的原生页
        // intent.redirect("*://Web").addParameter("url", intent.uri)
        // 后续会在 Route 注解中添加匹配 url 的参数
    }
})
```

### 6. UnregisterPageHandler
```kotlin
Compass.setUnregisterPageHandler(object: UnregisterPageHandler {
    override fun handleUri(intent: ProcessableIntent) {
        // 跳转的页面并没有注册到路由表中
        // 可用于调试，或处理成前往其他页面
    }
})
```

### 7. RouteInterceptor
TODO: 需要手动添加拦截器到 Compass 中，后续实现自动添加。
```kotlin
// 实现一个 RouteInterceptor
object LoginInterceptor : RouteInterceptor {
    override fun intercept(intent: ProcessableIntent) {
        if (!isLogin) {
            // 若未登录，则
            intent.redirect("*://Login")
                    .removeAllParameters()
                    .addParameter("entry", intent.requester)
        }
    }
}
// 将拦截器添加到 Compass 中
Compass.addRouteInterceptor(LoginInterceptor)

// 对应的页面的 Route 注解中指定拦截器
@Route(name = "Xxx", interceptors = [LoginInterceptor::class])
class XxxActivity: Activity()
```


### +. 自定义路由表包名与默认 Scheme

路由表所在包名默认为 `com.arch.jonnyhsia.compass`, 可以在 `build.gradle` 中为其添加参数。
Scheme 同样通过添加相应参数实现自定义缺省值，默认为 `compass`

```groovy
kapt {
    arguments {
        arg("COMPASS_TABLE_PKG_NAME", "custom.package.name")
        arg("DEFAULT_PAGE_SCHEME", "your_scheme")
    }
}
```

## 最后

是个菜鸡，有什么高见非常欢迎提 issue！
