package com.alibaba.android.arouter.base;

import java.util.TreeMap;

/**
 * TreeMap with unique key. 如果已经存在某一个 key，再次 put 该 key 则会抛出异常。
 *
 * @author zhilong <a href="mailto:zhilong.lzl@alibaba-inc.com">Contact me.</a>
 * @version 1.0
 * @since 2017/2/22 下午5:01
 */
public class UniqueKeyTreeMap<K, V> extends TreeMap<K, V> {

    private String tipText;

    public UniqueKeyTreeMap(String exceptionText) {
        super();
        tipText = exceptionText;
    }

    @Override
    public V put(K key, V value) {
        if (containsKey(key)) {
            throw new RuntimeException(String.format(tipText, key));
        } else {
            return super.put(key, value);
        }
    }

}
