# AI视频-客户端-混淆版
[toc]
## 1	环境变量

### 开发环境
| 参数名 | 字段值 |
| ------ | ------ |
|baseUrl|http://8.148.151.104:7068|


## 2	接口接入说明

#### 特别注意：第一版开发时，需要按非混淆版本接入接口，即按文档中的真实请求地址和字段名接入，全部接口接入并且达到提测的标准后，再和后台接口端联系添加接口混淆配置，接口端添加混淆配置后，再根据“混淆接口对接文档”的腾讯文档让AI帮改成混淆版本，接入完混淆版本后方可提测。
1.使用当前小幺鸡接口文档，需安装“**docway**”扩展程序，安装后刷新文档页面，在接口详情页面中点击“**测试接口**”的蓝色按钮，在弹框中填写对应参数后，选择“**插件运行模式**”再点击“**运行**”的按钮即可请求测试环境的接口。
2.需要实现同一套接口功能开发多个包名，所以需要根据包名给接口请求地址、请求传参字段名、请求返回数据格式和返回字段名做混淆，写在接口文档中的为真实的请求地址和字段名，客户端使用的是混淆的请求地址和混淆的字段名。
3.获取请求头的混淆字段名，手动请求下方接口**获取包名请求头混淆字段；**
4.获取接口的混淆数据，手动请求下方接口**获取接口混淆数据-单个接口**或者**获取接口混淆数据-全部接口。**
5.加密说明：（1）加密方式为AES，解密的CIPHER = AES/ECB/PKCS5Padding，**非混淆版本的解密key**：aikogpap1s2e2288；混淆版本的解密key配置混淆后再私发；（2）**添加设备信息（**/user/addDevice**）、恢复购买校验（**/googlePlay/restoreVerify**）、切换账号（**/user/change**）**接口返回的用户uid已进行加密，传回请求头的uid不用解密，客户端需外展的地方需要解密后再外展，（3）**获取用户信息（**/user/getInfo**）**接口返回的密码password已进行加密，客户端外展时需要解密后再外展；



## 3	获取包名请求头混淆字段

> GET  /confuse/getHeaderField
### 接口说明
> 该接口不能接入到客户端，只用于接入接口时手动请求来获取请求头混淆参数
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| platformType|number||true|平台，1=安卓，2=ios|
| packageName|string||true|包名|
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| headerParam|object||true|请求头混淆参数，与文档中请求参数对应，key为请求参数字段名，val为该参数的类型标识，标识包含：NUMBER=数值类型；STRING=字符串类型|
| headerParamRealField|object||true|请求头参数的真实字段与混淆字段的对应关系，key为真实字段名，val为混淆字段名|


## 4	获取接口混淆数据-单个接口

> GET  /confuse/getInterfaceConfig
### 接口说明
> 该接口不能接入到客户端，只用于接入接口时手动请求来获取接口对应的混淆格式
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| platformType|number||true|平台，1=安卓，2=ios|
| packageName|string||true|包名|
| path|string||true|文档中的真实请求地址（不包含域名和端口）|
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| realPath|string||true|真实请求地址|
| aliasPath|string||true|混淆请求地址（客户端使用该地址请求接口数据）|
| reqMethod|string||true|请求方式：GET、POST (与接口文档中的请求方式一致)|
| reqParamType|string||true|传参格式：FORM、FORM-DATA（上传图片文件时）、JSON（body传参）|
| reqParam|object||true|请求参数，与文档中请求参数对应，key为请求参数字段名，val为该参数的类型标识，标识包含：NUMBER=数值类型；STRING=字符串类型；BOOLEAN=布尔类型；ARRAY=数组类型；FILE=文件类型|
| reqParamRealField|object||true|请求参数的真实字段与混淆字段的对应关系，key为真实字段名，val为混淆字段名|
| reqPuzzleParam|object||true|请求干扰传参，key为干扰传参字段名，val为数值，数值的含义为该字段的传参规则，规则为：1=从0到999中获取一个值硬编码固定传值；2=随机定义一个字符串，长度20字符内，硬编码传值；3=当前时间戳；4=当前时间或日期字符串，格式yyyyMMddHHmmss或yyyyMMdd；5=uuid随机字符串|
| respField|object||true|返回数据混淆格式，key为返回的混淆字段名，val为该字段的类型标识，标识包含：，val的值包含：NUMBER=数值类型；STRING=字符串类型；BOOLEAN=布尔类型；ARRAY=数组类型。 在接口文档中外层data里的数据所在的层级与dataRoute对应|
| dataRoute|string||true|返回数据混淆中，data字段里的数据对应的层级|
| respRealField|object||true|返回数据的真实字段与混淆字段的对应关系，key为真实字段名，val为混淆字段名|
| respPuzzleField|object||true|返回数据干扰字段，客户端不用处理|
|⇥ louver|number||true||


## 5	获取接口混淆数据-全部接口

