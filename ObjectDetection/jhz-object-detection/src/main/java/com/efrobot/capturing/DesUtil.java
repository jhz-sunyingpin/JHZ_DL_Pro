package com.efrobot.capturing;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class DesUtil {
    private static String keyfileName = "DesKey.xml";
    private final static String ENCODE = "GBK";
    public void setKeyfileName(String keyfileName1)
    {
        keyfileName = keyfileName1;
    }
    /**
     * <p> DES解密文件
     * @param file 需要解密的文件
     * @param dest 解密后的文件
     * @throws Exception
     */
    public static void decrypt(String file, String dest) throws Exception {
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, getKey());
        InputStream is = new FileInputStream(file);
        OutputStream out = new FileOutputStream(dest);
        CipherOutputStream cos = new CipherOutputStream(out, cipher);
        byte[] buffer = new byte[1024];
        int r;
        while ((r = is.read(buffer)) >= 0) {
            cos.write(buffer, 0, r);
        }
        cos.close();
        out.close();
        is.close();
    }
    /**
     * <p>DES加密文件
     * @param file 源文件
     * @param destFile 加密后的文件
     * @throws Exception
     */
    public static void encrypt(String file, String destFile) throws Exception {
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, getKey());
        InputStream is = new FileInputStream(file);
        OutputStream out = new FileOutputStream(destFile);
        CipherInputStream cis = new CipherInputStream(is, cipher);
        byte[] buffer = new byte[1024];
        int r;
        while ((r = cis.read(buffer)) > 0) {
            out.write(buffer, 0, r);
        }
        cis.close();
        is.close();
        out.close();
    }

    /**
     * 根据byte数组生成文件
     *
     * @param bytes
     *            生成文件用到的byte数组
     */
    private static void createFileWithByte(byte[] bytes,String filePath) {
        // TODO Auto-generated method stub
        /**
         * 创建File对象，其中包含文件所在的目录以及文件的命名
         */
        File file = new File(filePath);
        // 创建FileOutputStream对象
        FileOutputStream outputStream = null;
        // 创建BufferedOutputStream对象
        BufferedOutputStream bufferedOutputStream = null;
        try {
            // 如果文件存在则删除
            if (file.exists()) {
                file.delete();
            }
            // 在文件系统中根据路径创建一个新的空文件
            file.createNewFile();
            // 获取FileOutputStream对象
            outputStream = new FileOutputStream(file);
            // 获取BufferedOutputStream对象
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            // 往文件所在的缓冲输出流中写byte数据
            bufferedOutputStream.write(bytes);
            // 刷出缓冲输出流，该步很关键，要是不执行flush()方法，那么文件的内容是空的。
            bufferedOutputStream.flush();
        } catch (Exception e) {
            // 打印异常信息
            e.printStackTrace();
        } finally {
            // 关闭创建的流对象
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    /**
     * Description 根据键值进行加密
     *
     * @param data
     * @param strKey
     *            加密键字符串
     * @param destFile
     *            保存文件
     * @return
     * @throws Exception
     */
    public static void encrypt(byte[] data,String strKey, String destFile) throws Exception {
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
        byte[] key = strKey.getBytes(ENCODE);
        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance("DES");

        // 用密钥初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);

        byte[] dataEncrypt = cipher.doFinal(data);

        createFileWithByte(dataEncrypt,destFile);
    }

    /**
     * Description 根据键值进行解密
     *
     * @param enfile
     *          加密文件
     * @param strKey
     *            加密键字符串
     * @param deFile
     *          解密文件
     * @return
     * @throws Exception
     */
    public static void decrypt(String strKey , String enfile, String deFile) throws Exception {
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
        byte[] key = strKey.getBytes(ENCODE);
        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance("DES");

        // 用密钥初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

        //byte[] dataDecrypt = cipher.doFinal(data);

        InputStream is = new FileInputStream(enfile);
        OutputStream out = new FileOutputStream(deFile);
        CipherOutputStream cos = new CipherOutputStream(out, cipher);
        byte[] buffer = new byte[1024];
        int r;
        while ((r = is.read(buffer)) >= 0) {
            cos.write(buffer, 0, r);
        }
        cos.close();
        out.close();
        is.close();
    }

    private static Key getKey() {
        Key kp = null;
        try {
            String fileName = keyfileName;
            InputStream is = new FileInputStream(fileName);
            ObjectInputStream oos = new ObjectInputStream(is);
            kp = (Key) oos.readObject();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return kp;
    }
    public static void main(String[] args) throws Exception {
        DesUtil.saveDesKey();
        DesUtil.encrypt("desinput.txt", "desoutput.txt");
        DesUtil.decrypt("desoutput.txt","desinput2.txt");
        //desinput.txt 经过加密和解密后生成的 desinput2.txt 应该与源文件一样
    }
    /**
     * <p> 生成KEY，并保存
     */
    public static void saveDesKey(){
        try {
            SecureRandom sr = new SecureRandom();
            //为我们选择的DES算法生成一个KeyGenerator对象
            KeyGenerator kg = KeyGenerator.getInstance ("DES" );
            kg.init (sr);
            FileOutputStream fos = new FileOutputStream(keyfileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            //生成密钥
            Key key = kg.generateKey();
            oos.writeObject(key);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
