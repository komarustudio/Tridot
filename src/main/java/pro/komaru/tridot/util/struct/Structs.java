package pro.komaru.tridot.util.struct;

import pro.komaru.tridot.util.struct.func.Cons;
import pro.komaru.tridot.util.struct.func.Func;
import pro.komaru.tridot.util.struct.func.Prov;

import java.io.*;
import java.util.*;

public class Structs {

    public static <A> Prov<A> nil(){
        return () -> null;
    }

    public static  <T> T or(T a, T b) {
        if(a == null) return b;
        return a;
    }
    public static String or(String a, String b) {
        if(a == null) return b;
        if(a.isBlank()) return b;
        return a;
    }
    public static <T,A> A safeGet(T a, Func<T,A> cb) {
        return safeGet(a,cb,nil());
    }
    public static <T,A> A safeGet(T a, Func<T,A> cb, Prov<A> def) {
        if(a != null) return cb.get(a);
        return def.get();
    }
    public static <T> void safeRun(T obj, Cons<T> cb) {
        if(obj != null) cb.get(obj);
    }
    public static <K,V> HashMap<K,V> map(Class<K> key, Class<V> val, Object... objs) {
        HashMap<K,V> map = new HashMap<>();
        for (int i = 0; i < objs.length; i+=2) {
            K k = (K) objs[i];
            V v = (V) objs[i+1];
            map.put(k,v);
        }
        return map;
    }
    public static <K,V> HashMap<V,K> revMap(HashMap<K,V> mapOrig) {
        HashMap<V,K> map = new HashMap<>();
        mapOrig.forEach((k,v) -> map.put(v,k));
        return map;
    }
}