> GET  /confuse/getAllInterfaceConfig
### 接口说明
> <p style="">该接口不能接入到客户端，只用于接入接口时手动请求来获取接口的混淆格式，返回的是后台全部接口的混淆数据，如果是客户端未使用到的接口（安卓或IOS端特有的接口）混淆数据可忽略</p>
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| platformType|number||true|平台，1=安卓，2=ios|
| packageName|string||true|包名|
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| realPath|string||true|真实请求地址|
| aliasPath|string||true|混淆请求地址（客户端使用该地址请求接口数据）|
| reqMethod|string||true|请求方式：GET、POST (与接口文档中的请求方式一致)|
| reqParamType|string||true|传参格式：FORM、FORM-DATA（上传图片文件时）、JSON（body传参）|
| reqParam|object||true|请求参数，与文档中请求参数对应，key为请求参数字段名，val为该参数的类型标识，标识包含：NUMBER=数值类型；STRING=字符串类型；BOOLEAN=布尔类型；ARRAY=数组类型；FILE=文件类型|
| reqParamRealField|object||true|请求参数的真实字段与混淆字段的对应关系，key为真实字段名，val为混淆字段名|
| reqPuzzleParam|object||true|请求干扰传参，key为干扰传参字段名，val为数值，数值的含义为该字段的传参规则，规则为：1=从0到999中获取一个值硬编码固定传值；2=随机定义一个字符串，长度20字符内，硬编码传值；3=当前时间戳；4=当前时间或日期字符串，格式yyyyMMddHHmmss或yyyyMMdd；5=uuid随机字符串|
| respField|object||true|返回数据混淆格式，key为返回的混淆字段名，val为该字段的类型标识，标识包含：，val的值包含：NUMBER=数值类型；STRING=字符串类型；BOOLEAN=布尔类型；ARRAY=数组类型。 在接口文档中外层data里的数据所在的层级与dataRoute对应|
| dataRoute|string||true|返回数据混淆中，data字段里的数据对应的层级|
| respRealField|object||true|返回数据的真实字段与混淆字段的对应关系，key为真实字段名，val为混淆字段名|
| respPuzzleField|object||true|返回数据干扰字段，客户端不用处理|
|⇥ louver|number||true||


## 6	公共模块

## 6.1	全局错误码处理说明

#### 
全部接口处理错误码（code字段值）：410=弹窗提示账号已注销，弹窗的文案在msg返回，弹窗的标题和按钮文案在“获取APP语言文案配置（/sys/getAppLanguageConfig）”接口返回，对应字段为：userCancelledPopupTitle、roger

## 6.2	全局系统参数

> GET  /sys/getGlobal
### 接口说明
> 进入首页请求
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|string||true||
| msg|string||true||
| data|object||true||
|⇥ integralEntranceJumpState|number||true|点击积分入口是否需要判断会员拦截，0=否，1=是|
|⇥ signInShow|number||true|签到领积分功能是否可用，0=否，1=全部用户可用，2=仅会员用户可用|
|⇥ templateAbduceIntegralShow|number||true|0=都不展示，1=仅主页展示，2=仅主题列表页和模板选择页展示，3=全部展示，4=仅探索页和使用模板页展示|
|⇥ integralAndVipEntranceShow|number||true|积分入口和会员入口是否展示，0=否，1=是|
|⇥ reportEntranceShow|number||true|举报入口，0=不展示，1=展示|
|⇥ userChangeShow|number||true|设置页面账号、密码和切换账号是否展示，0=否，1=是|
|⇥ textToVideoIntegralNum|number||true|文生视频所需积分数额，0=免费；未选择使用模板时，使用该积分，选择使用模板时，使用模板下的积分|
|⇥ textToImgIntegralNum|number||true|文生图片所需积分数额，0=免费；未选择使用模板时，使用该积分，选择使用模板时，使用模板下的积分|
|⇥ fbAppId|string||true|facebook app id（已进行AES128加密，需解密后再使用）|
|⇥ fbClientToken|string||true|facebook client token（已进行AES128加密，需解密后再使用）|


## 6.3	获取APP语言文案配置

