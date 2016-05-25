
##资源包
* SDK的UI库提供了两套界面资源文件，任选一个放入项目的res文件夹内即可。
* 支持自定义修改资源文件中颜色、demen参数，以及替换图片来达到您所需求的界面效果，但切勿更改任何属性名称和文件名。

##jar包
* 1.使用API 23(Android 6.0)及以上Android SDK进行编译的需要导入org.apache.http-simple4.4.2.jar。
* 2.根据是否需要发送位置功能，V5KF_1.x.x_r16xxxx.jar和V5KF_1.x.x_withMap_r16xxxx.jar进行二选一即可。
* 3.需要发送位置功能的才需要导入文件夹内腾讯地图相关的sdk包，并使用V5KF_1.x.x_withMap_r16xxxx.jar。
* 4.此外，eclipse目录的Demo中使用了第三方推送(信鸽推送)，以在用户离线之后接收客服消息，目前后台支持百度云推送和信鸽推送两个平台，接口使用详见SDK接口文档。
