package cloud.carvis.api.common.dao

interface BaseRepository<T, HashKey> {

    fun findAll(): List<T>

    fun findByHashKey(hashKey: HashKey): T?

    fun save(entity: T): T

    fun deleteByHashKey(hashKey: HashKey)

    fun count(): Int

    fun existsByHashKey(hashKey: HashKey) = findByHashKey(hashKey) !== null

}