> GET  /sys/getAppLanguageConfig
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true|200|
| data|object||true||
|⇥ useStorageTitle|string||true|存储权限使用说明|
|⇥ welcomeUse|string||true|欢迎使用Ai Art！|
|⇥ noMoreData|string||true|没有更多数据|
|⇥ webUrlEmpty|string||true|链接不能为空|
|⇥ languageSet|string||true|多语言|
|⇥ uploadImgNumMsg|string||true|请先选择至少一张图片|
|⇥ parseError|string||true|数据解析错误|
|⇥ systemError|string||true|系统出错，请点击屏幕重新加载|
|⇥ tipMsg|string||true|提示|
|⇥ useCameraConfirmDesc|string||true|应用请求访问您的相机权限，以便您修改头像、上传照片，如果您拒绝了访问，会导致无法使用该功能，请在设置中授权。|
|⇥ submitMsg|string||true|提交|
|⇥ logout|string||true|退出登录|
|⇥ generateMsg|string||true|立即生成|
|⇥ delPhotoConfirm|string||true|确定删除该照片吗？|
|⇥ adviceBack|string||true|意见反馈|
|⇥ generateHandle|string||true|AI正在分析图像...|
|⇥ settingsMsg|string||true|设置|
|⇥ aiVideoPro|string||true|AI Video PRO|
|⇥ networkError|string||true|网络错误，请检查网络|
|⇥ editInfo|string||true|修改资料|
|⇥ mineAiart|string||true|我的作品|
|⇥ sureMsg|string||true|确定|
|⇥ vipOpenMsg|string||true|立即开通|
|⇥ uploadImgFailed|string||true|上传失败|
|⇥ optional|string||true|（非必填）|
|⇥ loading|string||true|正在加载中...|
|⇥ version|string||true|版本号|
|⇥ agreeAndContinue|string||true|同意并继续|
|⇥ viewAll|string||true|查看全部|
|⇥ disagreeMsg|string||true|不同意|
|⇥ aiVideoMsg|string||true|AI Video|
|⇥ notFoundShareApp|string||true|没有找到可分享的应用|
|⇥ signVerifyException|string||true|存在签名异常，请在官方下载最新的APP|
|⇥ signVerifyFailed|string||true|签名校验失败|
|⇥ termsOfUse|string||true|使用条款|
|⇥ useTemplate|string||true|使用模板|
|⇥ privacyPolicyDesc|string||true|继续使用Ai Art应用程序，即表示您已经阅读并接受|
|⇥ goSettings|string||true|好，去设置|
|⇥ cancelMsg|string||true|取消|
|⇥ netErrorToast|string||true|系统出错, 请检查网络连接|
|⇥ requestFailed|string||true|请求失败，请稍后再试|
|⇥ useAndPrivacyTitle|string||true|使用条款和隐私政策|
|⇥ settingLanguage|string||true|设置语言|
|⇥ connTimeout|string||true|连接超时，请检查网络|
|⇥ finishMsg|string||true|完成|
|⇥ goOpen|string||true|去开启|
|⇥ givePraise|string||true|给个好评|
|⇥ uploadImg|string||true|上传图片|
|⇥ shareFriend|string||true|分享给好友|
|⇥ mine|string||true|我的|
|⇥ viewNow|string||true|立即查看|
|⇥ explore|string||true|探索|
|⇥ privacyPolicy|string||true|隐私政策|
|⇥ andMsg|string||true|和|
|⇥ uploadImgSuccess|string||true|上传成功|
|⇥ home|string||true|首页|
|⇥ useCameraConfirm|string||true|提示：是否允许应用访问您的相机权限？|
|⇥ contactWayTip|string||true|请留下您的联系方式|
|⇥ useCameraDesc|string||true|用于上传头像、上传照片等场景。|
|⇥ useStorageDesc|string||true|用于上传头像、上传照片、保存图片等场景中读取和写入相册和文件内容。|
|⇥ contactWay|string||true|联系方式|
|⇥ useCameraTitle|string||true|相机权限使用说明|
|⇥ noData|string||true|暂无数据|
|⇥ adviceBackTip|string||true|请填写您的意见或建议，有其他问题也可以留言|
|⇥ vipUnlockMsg|string||true|升级会员可解锁更多功能|
|⇥ noneWorkTaskMsg|string||true|请选择模板进行创作|
|⇥ selectIntegralProduct|string||true|选择积分套餐|
|⇥ getIntegral|string||true|获取积分|
|⇥ integralProductBut|string||true|充值|
|⇥ vipProductBut|string||true|开通|
|⇥ vipOpenPageAgreementMsg|string||true|开通会员，即表示您已经阅读并接受|
|⇥ integralBalance|string||true|积分余额|
|⇥ restorePurchase|string||true|恢复购买|
|⇥ deleteMsg|string||true|删除|
|⇥ deleteTaskMsg|string||true|确认删除所选作品？|
|⇥ downloadMsg|string||true|下载|
|⇥ production|string||true|作品|
|⇥ roger|string||true|知道了|
|⇥ nicknameMsg|string||true|昵称|
|⇥ saveMsg|string||true|保存|
|⇥ copyMsg|string||true|复制成功|
|⇥ accountMsg|string||true|账号|
|⇥ inputAccountHint|string||true|请输入账号|
|⇥ changeAccountMsg|string||true|切换账号|
|⇥ passwordEditMsg|string||true|修改密码|
|⇥ passwordEditLimitMsg|string||true|密码最多输入15位|
|⇥ passwordEditSuccMsg|string||true|修改成功|
|⇥ pleaseSelect|string||true|请选择|
|⇥ adviceType|string||true|反馈类型|
|⇥ selectAdviceType|string||true|请选择反馈类型|
|⇥ pleaseInputContactWay|string||true|请填写邮箱|
|⇥ required|string||true|（必填）|
|⇥ reportType|string||true|举报类型|
|⇥ selectReportType|string||true|请选择举报类型|
|⇥ reportBackTip|string||true|请简单描述下您遇到的问题|
|⇥ reportMsg|string||true|举报|
|⇥ reportDescription|string||true|请简单描述|
|⇥ adviceDescription|string||true|反馈内容|
|⇥ passwordMsg|string||true|密码|
|⇥ inputPasswordHint|string||true|请输入密码|
|⇥ systemNotify|string||true|系统通知|
|⇥ replyBut|string||true|回复|
|⇥ awardMsg|string||true|奖励|
|⇥ creationMsg|string||true|创作|
|⇥ creativeStudio|string||true|创意探索|
|⇥ textToVideoTitle|string||true|文生视频|
|⇥ textToVideoDesc|string||true|输入你的提示词，可以是任何你想创作的东西！|
|⇥ textToImgTitle|string||true|图片编辑|
|⇥ textToImgDesc|string||true|输入你的提示词，告诉 AI 你想如何修改这张图片！|
|⇥ useMsg|string||true|使用|
|⇥ accountSettingMsg|string||true|账户|
|⇥ preferencesMsg|string||true|偏好|
|⇥ supportAndAbount|string||true|帮助与关于|
|⇥ userCancelledPopupTitle|string||true|账号已注销通知|
|⇥ pleaseEnterText|string||true|请输入提示词|
|⇥ myCollection|string||true|我的收藏|
|⇥ |string||true||
|⇥ |string||true||


