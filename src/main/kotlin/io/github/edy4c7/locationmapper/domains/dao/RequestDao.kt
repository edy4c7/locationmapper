package io.github.edy4c7.locationmapper.domains.dao

import io.github.edy4c7.locationmapper.domains.entities.Request
import org.seasar.doma.*
import org.seasar.doma.boot.ConfigAutowireable

@Dao
@ConfigAutowireable
interface RequestDao {
    @Sql("""
        select * from request where id = /*id*/0
    """)
    @Select
    fun selectById(id: String): Request

    @Insert
    fun insert(request: Request): Int

    @Update
    fun update(request: Request): Int
}
