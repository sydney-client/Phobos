package net.sydneyclient.phobos.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CryptoHelper {
    public static String encrypt(String input) {
        try {
            Class<?> clazz = Class.forName("org.phobos.secure.zldfmHZHeiUwGiHn9xaS24B8lnBqhcRI.wk6hvwV3gAlBeUi1fOf19fLTxgNHbhzT");

            Method method = clazz.getDeclaredMethod("OwAa4zUSNtyeOruH8S4ytFApua1Cnyvr", String.class);
            method.setAccessible(true);

            return (String) method.invoke(null, input);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
            return "";
        }
    }

    public static String decrypt(String input) {
        try {
            Class<?> clazz = Class.forName("org.phobos.secure.zldfmHZHeiUwGiHn9xaS24B8lnBqhcRI.wk6hvwV3gAlBeUi1fOf19fLTxgNHbhzT");

            Method decode = clazz.getDeclaredMethod("32IAWUQxlpZEGPQw7CmpythoD9I1Lqsn", String.class);
            decode.setAccessible(true);

            return (String) decode.invoke(null, input);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
            return "";
        }
    }
}