## 6.4	语言列表

> GET  /language/list
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true|200|
| msg|string||true|请求成功|
| data|array[object]||true||
|⇥ id|number||true|语言id，用于接口请求头参数传回|
|⇥ language|string||true|语言|
|⇥ tag|string||true|语言标识|
|⇥ isDefault|number||true|是否默认选择，0=否，1=是（当客户端系统语言在当前列表没有的时候，则使用默认语言，当默认语言也没有的时候，则使用列表第一个即可）|


## 7	V 1.0

## 7.1	获取背景视频

> GET  /sys/getVideo
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| type|string||true|1=登录页，2=VIP开通页，3=同意协议页面|
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true||
| msg|string||true||
| data|object||true||
|⇥ url|string||true|视频地址|


## 7.2	获取网页url

> GET  /sys/getUrl
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| type|number||true|1=隐身政策，2=使用条款|
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true||
| msg|string||true||
| data|object||true||
|⇥ url|string||true||


## 7.3	获取分享信息

> GET  /sys/getShareInfo
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true||
| msg|string||true||
| data|object||true||
|⇥ title|string||true|标题|
|⇥ content|string||true|内容|


## 7.4	banner列表

> GET  /home/listBanner
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true||
| msg|string||true||
| data|array[object]||true||
|⇥ title|string||true|标题|
|⇥ subtitle|string||true|副标题|
|⇥ type|number||true|跳转类型，0=不跳转，1=内部跳转，2=外部跳转，3=跳转模板详情页|
|⇥ url|string||true|跳转的地址，当type=3时，该字段为模板id字符串，需要转成数值类型才能使用|
|⇥ img|string||true|banner图片|
|⇥ aiartImg|string||true|type为3时，返回模板图片地址|
|⇥ aiartUploadNum|number||true|模板需上传图片数量|
|⇥ lockType|number||true|解锁状态，0=免费，2=积分解锁|
|⇥ lockIntegral|number||true|解锁积分数量|
|⇥ aiartName|string||true|模板名称|
|⇥ sampleImgList|array[string]||true|模板示例图数组，示例图数量和当前模板需上传图片的数量保持一致|


## 7.5	系统通知列表

> GET  /sys/listSysNotify
### 接口说明
> 获取列表后，需要刷新获取用户信息（user/getInfo）接口来去掉系统通知的红点
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true||
| msg|string||true||
| data|array[object]||true||
|⇥ content|string||true|通知内容|


## 7.6	版本更新检查接口

> GET  /sys/checkVersion
### 接口说明
> 进入首页时请求
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|string||true||
| msg|string||true||
| data|object||true||
|⇥ upgradeState|number||true|0=正常，1=强制更新弹窗|
|⇥ title|string||true|弹窗标题|
|⇥ content|string||true|弹窗内容|
|⇥ buttonMsg|string||true|弹窗按钮文案|
|⇥ isSignInPopup|boolean||true|是否弹窗签到领金币，false=否，true=是|
|⇥ package|string||true|更新跳转的包名，已加密，需解密后使用|


## 7.7	获取意见反馈或举报的类型

> GET  /sys/listSysType
### 接口说明
> 跳转进入提交页面时请求
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| type|number||true|1=意见反馈，2=举报|
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true||
| msg|string||true||
| data|array[object]||true||
|⇥ id|number||true|类型id|
|⇥ name|string||true|类型名称|


## 7.8	用户

## 7.8.1	添加设备信息

> POST  /user/addDevice
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|string||true|设备码|
| source|number|1|true|来源，1=应用市场，2=Facebook，3=google ad|
| sourceFlag|string||false|来源标识|
| step|string||false|步骤标识|
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|string||true||
| msg|string||true||
| data|object||true||
|⇥ id|number||true|用户id，用于上传图片的路径地址：user/用户id/图片.jpg|
|⇥ uid|string||true|当前用户uid（已进行AES128加密，直接传回请求头即可，不用进行解密，客户端需要展示uid的地方才进行解密展示），存于设备本地，应用卸载时才会删除|


## 7.8.2	获取用户信息

