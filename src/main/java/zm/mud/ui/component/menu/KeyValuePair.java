package zm.mud.ui.component.menu;

public class KeyValuePair<K,V> {
    private final K key;
    private final V value;

    public KeyValuePair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    // 核心：JComboBox 会自动调用这个方法来展示文本
    @Override
    public String toString() {
        return String.valueOf(value); 
    }

}
