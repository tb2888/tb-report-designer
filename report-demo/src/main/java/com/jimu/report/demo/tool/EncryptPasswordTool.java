package com.jimu.report.demo.tool;

import com.jimu.report.core.util.AesUtil;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 命令行：把明文密码转成可以填进配置文件的密文。
 *
 * <p><b>为什么需要它</b>：{@code report.datasources[].password} 支持写成 {@code ENC(Base64密文)}，
 * 这样配置文件里就不再出现明文密码。密文必须用**当前服务的 {@code report.encrypt-key}** 生成，
 * 否则服务启动时解不开、连不上库。本类默认就去读 classpath 下 {@code application.yml} 里的那个值，
 * 从根上避免「密钥抄错」。
 *
 * <p><b>怎么跑</b>（在项目根目录）：
 * <pre>
 * # 方式一：命令行直接带明文（推荐脚本化使用）
 * mvn -q -pl report-demo org.codehaus.mojo:exec-maven-plugin:3.5.0:java \
 *     "-Dexec.mainClass=com.jimu.report.demo.tool.EncryptPasswordTool" "-Dexec.args=你的密码"
 *
 * # 方式二：不带参数，回车后从控制台读入明文（不会回显到历史记录里）
 * mvn -q -pl report-demo org.codehaus.mojo:exec-maven-plugin:3.5.0:java \
 *     "-Dexec.mainClass=com.jimu.report.demo.tool.EncryptPasswordTool"
 * </pre>
 * IDE 里直接右键本类 Run 也可以（同样支持控制台输入）。
 *
 * <p>输出里的 {@code ENC(...)} 整串整串填到 {@code password:} 后面即可，例如：
 * <pre>
 * report:
 *   datasources:
 *     - id: mysql-biz
 *       password: ENC(Base64密文)
 * </pre>
 *
 * <p>想临时指定另一个密钥：第二个参数传 {@code report.encrypt-key}（一般用不到）。
 */
public final class EncryptPasswordTool {

    private static final String KEY_PROPERTY = "report.encrypt-key";

    private EncryptPasswordTool() {
    }

    public static void main(String[] args) throws Exception {
        useNativeConsoleEncoding();
        String plain = args.length > 0 ? args[0] : readFromConsole("请输入要加密的密码：");
        if (plain == null || plain.isEmpty()) {
            System.err.println("没有读到密码，已退出。");
            System.exit(1);
            return;
        }
        String key = args.length > 1 ? args[1] : loadKeyFromClasspath();

        String cipher = AesUtil.encrypt(plain, key);
        // 自检：立刻解一次，确认"生成的密文一定能被这个密钥解开"
        if (!plain.equals(AesUtil.decrypt(cipher, key))) {
            System.err.println("自检失败：生成的密文解不回原文，请检查密钥。");
            System.exit(2);
            return;
        }

        System.out.println();
        System.out.println("使用的密钥：" + mask(key) + "（取自 " + (args.length > 1 ? "命令行参数" : "application.yml") + "）");
        System.out.println("明文长度　：" + plain.length() + " 个字符（不在此回显）");
        System.out.println();
        System.out.println("纯密文（存数据库用）：");
        System.out.println(cipher);
        System.out.println();
        System.out.println("填进 application.yml（整串复制到 password: 后面）：");
        System.out.println("password: " + AesUtil.wrapCipher(cipher));
        System.out.println();
        System.out.println("提示：密文里的 IV 是随机的，同一个密码每次生成的结果都不同，任选一个用即可。");
    }

    /**
     * Java 17 起 {@code System.out} 固定按 UTF-8 输出，而中文 Windows 控制台按 GBK 解码 →
     * 提示文字会变成乱码。这里按 JVM 报出的「本机编码」重新包一层 stdout。
     */
    private static void useNativeConsoleEncoding() {
        String nativeEncoding = System.getProperty("native.encoding");
        if (nativeEncoding == null || nativeEncoding.isBlank()) {
            return;
        }
        try {
            System.setOut(new java.io.PrintStream(new java.io.FileOutputStream(java.io.FileDescriptor.out), true,
                    java.nio.charset.Charset.forName(nativeEncoding)));
        } catch (Exception ignored) {
            // 包不上就算了，最多中文乱码，不影响密文本身
        }
    }

    /** 从 classpath 的 application.yml 读 report.encrypt-key，与运行时用的是同一份配置。 */
    private static String loadKeyFromClasspath() throws Exception {
        Resource resource = new ClassPathResource("application.yml");
        if (!resource.exists()) {
            throw new IllegalStateException("classpath 下找不到 application.yml，"
                    + "请在第二个参数里显式传入 report.encrypt-key");
        }
        List<PropertySource<?>> sources =
                new YamlPropertySourceLoader().load("application", resource);
        for (PropertySource<?> source : sources) {
            Object value = source.getProperty(KEY_PROPERTY);
            if (value != null && !String.valueOf(value).isBlank()) {
                String key = String.valueOf(value);
                if (key.contains("${")) {
                    throw new IllegalStateException("application.yml 里的 " + KEY_PROPERTY
                            + " 是占位符（" + key + "），本工具无法解析环境变量，请用第二个参数显式传入实际密钥");
                }
                return key;
            }
        }
        throw new IllegalStateException("application.yml 里没有配置 " + KEY_PROPERTY);
    }

    private static String readFromConsole(String prompt) throws Exception {
        System.out.print(prompt);
        System.out.flush();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            return line == null ? null : line.trim();
        }
    }

    /** 密钥只回显首尾各 2 个字符，用于确认「用的是哪个密钥」，同时避免把完整密钥打到终端/日志里。 */
    private static String mask(String key) {
        if (key == null || key.isEmpty()) {
            return "(空)";
        }
        if (key.length() <= 4) {
            return "****";
        }
        return key.substring(0, 2) + "*".repeat(Math.min(key.length() - 4, 12)) + key.substring(key.length() - 2);
    }
}