> GET  /user/getInfo
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true||
| msg|string||true||
| data|object||true||
|⇥ id|number||true|用户id，用于上传图片的图片路径|
|⇥ integralBalance|number||true|积分余额|
|⇥ isVip|boolean||true|是否为vip，false=否，true=是|
|⇥ mineRedDot|boolean||true|我的页面是否展示红点，false=否，true=是|
|⇥ name|string||true|昵称|
|⇥ avatar|string||true|头像|
|⇥ password|string||true|账号密码，已进行AES128加密，展示时需要进行解密|
|⇥ sysNotifyRedDot|boolean||true|系统通知红点是否展示，false=否，true=是|
|⇥ clearTaskMsg|string||true|用户作品总数超出规定作品最大数量时的提示语，为空不展示|


## 7.8.3	更新用户信息

> POST  /user/updateInfo
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| name|string||true|昵称|
| file|file||false|头像图片文件|
### 响应体
● 200: OK 响应数据格式：JSON


## 7.8.4	切换账号

> POST  /user/change
### 接口说明
> 切换账号成功后，返回的uid替换掉本地保存的uid，刷新获取用户信息（user/getInfo）接口、获取用户作品列表（aiart/pageTaskList）接口
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| uid|string||true|uid|
| password|string||true|密码明文|
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true|200=正常，400=toast提示|
| msg|string||true||
| data|object||true||
|⇥ uid|string||true|切换的用户uid，接口混淆版本已进行AES128加密，直接传回请求头即可，不用进行解密，客户端需要展示uid的地方才进行解密展示|


## 7.8.5	更新用户密码

> POST  /user/updatePwd
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| password|string||true|密码，传参格式，5-15位的数字或字母|
### 响应体
● 200: OK 响应数据格式：JSON


## 7.9	作品

## 7.9.1	获取作品提交页面数据

> GET  /aiart/getGenerateSubmitPage
### 接口说明
> 跳转进入提交页面时请求
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| aiartId|number||true|模板id|
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true||
| msg|string||true||
| data|object||true||
|⇥ repeatPopupTitle|string||true|重复弹窗标题，文案为空时，则不判断该功能的弹窗|
|⇥ repeatPopupMsg|string||true|重复弹窗文案，文案为空时，则不判断该功能的弹窗|
|⇥ isConsumeIntegralPopup|boolean||true|是否弹窗积分消耗确认，false=否，true=是|
|⇥ consumeIntegralPopupTitle|string||true|积分消耗确认弹窗标题|
|⇥ consumeIntegralPopupMsg|string||true|积分消耗确认弹窗文案|
|⇥ isGenerateFreeEverydayPopup|boolean||true|是否弹窗操作频繁或系统繁忙，false=否，true=是|
|⇥ generateFreeEverydayPopupTitle|string||true|操作频繁或系统繁忙弹窗标题|
|⇥ generateFreeEverydayPopupMsg|string||true|操作频繁或系统繁忙弹窗文案|


## 7.9.2	获取用户作品列表

> GET  /aiart/pageTaskList
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| pageNum|number|1|true||
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|string||true||
| msg|string||true||
| data|object||true||
|⇥ records|array[object]||true||
|⇥⇥ taskId|string||true|生成任务id|
|⇥⇥ aiartType|number||true|类型，1=生成图片，2=生成视频|
|⇥⇥ state|number||true|状态，0=待解锁，1=待处理，2=处理中，3=已完成，4=处理失败|
|⇥⇥ outputUrl|string||true|生成图片或视频的链接地址|
|⇥⇥ showMsg|string||true|作品外展文案，不为空时展示|
|⇥⇥ againGenerateButtonMsg|string||true|再次生成按钮文案|
|⇥⇥ inputImgList|array[string]||true|原上传图片列表|
|⇥⇥ saveLocalPopupMsg|string||true|保存本地弹窗文案|
|⇥⇥ saveLocalDownloadingMsg|string||true|保存本地下载中提示文案|
|⇥⇥ downloadingMsg|string||true|下载中|
|⇥⇥ estimatTimeMsg|string||true|作品生成预估时间文案|
|⇥⇥ videoDurationMsg|string||true|视频时长展示文案|
|⇥⇥ aiartImg|string||true|模板图片地址|
|⇥⇥ aiartId|number||true|模板id|
|⇥ total|number||true||
|⇥ size|number||true||
|⇥ current|number||true||
|⇥ hasNext|boolean||true||


## 7.9.3	AI生成作品提交

> POST  /aiart/generateV1
### 接口说明
> <p style=""></p>
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| aiartId|number||true|模板id|
| file1|file||true|上传图片，可多张（多张时，传参名称为file1、file2）|
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|string||true||
| msg|string||true||
| data|object||true||
|⇥ taskId|string||true|任务id|
|⇥ showMsgOne|string||true|AI正在分析图像...|
|⇥ showMsgTwo|string||true|正在生成动态视频...|
|⇥ popupTitle|string||true|我们正在生成你的视频|
|⇥ popupTimeMsg|string||true|预计时间：10分钟|
|⇥ popupDescMsg|string||true|完成后我们会在你的作品里面为您呈现！|
|⇥ popupButtonMsg|string||true|探索更多AI视频|
|⇥ state|number||true|0=生产等待提醒弹窗，1=VIP拦截弹窗，2=充值积分拦截弹框|


