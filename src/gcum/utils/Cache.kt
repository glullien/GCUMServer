package gcum.utils

import java.util.concurrent.ConcurrentHashMap

class Cache<K, V> {
   private val cache = ConcurrentHashMap<K, V>()
   fun get(k: K, v: () -> V) = cache.computeIfAbsent(k, {v()})
}