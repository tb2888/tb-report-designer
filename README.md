# TbReport · 可嵌入 Spring Boot 3 的轻量低代码报表工具（社区版）

TbReport 是一个**低代码报表 / 报表设计器**：在浏览器里画表格、绑定字段，就能做出**销售报表、对账单、发货单、检验单、工资单**这类**数据报表与打印表单**。
支持**循环块（明细自动展开）、主子表、分组汇总、二维码/条码、1:1 打印套打、导出 Excel**；前端页面随 jar 一起发布，**一个依赖**嵌进你的 Spring Boot 3 项目，不用单独部署前端。
可作为 **JimuReport（积木报表）** 一类商业报表工具的**社区版替代方案**。

> Lightweight low-code reporting for Java / Spring Boot 3: visual report designer, master-detail & loop blocks,
> SQL / HTTP / JSON datasets, 1:1 print forms, Excel export — front-end included, embed with one jar.

本仓库**只包含演示程序（`report-demo`）的源码与文档**，报表引擎与集成外壳以**编译好的 jar** 形式提供
（`lib/tb-report-spring-boot-starter-*.jar`），因此 clone 下来不含引擎源码。

你可以用它做两件事：

| 我想…… | 看哪一节 |
|---|---|
| 直接跑起来看看长什么样 | [一、30 秒启动演示](#一30-秒启动演示) |
| 把它嵌进我自己的系统 | [二、集成到你的 Spring Boot 项目](#二集成到你的-spring-boot-项目) |
| 自己编译本仓库的 demo | [三、自己编译 demo](#三自己编译-demo) |
| 了解免费版/商业版功能差别 | [四、版本功能说明](#四版本功能说明) |

---

## 一、30 秒启动演示

**前提**：本机有 **Java 17 或更高**（`java -version` 能看到 17+）。

1. 到本仓库的 **Releases** 页面下载 `tb-report-demo-<版本>.jar`（约 45 MB，含内嵌前端与示例数据库，所以不放进 git）。
2. 在任意目录执行：

```bash
java -jar tb-report-demo-<版本>.jar --report.metadata.datasource-id=MAIN
```

3. 浏览器打开 **http://localhost:8080/report/reports**

`--report.metadata.datasource-id=MAIN` 的意思是"报表配置数据放在自带的本地文件数据库里"——
不写这个参数它会去找本地 MySQL，没有就启动失败（见[常见问题](#六常见问题)）。

启动后你会看到：

| 页面 | 地址 | 干什么的 |
|---|---|---|
| 报表列表 | `/report/reports` | 新建/打开/复制报表，进入设计器与预览 |
| 设计器 | `/report/designer/{id}` | 拖字段、画表格、设循环块、配打印 |
| 预览 | `/report/viewer/{id}` | 填参数、看真实数据、打印/导出 Excel |
| 数据集 | `/report/datasets` | 配 SQL / HTTP 接口 / JSON / JavaBean 取数，解析字段 |
| 数据源 | `/report/datasources` | 看配置文件里声明了哪些库 |
| 数据字典 | `/report/dicts` | 字段值 → 显示名（手工 / 接口 / SQL） |
| 纸张管理 | `/report/papers` | 维护纸张规格 |
| 使用帮助 | `/report/help` | **操作手册**（含截图）：从建数据集到打印的完整流程 |

> 演示数据会写在你执行命令的那个目录下的 `tb-report.mv.db`，删掉它就回到"全新状态"。

---

## 二、集成到你的 Spring Boot 项目

### 1）把 jar 装进你的本地仓库

本仓库 `lib/` 目录下的就是集成用的 jar：

```bash
mvn install:install-file -Dfile=lib/tb-report-spring-boot-starter-<版本>.jar ^
  -DgroupId=io.github.tb2888 -DartifactId=tb-report-spring-boot-starter -Dversion=<版本> -Dpackaging=jar
```

（Linux/macOS 把行尾的 `^` 换成 `\`。）

### 2）在你的 pom 里加依赖

```xml
<dependency>
  <groupId>io.github.tb2888</groupId>
  <artifactId>tb-report-spring-boot-starter</artifactId>
  <version><版本></version>
</dependency>
```

### 3）你需要自备的东西

- **Spring Boot 3.3.x**（web 与 jdbc 会由本 starter 带过去）
- **你自己的数据库 JDBC 驱动**（本 starter 故意不强制任何驱动：MySQL / Oracle / PG / SQL Server / H2 都行）
- 想用后端导出 xlsx，再引 `poi-ooxml`（不引就只有前端导出，不影响其他功能）

### 4）最小配置（`application.yml`）

```yaml
report:
  enabled: true
  # 报表的元数据（报表/数据集/纸张/字典）放哪个库：
  #   MAIN = 你应用的主库；也可以填下面 datasources 里声明的 id
  metadata:
    datasource-id: MAIN
  # 数据源密码的加密密钥：20 位以上、大小写字母+数字，必须改掉
  encrypt-key: 请改成你自己的随机串_20位以上
  datasources:
    - id: biz
      name: 业务库
      type: MYSQL
      url: "jdbc:mysql://127.0.0.1:3306/mydb?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
      username: root
      password: ""        # 支持三种写法：ENC(密文) / ${环境变量} / 明文
```

元数据表（`tb_report*`）会在启动时**自动创建**，不用手工建表；升级版本也会自动补列扩列。

- 密码不想写明文：进 `/report/password-tool` 页面生成 `ENC(...)` 密文再填进来。
- 更多配置项（表名前缀、渲染行数上限、超时、多数据源池参数等）见 `report.*` 的注释说明。

### 5）需要鉴权时

默认**放行所有请求**。要接你自己的登录态，实现一个 Bean 即可（接口名与签名保持稳定，不会因版本升级而变）：

```java
@Bean
public ReportAccessProvider reportAccessProvider() {
    return request -> {
        // 返回 false → 403；在这里读你的 token/session
        return myAuthService.isLoggedIn(request);
    };
}
```

### 6）前后端一套带走

前端页面已经打进 jar（`/report/**`），**不需要你单独部署前端**。启动后直接访问
`http://你的域名:端口/report/reports`。

---

## 三、自己编译 demo

1. 先按[第二节第 1 步](#1把-jar-装进你的本地仓库)把 `lib/` 里的 starter jar 装进本地 Maven 仓库（demo 依赖它）。
2. 然后：

```bash
mvn clean package
java -jar report-demo/target/tb-report-demo-<版本>.jar
```

本仓库里的 `application.yml` 已经默认落在本地 H2 文件库（`MAIN`），无需额外参数。

---

## 四、版本功能说明

| 功能 | 免费版 | 商业版 |
|---|---|---|
| 报表设计 / 循环块 / 主子表 / 分组汇总 / 统计 | ✅ | ✅ |
| 数据集（SQL / HTTP 接口 / JSON / JavaBean） | ✅ | ✅ |
| 数据字典、纸张管理、字段值翻译 | ✅ | ✅ |
| 打印（1:1 / 边距 / 页眉页脚 / 参考线）、Excel 导出 | ✅ | ✅ |
| C-Lodop 插件打印（非标纸套打 1:1） | ✅ | ✅ |
| **每页页码 / 本单据页码** | 置灰 | ✅ |
| **电子印章（设计器定位、随内容走）** | 置灰 | ✅ |
| **固定打印表头 / 表尾（每页重复）** | 置灰 | ✅ |

本仓库提供的 starter jar 是**免费版**构建产物：上表标「置灰」的三项（每页页码 / 电子印章 / 固定打印表头表尾）入口置灰并提示，其余功能完全一致。
需要这三项功能请购买商业授权：**11295920@qq.com**。

---

## 五、目录结构

```
.
├── README.md
├── pom.xml                 # 只聚合 report-demo
├── report-demo/            # 演示应用源码（Spring Boot 宿主 + 示例数据 + mock 接口）
│   └── src/main/resources/db/  # 示例业务表结构与数据
└── lib/
    └── tb-report-spring-boot-starter-<版本>.jar   # 集成用（含引擎与内嵌前端）
```

---

## 六、常见问题

**启动报数据库连接错误？**
没加 `--report.metadata.datasource-id=MAIN`，它在找你本机的 MySQL。加上这个参数即可用自带文件库。

**端口被占用？**
`java -jar tb-report-demo-<版本>.jar --server.port=9090`

**数据想重置？**
删掉运行目录下的 `tb-report.mv.db`（以及 `demo-biz.mv.db`）后重启。

**浏览器打印出来有空白页 / 页面被裁？**
看 `/report/help` 的「打印设置」一节：纸张、边距、缩放必须按说明设置（缩放 100%、边距最小/默认），
页面里还有一条自检栏会直接告诉你能打印区域多大的问题。非标纸/套打建议用插件打印（C-Lodop）。

**升级版本要注意什么？**
替换 `lib/` 里的 jar 与依赖版本即可；元数据表会自动增量升级，老数据保留。

---

## 七、许可（License）

本仓库采用 **TbReport 社区版许可协议 v1.0（源码可见许可，source-available）**，完整条款见 [`LICENSE`](LICENSE)。
它**不是** OSI 认证的开源协议——因为它限制了部分使用方式。用一句话概括：

### ✅ 你可以

- 自己用、在公司内部系统里用；
- **把它集成进你自己的商业项目**，对外提供服务（只要这个服务的主要价值不是你转卖的"报表工具"本身）；
- 修改本仓库提供的 demo/文档源码以适配你的项目；
- 把（原样或修改过的）本仓库源码再分发出去——前提是**带上原始 LICENSE 并保留署名**。

### ❌ 你不可以

- 把本软件（或改个名字的版本）**当作独立商品/独立服务卖钱、转售、出租**（含以 SaaS 形式单独对外提供这个报表工具本身）；
- 对 **`lib/` 里的 jar（二进制部分）反编译**、反汇编、破解；
- **绕过或禁用任何授权校验 / 功能开关**，或通过改配置、改字节码等方式启用未授权的商业版功能；
- 删除或修改版权声明与 LICENSE 文本，或换成别的协议；
- 用本软件做出一个功能实质相同、可以替代它的竞争产品。

### ✍️ 商业使用需要署名

请在你产品的"关于/版权信息"、产品文档或项目 README 中显著注明，例如：

```
本产品使用了 TbReport（https://github.com/tb2888/tb-report-designer）
```

### 💼 商业版功能

每页页码 / 电子印章 / 固定打印表头表尾属于**商业版功能**，需取得商业授权后使用。本仓库提供的
jar 中这些入口会置灰。

### 🔧 那我能改它的功能吗？

- 改**报表模板、样式、打印设置、数据集、SQL/接口、参数、字典**，以及**你自己的集成代码与鉴权逻辑**
  → 随便改，**不需要本软件的源码**（通过设计器界面和公开 API 即可）。
- 需要改**引擎内部逻辑**（渲染算法、内置函数、导出口径等）→ 属于**源码授权 / 商业定制**，请联系我们；
  也欢迎提需求，我们可能会把它做成新的**扩展点（SPI）**，这样所有人都不用改 jar 就能扩展。

> 需要商业授权、源码授权、或对本协议有疑问，请联系：11295920@qq.com