## 7.9.4	AI生成作品重新提交

> POST  /aiart/againGenerate
### 接口说明
> 返回数据结构和 AI生成任务提交（aiart/generate）接口一致
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| taskId|string||true|任务id|
### 响应体
● 200: OK 响应数据格式：JSON


## 7.9.5	获取生成作品

> GET  /aiart/getTask
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| taskId|string||true|任务id|
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|string||true||
| msg|string||true||
| data|object||true||
|⇥ taskId|string||true|生成任务id|
|⇥ aiartType|number||true|类型，1=生成图片，2=生成视频|
|⇥ state|number||true|状态，1=待处理，2=处理中，3=已完成，4=处理失败|
|⇥ outputUrl|string||true|生成图片或视频的链接地址|
|⇥ showMsg|string||true|作品外展文案，不为空时展示|
|⇥ againGenerateButtonMsg|string||true|再次生成按钮文案|
|⇥ inputImgList|array[string]||true|原上传图片列表|
|⇥ saveLocalPopupMsg|string||true|保存本地弹窗文案|
|⇥ estimatTimeMsg|string||true|作品生成预估时间文案|
|⇥ videoDurationMsg|string||true|视频时长展示文案|
|⇥ saveLocalDownloadingMsg|string||true|保存本地下载中提示文案|
|⇥ downloadingMsg|string||true|下载中|


## 7.9.6	下载作品成功

> POST  /aiart/downloadTask
### 接口说明
> 下载作品成功后，调用该接口给后台记录状态
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| taskId|string||true||
### 响应体
● 200: OK 响应数据格式：JSON


## 7.9.7	删除作品

> POST  /aiart/delTask
### 接口说明
> 进入首页时请求
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| taskIdArr|array[string]||true|任务id数组|
### 响应体
● 200: OK 响应数据格式：JSON


## 7.9.8	提示词生成作品提交

> POST  /aiart/generateByKeyword
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| aiartId|number|0|true|模板id，未选用模板时传默认值0|
| type|number||true|类型，3=文生视频，4=文生图片|
| keyword|string||true|提示词|
| img|string||false|图片地址|
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|string||true||
| msg|string||true||
| data|object||true||
|⇥ taskId|string||true|任务id|
|⇥ showMsgOne|string||true|AI正在分析提示词...|
|⇥ showMsgTwo|string||true|正在进行创作...|
|⇥ popupTitle|string||true|我们正在生成你的视频|
|⇥ popupTimeMsg|string||true|预计时间：10分钟|
|⇥ popupDescMsg|string||true|完成后我们会在你的作品里面为您呈现！|
|⇥ popupButtonMsg|string||true|探索更多AI视频|
|⇥ state|number||true|0=生产等待提醒弹窗，1=VIP拦截弹窗，2=充值积分拦截弹框|


## 7.10	模板

## 7.10.1	首页信息

> GET  /home/getInfo
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|string||true||
| msg|string||true||
| data|object||true||
|⇥ aiartTopicList|object||true|主题列表|
|⇥⇥ id|number||true|主题id|
|⇥⇥ name|string||true|主题名称|
|⇥⇥ description|string||true|主题描述|
|⇥⇥ img|string||true|主题图片|
|⇥⇥ aiartList|array[object]||true|外展模板列表|
|⇥⇥⇥ id|string||true|模板id|
|⇥⇥⇥ name|string||true|模板名称|
|⇥⇥⇥ img|string||true|原图|
|⇥⇥⇥ uploadNum|number||true|需上传图片数量|
|⇥⇥⇥ lockType|number||true|解锁状态，0=免费，2=积分解锁|
|⇥⇥⇥ lockIntegral|number||true|解锁积分数量|
|⇥⇥⇥ useNumMsg|string||true|使用人数展示文案|
|⇥⇥⇥ sampleImgList|array[string]||true|模板示例图数组，示例图数量和当前模板需上传图片的数量保持一致|
|⇥⇥ useNumMsg|string||true|主题使用人数展示文案|


## 7.10.2	主题模板列表

> GET  /aiart/pageList
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| pageNum|number|1|true||
| topicId|number||true|主题id|
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true||
| msg|string||true||
| data|object||true||
|⇥ records|array[object]||true||
|⇥⇥ id|number||true|模板id|
|⇥⇥ name|string||true|模板名称|
|⇥⇥ img|string||true|模板图片|
|⇥⇥ uploadNum|number||true|需上传图片数量|
|⇥⇥ lockType|number||true|解锁状态，0=免费，2=积分解锁|
|⇥⇥ lockIntegral|number||true|解锁积分数量|
|⇥⇥ useNumMsg|string||true|使用人数展示文案|
|⇥⇥ sampleImgList|array[string]||true|模板示例图数组，示例图数量和当前模板需上传图片的数量保持一致|
|⇥ total|number||true||
|⇥ size|number||true||
|⇥ current|number||true||
|⇥ hasNext|boolean||true||
|⇥ searchCount|boolean||true||
|⇥ pages|number||true||


