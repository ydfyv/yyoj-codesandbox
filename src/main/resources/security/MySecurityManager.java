import java.security.Permission;

public class MySecurityManager extends SecurityManager{

    // 检测用户权限
    @Override
    public void checkPermission(Permission perm) {
//        super.checkPermission(perm);
    }

    // 检测程序是否允许读取
    @Override
    public void checkRead(String file) {
//        super.checkRead(file);
        if (file.contains("D:\\learn\\oj\\yyoj-codesandbox")) return;
        throw new RuntimeException("权限异常" + file);
    }

    // 检测程序是否允许写入
    @Override
    public void checkWrite(String file) {
//        super.checkWrite(file);
        throw new RuntimeException("权限异常" + file);
    }

    // 检测程序是否允许删除
    @Override
    public void checkDelete(String file) {
//        super.checkDelete(file);
        throw new RuntimeException("权限异常" + file);
    }

    // 检测程序是否允许连接网络
    @Override
    public void checkConnect(String host, int port) {
//        super.checkConnect(host, port);
        throw new RuntimeException("权限异常" + "host:" +  port);
    }
}