## 7.10.3	获取标签列表

> GET  /sys/listTag
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true||
| msg|string||true||
| data|array[object]||true||
|⇥ id|number||true|标签id|
|⇥ name|string||true|标签名称|


## 7.10.4	标签模板列表

> GET  /aiart/pageTagList
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| pageNum|number|1|true||
| tagId|number||true|标签id|
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true||
| msg|string||true||
| data|object||true||
|⇥ records|array[object]||true||
|⇥⇥ id|number||true|模板id|
|⇥⇥ name|string||true|模板名称|
|⇥⇥ img|string||true|模板图片|
|⇥⇥ uploadNum|number||true|需上传图片数量|
|⇥⇥ lockType|number||true|解锁状态，0=免费，2=积分解锁|
|⇥⇥ lockIntegral|number||true|解锁积分数量|
|⇥⇥ useNumMsg|string||true|使用人数展示文案|
|⇥⇥ sampleImgList|array[string]||true|模板示例图数组，示例图数量和当前模板需上传图片的数量保持一致|
|⇥ total|number||true||
|⇥ size|number||true||
|⇥ current|number||true||
|⇥ hasNext|boolean||true||
|⇥ searchCount|boolean||true||
|⇥ pages|number||true||


## 7.10.5	提示词创作模板列表

> GET  /aiart/pageTextList
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| pageNum|number|1|true||
| type|number||true|类型，3=图片编辑模板，4=文生视频模板|
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true||
| msg|string||true||
| data|object||true||
|⇥ records|array[object]||true||
|⇥⇥ id|number||true|模板id|
|⇥⇥ name|string||true|模板名称|
|⇥⇥ img|string||true|模板图片|
|⇥⇥ lockType|number||true|解锁状态，0=免费，2=积分解锁|
|⇥⇥ lockIntegral|number||true|解锁积分数量|
|⇥⇥ keyword|string||true|提示词|
|⇥ total|number||true||
|⇥ size|number||true||
|⇥ current|number||true||
|⇥ hasNext|boolean||true||
|⇥ searchCount|boolean||true||
|⇥ pages|number||true||


## 7.11	支付

## 7.11.1	获取VIP套餐

> GET  /vip/getProduct
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true||
| msg|string||true||
| data|object||true||
|⇥ vipList|array[object]||true|套餐列表|
|⇥⇥ name|string||true|套餐名称|
|⇥⇥ productId|string||true|产品id|
|⇥⇥ planId|string||true|订阅计划id|
|⇥⇥ giveIntegral|number||true|赠送积分|
|⇥⇥ description|string||true|描述|
|⇥⇥ tag|string||true|标签|
|⇥⇥ isDefault|number||true|是否默认选中，0=否，1=是|
|⇥ restroreFailMsg|string||true|当客户端获取用户订单token没有时，即toast该文案即可，不用再请求恢复购买校验接口|
|⇥ upgradeState|number||true|点击购买按钮时，是否需要弹窗版本更新（需要弹窗时，不进行购买操作），0=否，1=是|
|⇥ title|string||true|版本更新弹窗标题|
|⇥ content|string||true|版本更新弹窗内容|
|⇥ package|string||true|版本更新跳转的包名，已加密，需解密后使用|


## 7.11.2	获取积分套餐

> GET  /integral/getProduct
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true||
| msg|string||true||
| data|object||true||
|⇥ balance|number||true|积分余额|
|⇥ integralList|array[object]||true|套餐列表|
|⇥⇥ productId|string||true|产品id|
|⇥⇥ integral|number||true|积分数量|
|⇥⇥ tag|string||true|标签|
|⇥⇥ isDefault|number||true|是否默认选中，0=否，1=是|
|⇥ expireHintMsg|string||true|积分过期提示语，为空不展示|
|⇥ upgradeState|number||true|点击购买按钮时，是否需要弹窗版本更新（需要弹窗时，不进行购买操作），0=否，1=是|
|⇥ title|string||true|版本更新弹窗标题|
|⇥ content|string||true|版本更新弹窗内容|
|⇥ package|string||true|版本更新跳转的包名，已加密，需解密后使用|


## 7.11.3	添加内购版本更新确认标识

> POST  /sys/addPurchaseUpgrade
### 接口说明
> <p style="">在内购拦截版本更新弹窗点击 确定 按钮时，在跳转前调用此接口即可，不用处理该接口的返回数据</p>
### 响应体
● 200: OK 响应数据格式：JSON


## 7.11.4	google play校验

> POST  /googlePlay/verify
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| productId|string||true|商品id|
| orderId|string||true|订单id|
| purchaseToken|string||true|订单token|
| purchaseTime|number||true||
| purchaseState|number||true||
| modelType|number||true|1=支付回调监听，2=首页扫描|
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true||
| msg|string||true||
| data|object||true||
|⇥ verifyState|number||true|校验状态，0=成功，1=失败|
|⇥ msg|string||true|toast文案，不为空则toast即可|
|⇥ backState|number||true|回传付费金额状态，0=不回传，1=回传Facebook，2=回传google ad|
|⇥ backRate|number||true|回传付费金额比例，返回数值范围为0-100，先判断回传状态backState，可以回传再计算回传金额|
|⇥ id|number||true|订单id，更新回传数额 接口使用到|
|⇥ productType|number||true|商品类型，1=订阅，2=一次商品|


## 7.11.5	更新回传数额

> POST  /googlePlay/updateBackNum
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| id|number||true|订单id|
| backNum|number||true|数额，可小数|
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true||
| msg|string||true||


## 7.11.6	恢复购买校验

> POST  /googlePlay/restoreVerify
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| purchaseTokenList|array[string]||true||
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true||
| msg|string||true||
| data|object||true||
|⇥ isVerify|boolean||true|是否校验成功，true=成功（替换用户当前存储的uid，并且刷新获取用户信息接口）|
|⇥ uid|string||true|用户uid，接口混淆版本已进行AES128加密，直接传回请求头即可，不用进行解密，客户端需要展示uid的地方才进行解密展示|
|⇥ msg|string||true|toast文案，不为空则toast即可|


## 7.11.7	苹果支付校验

> POST  /apple/verify
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| transactionId|string||true||
| receiptData|string||true||
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true||
| msg|string||true||
| data|object||true||
|⇥ isFinish|boolean||true|是否支付完成，true=是，false=否|
|⇥ transactionId|string||true||


## 7.12	意见反馈

## 7.12.1	意见反馈

> POST  /advice/add
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| content|string||false|反馈内容|
| contact|string||false|邮箱（安卓端必传，ios端根据实际需求判断是否必传）|
| sysTypeId|number||false|类型id（安卓端必传，ios端根据实际需求判断是否必传）|
### 响应体
● 200: OK 响应数据格式：JSON


## 7.13	举报

## 7.13.1	举报提交

> POST  /report/add
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| content|string||false|举报内容|
| sysTypeId|number||true|类型id|
### 响应体
● 200: OK 响应数据格式：JSON


## 7.14	签到

## 7.14.1	签到弹框信息

> GET  /signIn/getInfo
### 接口说明
> 进入首页时请求
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|string||true||
| msg|string||true||
| data|object||true||
|⇥ isSignInToday|boolean||true|今天是否已签到|
|⇥ signInDay|number||true|当前签到的天数，范围0-7,0代表周期内首次且未签到|
|⇥ signInIntegralArr|array[number]||true|签到的积分数量数组|
|⇥ signInIntegralMsgArr|array[string]||true|签到的积分数量下的文案数组|
|⇥ popupTitle|string||true|签到弹框标题|
|⇥ signInDesc|string||true|签到弹框标题下的说明文案|
|⇥ signInButMsg|string||true|签到按钮文案|
|⇥ signInMsg|string||true|签到按钮下的说明文案，为空则不展示|


## 7.14.2	签到提交

> POST  /signIn/add
### 接口说明
> 进入首页时请求
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| signInDay|number||true|签到天数，从签到信息接口获取传回|
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|string||true|200=toast提示msg的内容，并且刷新获取用户信息接口|
| msg|string||true||


## 8	壁纸

## 8.1	壁纸首页信息

> GET  /home/getWallInfo
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|string||true||
| msg|string||true||
| data|object||true||
|⇥ wallTopicList|object||true|主题列表|
|⇥⇥ id|number||true|主题id|
|⇥⇥ name|string||true||
|⇥⇥ description|string||true|主题描述|
|⇥⇥ img|string||true||
|⇥⇥ wallList|array[object]||true|外展壁纸列表|
|⇥⇥⇥ id|string||true|壁纸id|
|⇥⇥⇥ name|string||true||
|⇥⇥⇥ img|string||true|原图|


## 8.2	主题壁纸列表

> GET  /wall/pageList
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| pageNum|number|1|true||
| topicId|number||true|主题id|
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|number||true||
| msg|string||true||
| data|object||true||
|⇥ records|array[object]||true||
|⇥⇥ id|number||true||
|⇥⇥ name|string||true||
|⇥⇥ img|string||true||
|⇥ total|number||true||
|⇥ size|number||true||
|⇥ current|number||true||
|⇥ hasNext|boolean||true||
|⇥ searchCount|boolean||true||
|⇥ pages|number||true||


## 8.3	壁纸收藏添加或取消

> POST  /wall/addOrUpdateCollect
### 请求体(Request Body)
| 参数名称 | 数据类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| id|number||true|壁纸id|
| type|number||true|1=添加收藏，2=取消收藏|
### 响应体
● 200: OK 响应数据格式：JSON


## 8.4	收藏壁纸列表

> GET  /wall/listCollect
### 响应体
● 200: OK 响应数据格式：JSON
| 参数名称 | 类型 | 默认值 | 不为空 | 描述 |
| ------ | ------ | ------ | ------ | ------ |
| code|string||true||
| msg|string||true||
| data|array[object]||true||
|⇥ id|string||true||
|⇥ name|string||true||
|⇥ img|string||true||

